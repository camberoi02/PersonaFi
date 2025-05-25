package com.example.personafi;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.example.personafi.CircularProgressView;
import com.example.personafi.ConfettiView;
import com.example.personafi.AnimatedShapesView;
import com.example.personafi.utils.ToastUtils;
import com.example.personafi.utils.TimeUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.List;

public class SavingsFragment extends Fragment {
    
    private SavingsViewModel savingsViewModel;
    private RecyclerView recyclerView;
    private SavingsGoalAdapter adapter;
    private CircularProgressView circularProgressOverallSavings;
    private TextView textOverallProgress;
    private TextView textOverallPercentage;
    private TextView textActiveGoalsCount;
    private TextView textAchievedGoalsCount;
    private TextView textMonthlySavingsGoal;
    private TextView textGoalPeriodLabel;
    private TextView textSavingsTip;
    private TextView textSortGoals;
    private ImageView buttonRefreshTip;
    private LinearLayout layoutEmptyState;
    private MaterialButton buttonCreateFirstGoal;
    private ConfettiView confettiView;
    private View cardBackground;
    private AnimatedShapesView animatedShapesBackground;
    private NumberFormat currencyFormatter;
    
    // Search-related fields
    private EditText editSearch;
    private ImageView buttonClearSearch;
    private MaterialCardView cardSearch;
    private LiveData<List<SavingsGoal>> searchResultsLiveData;
    private LiveData<List<SavingsGoal>> allSavingsGoalsLiveData;
    private boolean isSearchActive = false;
    
    private int currentSortMethod = 0; // 0: Date, 1: Progress, 2: Amount, 3: Name
    
    private boolean previouslyHadNoGoals = true;
    private double previousSavingsPercentage = 0;
    private boolean isInitialLoad = true; // Add this flag to prevent multiple UI updates during initial load
    
    // Handler and Runnable for auto-rotating tips
    private Handler tipRotationHandler;
    private Runnable tipRotationRunnable;
    private static final int TIP_ROTATION_INTERVAL = 15000; // 15 seconds
    
    private final String[] savingsTips = {
        "Try the 50/30/20 rule: 50% of income for needs, 30% for wants, and 20% for savings.",
        "Set up automatic transfers to your savings account on payday.",
        "Challenge yourself to a no-spend week each month.",
        "Use cash envelopes for everyday spending to avoid overspending.",
        "Review and cancel unused subscriptions and memberships.",
        "Save loose change in a jar and deposit it monthly.",
        "Wait 24 hours before making any non-essential purchase.",
        "Cook meals at home instead of eating out.",
        "Look for student discounts on services and products.",
        "Set specific savings goals to stay motivated.",
        "Pay yourself first - treat savings as a non-negotiable expense.",
        "Use the 24-hour rule: wait a day before making unplanned purchases.",
        "Try meal planning to reduce food waste and grocery expenses.",
        "Consider sharing subscriptions with family or friends.",
        "Set up price alerts for items you want to purchase.",
        "Use cashback apps and credit cards for everyday purchases.",
        "Repair items instead of replacing them when possible.",
        "Buy quality items that last longer rather than cheap alternatives.",
        "Shop with a list to avoid impulse purchases.",
        "Turn saving into a game or challenge with friends or family."
    };
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private Date selectedTargetDate;
    
    private final String[] savingsCategories = {
        "General Savings",
        "Emergency Fund",
        "Education",
        "Travel",
        "Home",
        "Vehicle",
        "Technology",
        "Business",
        "Wedding",
        "Gift"
    };
    
    // Add a new field to track the current goal period display mode
    private int goalPeriodMode = 2; // 0: Daily, 1: Weekly, 2: Monthly (default)
    
    // Constants for SharedPreferences
    private static final String PREFS_NAME = "SavingsPreferences";
    private static final String KEY_GOAL_PERIOD_MODE = "goalPeriodMode";
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_savings, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set window flags to extend layout under status bar for full screen shapes
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
        }
        
        // Setup ViewModel first to start data loading as early as possible
        savingsViewModel = new ViewModelProvider(requireActivity()).get(SavingsViewModel.class);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerview_savings_goals);
        circularProgressOverallSavings = view.findViewById(R.id.circular_progress_overall_savings);
        textOverallProgress = view.findViewById(R.id.text_overall_progress);
        textOverallPercentage = view.findViewById(R.id.text_overall_percentage);
        textActiveGoalsCount = view.findViewById(R.id.text_active_goals_count);
        textAchievedGoalsCount = view.findViewById(R.id.text_achieved_goals_count);
        textMonthlySavingsGoal = view.findViewById(R.id.text_monthly_savings_goal);
        textGoalPeriodLabel = view.findViewById(R.id.text_goal_period_label);
        textSavingsTip = view.findViewById(R.id.text_savings_tip);
        textSortGoals = view.findViewById(R.id.text_sort_goals);
        buttonRefreshTip = view.findViewById(R.id.button_refresh_tip);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        buttonCreateFirstGoal = view.findViewById(R.id.button_create_first_goal);
        confettiView = view.findViewById(R.id.confetti_view);
        animatedShapesBackground = view.findViewById(R.id.animated_shapes_background);
        
        // Initialize search components
        editSearch = view.findViewById(R.id.edit_search);
        buttonClearSearch = view.findViewById(R.id.button_clear_search);
        cardSearch = view.findViewById(R.id.card_search);
        
        // Get the card background and make it visible
        View cardSavingsOverview = view.findViewById(R.id.card_savings_overview);
        if (cardSavingsOverview != null) {
            cardSavingsOverview.setAlpha(1.0f);
        }
        
        // Initialize with default values to prevent flicker
        circularProgressOverallSavings.setProgress(0);
        textOverallProgress.setText("₱0 / ₱0");
        textOverallPercentage.setText("0%");
        textActiveGoalsCount.setText("0");
        textAchievedGoalsCount.setText("0");
        textMonthlySavingsGoal.setText("Set a goal");
        
        // Set default period mode to Monthly (2) and update label with secondary text color
        goalPeriodMode = 2;
        textGoalPeriodLabel.setText("Monthly");
        textGoalPeriodLabel.setTextColor(getResources().getColor(R.color.text_secondary));
        
        // Setup currency formatter
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fil", "PH"));
        
        // Setup RecyclerView and adapter
        adapter = new SavingsGoalAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        
        // Setup adapter click listener
        adapter.setOnAddFundsClickListener((goal, sourceView) -> showAddFundsDialog(goal));
        
        // Load savings data immediately
        loadSavingsData();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup search functionality
        setupSearchFunctionality();
        
        // Display initial random savings tip
        displayRandomSavingsTip();
        
        // Initialize tip rotation
        setupTipRotation();
        
        // Apply the default sort method
        applyCurrentSortMethod();
        
        // Setup scroll listener for FAB
        setupScrollListener();
        
        // Configure animated shapes background
        setupAnimatedBackground();
    }
    
    private void setupClickListeners() {
        // Setup tip refresh button
        buttonRefreshTip.setOnClickListener(v -> {
            Animation rotateAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate);
            v.startAnimation(rotateAnimation);
            
            Animation slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right);
            textSavingsTip.startAnimation(slideIn);
            
            displayRandomSavingsTip(true);
            startTipRotation();
        });
        
        // Setup sort button
        textSortGoals.setOnClickListener(v -> showSortOptionsDialog());
        
        // Setup monthly savings layout click
        View layoutMonthlySavings = requireView().findViewById(R.id.layout_monthly_savings);
        layoutMonthlySavings.setOnClickListener(v -> {
            // Cycle through periods: Monthly (2) -> Weekly (1) -> Daily (0)
            goalPeriodMode = (goalPeriodMode + 2) % 3; // This will cycle: 2->1->0->2
            savePreferences();
            updateOverallProgress();
        });
    }
    
    private void setupTipRotation() {
        tipRotationHandler = new Handler(Looper.getMainLooper());
        tipRotationRunnable = new Runnable() {
            @Override
            public void run() {
                displayRandomSavingsTip(true);
                tipRotationHandler.postDelayed(this, TIP_ROTATION_INTERVAL);
            }
        };
    }
    
    private void setupAnimatedBackground() {
        if (animatedShapesBackground != null) {
            animatedShapesBackground.configureSavingsTheme();
            animatedShapesBackground.setAlpha(0.9f);
            animatedShapesBackground.animate()
                .alpha(0.9f)
                .setDuration(1000)
                .start();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Start the auto-rotation of tips when the fragment is visible
        startTipRotation();
        
        // Ensure animated background is running and visible
        if (animatedShapesBackground != null) {
            animatedShapesBackground.setAlpha(0.0f);
            animatedShapesBackground.animate()
                .alpha(0.9f)  // Higher alpha for more visibility
                .setDuration(1000)
                .start();
        }
    }
    
    @Override
    public void onPause() {
        // Stop the auto-rotation of tips when the fragment is not visible
        stopTipRotation();
        
        // Fade out animated background
        if (animatedShapesBackground != null) {
            animatedShapesBackground.animate()
                .alpha(0.0f)
                .setDuration(500)
                .start();
        }
        
        super.onPause();
    }
    
    /**
     * Start the automatic rotation of savings tips
     */
    private void startTipRotation() {
        // Remove any existing callbacks to avoid duplicates
        stopTipRotation();
        // Start the rotation after the interval
        tipRotationHandler.postDelayed(tipRotationRunnable, TIP_ROTATION_INTERVAL);
    }
    
    /**
     * Stop the automatic rotation of savings tips
     */
    private void stopTipRotation() {
        if (tipRotationHandler != null && tipRotationRunnable != null) {
            tipRotationHandler.removeCallbacks(tipRotationRunnable);
        }
    }
    
    private void setupScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                // Remove the fab shrink/extend logic since the fab is gone
            }
        });
    }
    
    /**
     * Display a random savings tip with optional animation
     * 
     * @param animate Whether to animate the tip change
     */
    private void displayRandomSavingsTip(boolean animate) {
        // Select a new random tip (ensuring it's different from current one if possible)
        String currentTip = textSavingsTip.getText().toString();
        String newTip;
        int randomIndex;
        
        // If we have more than one tip, ensure we don't show the same tip twice in a row
        if (savingsTips.length > 1) {
            do {
                randomIndex = new Random().nextInt(savingsTips.length);
                newTip = savingsTips[randomIndex];
            } while (newTip.equals(currentTip));
        } else {
            // If we only have one tip, just show it
            randomIndex = 0;
            newTip = savingsTips[0];
        }
        
        // Store in final variable for use in inner class
        final String finalNewTip = newTip;
        
        // Apply animation if requested
        if (animate && textSavingsTip != null) {
            // Create fade out and fade in animations
            Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_out);
            Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in);
            
            fadeOut.setDuration(500); // 500ms fade out
            fadeIn.setDuration(500);  // 500ms fade in
            
            // Set animation listener to change the text when fade out completes
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                
                @Override
                public void onAnimationEnd(Animation animation) {
                    // Change text and start fade in
                    textSavingsTip.setText(finalNewTip);
                    textSavingsTip.startAnimation(fadeIn);
                }
                
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            
            // Start the fade out animation
            textSavingsTip.startAnimation(fadeOut);
        } else {
            // No animation, just update the text
            textSavingsTip.setText(newTip);
        }
    }
    
    /**
     * Display a random savings tip without animation (for backward compatibility)
     */
    private void displayRandomSavingsTip() {
        displayRandomSavingsTip(false);
    }
    
    private void loadSavingsData() {
        // Store the LiveData reference to manage observers
        allSavingsGoalsLiveData = savingsViewModel.getAllSavingsGoals();
        
        // Observe savings goals from the ViewModel
        allSavingsGoalsLiveData.observe(getViewLifecycleOwner(), savingsGoals -> {
            if (savingsGoals != null) {
                // Submit the list directly to avoid flickering
                adapter.submitList(savingsGoals);
                
                // Show/hide empty state based on data
                boolean isEmpty = savingsGoals.isEmpty();
                recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                
                // Show confetti animation if coming from empty state
                if (previouslyHadNoGoals && !isEmpty) {
                    showConfettiCelebration();
                }
                previouslyHadNoGoals = isEmpty;
                
                // Set appropriate height for RecyclerView to allow proper scrolling
                ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                if (params instanceof ViewGroup.MarginLayoutParams) {
                    params.height = savingsGoals.size() <= 2 ? 
                        ViewGroup.LayoutParams.WRAP_CONTENT : 
                        ViewGroup.LayoutParams.MATCH_PARENT;
                    recyclerView.setLayoutParams(params);
                }
                
                // Count active and achieved goals
                int activeGoals = 0;
                int achievedGoals = 0;
                
                for (SavingsGoal goal : savingsGoals) {
                    if (goal.getCurrentAmount() >= goal.getTargetAmount()) {
                        achievedGoals++;
                    } else {
                        activeGoals++;
                    }
                }
                
                textActiveGoalsCount.setText(String.valueOf(activeGoals));
                textAchievedGoalsCount.setText(String.valueOf(achievedGoals));
                
                // Trigger an immediate update of the overall progress
                updateOverallProgress();
            }
        });
        
        // Observe total savings and target for progress bar with immediate updates
        savingsViewModel.getTotalSavings().observe(getViewLifecycleOwner(), savings -> {
            updateOverallProgress();
            isInitialLoad = false;
        });
        
        savingsViewModel.getTotalSavingsTarget().observe(getViewLifecycleOwner(), target -> {
            updateOverallProgress();
            isInitialLoad = false;
        });
        
        // Restore saved preferences for period mode
        restorePreferences();
    }
    
    private void updateOverallProgress() {
        // Get the latest values
        double currentSavings = savingsViewModel.getTotalSavings().getValue() != null ? 
                savingsViewModel.getTotalSavings().getValue() : 0;
        double targetSavings = savingsViewModel.getTotalSavingsTarget().getValue() != null ? 
                savingsViewModel.getTotalSavingsTarget().getValue() : 0;
                
        // Calculate progress percentage
        int progress = 0;
        if (targetSavings > 0) {
            progress = (int) Math.min(100, (currentSavings / targetSavings) * 100);
        }
        
        // Update progress bar with single animation to avoid flickering
        circularProgressOverallSavings.setProgress(progress);
        
        // Update the percentage and progress text views
        textOverallPercentage.setText(String.format("%d%%", progress));
        textOverallProgress.setText(String.format("₱%s / ₱%s", 
            CircularProgressView.formatLargeNumber(currentSavings),
            CircularProgressView.formatLargeNumber(targetSavings)));

        // Get all savings goals to find the latest target date
        List<SavingsGoal> goals = savingsViewModel.getAllSavingsGoals().getValue();
        if (goals == null || goals.isEmpty()) {
            // No goals, clear all amounts
            textMonthlySavingsGoal.setText("Set a goal");
            textMonthlySavingsGoal.setTextColor(getResources().getColor(R.color.primary));
            return;
        }

        // Calculate total monthly amount needed across all active goals
        double totalMonthlyAmount = 0;
        boolean hasOverdueGoals = false;
        Date currentDate = new Date();

        for (SavingsGoal goal : goals) {
            if (!goal.isCompleted()) {
                // Get the goal's monthly amount using its own target date
                double remainingAmount = goal.getRemainingAmount();
                Date targetDate = goal.getTargetDate();
                
                if (targetDate != null) {
                    boolean isOverdue = TimeUtils.isDateInPast(targetDate);
                    if (isOverdue) {
                        hasOverdueGoals = true;
                    }
                    
                    // Calculate periods for this specific goal
                    double totalMonths = TimeUtils.getPeriodsRemaining(currentDate, targetDate, 2);
                    
                    // Calculate monthly amount for this goal
                    double monthlyAmount;
                    if (isOverdue) {
                        // If overdue, need to complete within a month
                        monthlyAmount = remainingAmount;
                    } else {
                        // Check if it's an exact number of months
                        boolean isExactMonths = Math.abs(Math.round(totalMonths) - totalMonths) < 0.1;
                        
                        if (isExactMonths) {
                            // If it's exactly X months, divide target amount equally
                            monthlyAmount = goal.getTargetAmount() / Math.round(totalMonths);
                        } else {
                            int fullMonths = (int) Math.ceil(totalMonths);
                            if (fullMonths <= 1) {
                                // If it's less than or exactly one month, show the full remaining amount
                                monthlyAmount = remainingAmount;
                            } else {
                                // For regular cases, calculate monthly amount
                                monthlyAmount = remainingAmount / totalMonths;
                            }
                        }
                    }
                    
                    // Add this goal's monthly amount to the total
                    totalMonthlyAmount += monthlyAmount;
                }
            }
        }

        // Update period label based on current mode
        String periodLabel;
        double displayAmount;
        switch (goalPeriodMode) {
            case 0: // Daily
                periodLabel = "Daily";
                displayAmount = totalMonthlyAmount / 30.0; // Convert monthly to daily
                break;
            case 1: // Weekly
                periodLabel = "Weekly";
                displayAmount = totalMonthlyAmount / 4.0; // Convert monthly to weekly
                break;
            case 2: // Monthly
            default:
                periodLabel = "Monthly";
                displayAmount = totalMonthlyAmount;
                break;
        }
        
        // Update the period label text
        textGoalPeriodLabel.setText(periodLabel);
        
        // Format and display the amounts
        textMonthlySavingsGoal.setText(String.format("₱%s", 
            CircularProgressView.formatLargeNumber(displayAmount)));
        
        // Log calculation details for debugging
        android.util.Log.d("SavingsFragment", String.format(
            "Overall Goal Calculation:\n" +
            "Current Savings: %.2f\n" +
            "Target Savings: %.2f\n" +
            "Progress: %d%%\n" +
            "Total Monthly Required: %.2f\n" +
            "Has Overdue Goals: %b",
            currentSavings,
            targetSavings,
            progress,
            totalMonthlyAmount,
            hasOverdueGoals
        ));
        
        // Show confetti if progress increased significantly
        if (Math.abs(progress - previousSavingsPercentage) >= 25 || 
            progress >= 100 && previousSavingsPercentage < 100) {
            showConfettiCelebration();
        }
        
        previousSavingsPercentage = progress;
        
        // Always ensure the card is visible
        View cardSavingsOverview = getView() != null ? getView().findViewById(R.id.card_savings_overview) : null;
        if (cardSavingsOverview != null) {
            cardSavingsOverview.setAlpha(1f);
        }
    }
    
    /**
     * Format currency values to be more compact for larger numbers
     */
    private String formatCurrency(double amount) {
        // Use the improved formatting method from CircularProgressView
        return "₱" + CircularProgressView.formatLargeNumber(amount);
    }
    
    private void showConfettiCelebration() {
        if (confettiView != null) {
            // First stop any ongoing animation
            confettiView.stop();
            // Make it visible if it isn't already
            confettiView.setVisibility(View.VISIBLE);
            // Use the start method with a parameter of 100 particles
            confettiView.start(100);
            
            // Log for debugging
            android.util.Log.d("SavingsFragment", "Showing confetti celebration");
        } else {
            android.util.Log.e("SavingsFragment", "confettiView is null");
        }
    }
    
    private void showSortOptionsDialog() {
        String[] sortOptions = {
            "Target Date (Closest First)",
            "Progress (Highest First)",
            "Amount (Highest First)",
            "Name (A-Z)",
            "Priority (High to Low)"
        };
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sort Savings Goals")
            .setItems(sortOptions, (dialog, which) -> {
                switch (which) {
                    case 0: // Target Date
                        sortSavingsGoalsByDate();
                        break;
                    case 1: // Progress
                        sortSavingsGoalsByProgress();
                        break;
                    case 2: // Amount
                        sortSavingsGoalsByAmount();
                        break;
                    case 3: // Name
                        sortSavingsGoalsByName();
                        break;
                    case 4: // Priority
                        sortSavingsGoalsByPriority();
                        break;
                }
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
            .show();
    }
    
    // Sort by target date (closest first)
    private void sortSavingsGoalsByDate() {
        List<SavingsGoal> currentList = new ArrayList<>(adapter.getCurrentList());
        
        currentList.sort((goal1, goal2) -> {
            // Completed goals go to the bottom
            if (goal1.isCompleted() && !goal2.isCompleted()) {
                return 1;
            } else if (!goal1.isCompleted() && goal2.isCompleted()) {
                return -1;
            }
            
            // Sort by target date
            return goal1.getTargetDate().compareTo(goal2.getTargetDate());
        });
        
        adapter.submitList(currentList);
        currentSortMethod = 0;
        textSortGoals.setText("Sort: Date");
    }
    
    // Sort by progress percentage (highest first)
    private void sortSavingsGoalsByProgress() {
        List<SavingsGoal> currentList = new ArrayList<>(adapter.getCurrentList());
        
        currentList.sort((goal1, goal2) -> {
            // Completed goals go to the top
            if (goal1.isCompleted() && !goal2.isCompleted()) {
                return -1;
            } else if (!goal1.isCompleted() && goal2.isCompleted()) {
                return 1;
            }
            
            // Sort by progress percentage (descending)
            return Double.compare(goal2.getProgressPercentage(), goal1.getProgressPercentage());
        });
        
        adapter.submitList(currentList);
        currentSortMethod = 1;
        textSortGoals.setText("Sort: Progress");
    }
    
    // Sort by target amount (highest first)
    private void sortSavingsGoalsByAmount() {
        List<SavingsGoal> currentList = new ArrayList<>(adapter.getCurrentList());
        
        currentList.sort((goal1, goal2) -> {
            // First sort by completion status
            if (goal1.isCompleted() && !goal2.isCompleted()) {
                return 1;
            } else if (!goal1.isCompleted() && goal2.isCompleted()) {
                return -1;
            }
            
            // Then sort by target amount (descending)
            return Double.compare(goal2.getTargetAmount(), goal1.getTargetAmount());
        });
        
        adapter.submitList(currentList);
        currentSortMethod = 2;
        textSortGoals.setText("Sort: Amount");
    }
    
    // Sort by name (alphabetically)
    private void sortSavingsGoalsByName() {
        List<SavingsGoal> currentList = new ArrayList<>(adapter.getCurrentList());
        
        currentList.sort((goal1, goal2) -> {
            // First sort by completion status
            if (goal1.isCompleted() && !goal2.isCompleted()) {
                return 1;
            } else if (!goal1.isCompleted() && goal2.isCompleted()) {
                return -1;
            }
            
            // Then sort by name
            return goal1.getName().compareToIgnoreCase(goal2.getName());
        });
        
        adapter.submitList(currentList);
        currentSortMethod = 3;
        textSortGoals.setText("Sort: Name");
    }
    
    // Sort by priority (high to low)
    private void sortSavingsGoalsByPriority() {
        List<SavingsGoal> currentList = new ArrayList<>(adapter.getCurrentList());
        
        currentList.sort((goal1, goal2) -> {
            // First sort by completion status
            if (goal1.isCompleted() && !goal2.isCompleted()) {
                return 1;
            } else if (!goal1.isCompleted() && goal2.isCompleted()) {
                return -1;
            }
            
            // Then sort by priority (HIGH first, then MEDIUM, then LOW)
            SavingsGoal.Priority p1 = goal1.getPriority();
            SavingsGoal.Priority p2 = goal2.getPriority();
            
            // Custom ordering for priority (HIGH = 0, MEDIUM = 1, LOW = 2)
            int p1Value = getPriorityValue(p1);
            int p2Value = getPriorityValue(p2);
            
            return Integer.compare(p1Value, p2Value);
        });
        
        adapter.submitList(currentList);
        currentSortMethod = 4;
        textSortGoals.setText("Sort: Priority");
    }
    
    // Helper method to get numerical value for priority for comparison
    private int getPriorityValue(SavingsGoal.Priority priority) {
        switch (priority) {
            case HIGH:
                return 0; // HIGH comes first
            case MEDIUM:
                return 1; // MEDIUM comes second
            case LOW:
                return 2; // LOW comes last
            default:
                return 3; // Any other value (shouldn't happen) comes after everything else
        }
    }
    
    /**
     * Show dialog to add a new savings goal
     */
    public void showAddSavingsGoalDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_savings_goal, null);
        
        // Get references to dialog views
        TextInputEditText editGoalName = dialogView.findViewById(R.id.edit_goal_name);
        TextInputEditText editGoalDescription = dialogView.findViewById(R.id.edit_goal_description);
        TextInputEditText editTargetAmount = dialogView.findViewById(R.id.edit_target_amount);
        TextInputEditText editInitialAmount = dialogView.findViewById(R.id.edit_initial_amount);
        TextView textTargetDate = dialogView.findViewById(R.id.text_target_date);
        RadioGroup radioGroupPriority = dialogView.findViewById(R.id.radio_group_priority);
        RadioButton radioLowPriority = dialogView.findViewById(R.id.radio_low_priority);
        RadioButton radioMediumPriority = dialogView.findViewById(R.id.radio_medium_priority);
        RadioButton radioHighPriority = dialogView.findViewById(R.id.radio_high_priority);
        AutoCompleteTextView dropdownCategory = dialogView.findViewById(R.id.dropdown_category);
        
        // Setup dropdown options for category
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_dropdown,
                savingsCategories
        );
        dropdownCategory.setAdapter(categoryAdapter);
        
        // Set up date picker
        Calendar calendar = Calendar.getInstance();
        selectedTargetDate = calendar.getTime(); // Initialize with current date
        textTargetDate.setText(dateFormat.format(selectedTargetDate));
        
        textTargetDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            if (selectedTargetDate != null) {
                c.setTime(selectedTargetDate);
            }
            
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, monthOfYear, dayOfMonth) -> {
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(year, monthOfYear, dayOfMonth);
                        selectedTargetDate = selectedCalendar.getTime();
                        textTargetDate.setText(dateFormat.format(selectedTargetDate));
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );
            
            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
        
        // Create the dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add New Savings Goal")
                .setView(dialogView)
                .setPositiveButton("Save", null); // Set null to avoid auto-dismiss
        
        // Create dialog with builder
        AlertDialog dialog = builder.create();
        
        // Set dialog to almost full width for better visibility of all fields
        dialog.show();
        
        // Apply rounded corners and set width
        if (dialog.getWindow() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
        }
        
        // Override the positive button click to validate input before dismissing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = editGoalName.getText().toString().trim();
            String description = editGoalDescription.getText().toString().trim();
            String targetAmountStr = editTargetAmount.getText().toString().trim();
            String initialAmountStr = editInitialAmount.getText().toString().trim();
            String category = dropdownCategory.getText().toString().trim();
            
            // Get selected priority
            SavingsGoal.Priority priority = SavingsGoal.Priority.MEDIUM; // Default medium
            int selectedPriorityId = radioGroupPriority.getCheckedRadioButtonId();
            
            if (selectedPriorityId == R.id.radio_low_priority) {
                priority = SavingsGoal.Priority.LOW;
            } else if (selectedPriorityId == R.id.radio_high_priority) {
                priority = SavingsGoal.Priority.HIGH;
            }
            
            // Append category to name if not "General Savings"
            if (!category.equals("General Savings") && !name.contains(category)) {
                description = (TextUtils.isEmpty(description) ? "" : description + "\n\n") + 
                              "Category: " + category;
            }
            
            // Validate input
            if (TextUtils.isEmpty(name)) {
                editGoalName.setError("Please enter a goal name");
                editGoalName.requestFocus();
                return;
            }
            
            if (TextUtils.isEmpty(targetAmountStr)) {
                editTargetAmount.setError("Please enter a target amount");
                editTargetAmount.requestFocus();
                return;
            }
            
            double targetAmount;
            double initialAmount = 0.0;
            
            try {
                targetAmount = Double.parseDouble(targetAmountStr);
                if (targetAmount <= 0) {
                    editTargetAmount.setError("Target amount must be greater than zero");
                    editTargetAmount.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                editTargetAmount.setError("Please enter a valid number");
                editTargetAmount.requestFocus();
                return;
            }
            
            if (!TextUtils.isEmpty(initialAmountStr)) {
                try {
                    initialAmount = Double.parseDouble(initialAmountStr);
                    if (initialAmount < 0) {
                        editInitialAmount.setError("Initial amount cannot be negative");
                        editInitialAmount.requestFocus();
                        return;
                    }
                    
                    if (initialAmount > targetAmount) {
                        editInitialAmount.setError("Initial amount cannot exceed target amount");
                        editInitialAmount.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    editInitialAmount.setError("Please enter a valid number");
                    editInitialAmount.requestFocus();
                    return;
                }
            }
            
            // Input is valid, create the savings goal
            savingsViewModel.createSavingsGoal(
                    name,
                    description,
                    targetAmount,
                    initialAmount,
                    selectedTargetDate,
                    priority
            );
            
            // If there's an initial amount, show confetti celebration
            if (initialAmount > 0) {
                showConfettiCelebration();
                ToastUtils.showSuccess(requireContext(), "Savings goal created successfully");
            } else {
                ToastUtils.showSuccess(requireContext(), "Savings goal created");
            }
            
            // Dismiss the dialog
            dialog.dismiss();
        });
    }
    
    /**
     * Show dialog to add funds to a savings goal
     */
    private void showAddFundsDialog(SavingsGoal goal) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_funds, null);
        
        TextView textGoalName = dialogView.findViewById(R.id.text_goal_name);
        TextView textCurrentAmount = dialogView.findViewById(R.id.text_current_amount);
        TextView textTargetAmount = dialogView.findViewById(R.id.text_target_amount);
        TextView textProgressPercentage = dialogView.findViewById(R.id.text_progress_percentage);
        EditText editAmount = dialogView.findViewById(R.id.edit_amount);
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_goal);
        
        // Set up quick amount chips
        com.google.android.material.chip.Chip chip100 = dialogView.findViewById(R.id.chip_amount_100);
        com.google.android.material.chip.Chip chip500 = dialogView.findViewById(R.id.chip_amount_500);
        com.google.android.material.chip.Chip chip1000 = dialogView.findViewById(R.id.chip_amount_1000);
        
        // Calculate progress percentage
        double progressPercentage = goal.getProgressPercentage();
        
        // Populate views
        textGoalName.setText(goal.getName());
        textCurrentAmount.setText("Current: " + formatCurrency(goal.getCurrentAmount()));
        textTargetAmount.setText("Target: " + formatCurrency(goal.getTargetAmount()));
        textProgressPercentage.setText(String.format("%.0f%%", progressPercentage));
        progressBar.setProgress((int) progressPercentage);
        
        // Set up quick amount button listeners
        chip100.setOnClickListener(v -> editAmount.setText("100"));
        chip500.setOnClickListener(v -> editAmount.setText("500"));
        chip1000.setOnClickListener(v -> editAmount.setText("1000"));
        
        // Create dialog builder with rounded corners theme
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(dialogView)
               .setPositiveButton("Add", (dialogInterface, i) -> {
                    String amountStr = editAmount.getText().toString().trim();
                    
                    // Validate input
                    if (amountStr.isEmpty()) {
                        ToastUtils.showWarning(requireContext(), "Please enter an amount");
                        return;
                    }
                    
                    try {
                        double amount = Double.parseDouble(amountStr);
                        
                        if (amount <= 0) {
                            ToastUtils.showWarning(requireContext(), "Amount must be greater than 0");
                            return;
                        }
                        
                        // Create a copy with updated amount for immediate UI feedback
                        SavingsGoal updatedGoal = goal.copy();
                        double newAmount = updatedGoal.getCurrentAmount() + amount;
                        updatedGoal.setCurrentAmount(newAmount);
                        
                        // Update the adapter directly for immediate feedback
                        adapter.updateGoal(updatedGoal);
                        
                        // Show toast message
                        if (newAmount >= goal.getTargetAmount() && goal.getCurrentAmount() < goal.getTargetAmount()) {
                            // Goal reached with this contribution!
                            ToastUtils.showSuccess(requireContext(), "🎉 Goal reached! Congratulations!");
                        } else {
                            ToastUtils.showSuccess(requireContext(), "Added " + formatCurrency(amount) + " to " + goal.getName());
                        }
                        
                        // Update the goal in ViewModel
                        savingsViewModel.addFundsToGoal(goal, amount);
                        
                        // Try to show confetti celebration if we're in an activity
                        try {
                            Activity activity = getActivity();
                            if (activity != null) {
                                ConfettiView confettiView = activity.findViewById(R.id.confetti_view);
                                if (confettiView != null) {
                                    confettiView.stop();
                                    confettiView.setVisibility(View.VISIBLE);
                                    confettiView.start(100);
                                }
                            }
                        } catch (Exception e) {
                            android.util.Log.e("SavingsFragment", "Could not show confetti: " + e.getMessage());
                        }
                    } catch (NumberFormatException e) {
                        ToastUtils.showError(requireContext(), "Please enter a valid amount");
                    }
                })
                .setNegativeButton("Cancel", null);
        
        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // IMPORTANT: Apply rounded corners to dialog window - do this AFTER dialog.show()
        if (dialog.getWindow() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
        }
    }
    
    /**
     * Show dialog with goal details and options
     */
    private void showGoalDetailsDialog(SavingsGoal goal) {
        String[] options;
        
        if (goal.isCompleted()) {
            options = new String[]{"View Details", "Edit Goal", "Delete Goal"};
        } else {
            options = new String[]{"View Details", "Add Funds", "Withdraw Funds", "Edit Goal", "Mark as Completed", "Delete Goal"};
        }
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(goal.getName())
                .setItems(options, (dialogInterface, which) -> {
                    switch (which) {
                        case 0:  // View Details
                            showGoalDetailsView(goal);
                            break;
                        case 1:  // Add Funds / Edit
                            if (goal.isCompleted()) {
                                // Edit goal
                                // TODO: Implement edit goal functionality
                                break;
                            }
                            // Use the same Add Funds dialog as the button
                            showAddFundsDialog(goal);
                            break;
                        case 2:  // Withdraw Funds / Delete
                            if (goal.isCompleted()) {
                                confirmDeleteGoal(goal);
                                break;
                            }
                            showWithdrawFundsDialog(goal);
                            break;
                        case 3:  // Edit Goal
                            // TODO: Implement edit goal functionality
                            break;
                        case 4:  // Mark as Completed
                            savingsViewModel.markGoalCompleted(goal);
                            ToastUtils.showSuccess(requireContext(), "Goal marked as completed");
                            break;
                        case 5:  // Delete Goal
                            confirmDeleteGoal(goal);
                            break;
                    }
                })
                .create();
                
        dialog.show();
        
        // Apply rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
        }
    }
    
    /**
     * Show detailed view of a savings goal
     */
    private void showGoalDetailsView(SavingsGoal goal) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_goal_details, null);
        
        TextView textName = dialogView.findViewById(R.id.text_goal_name);
        TextView textDescription = dialogView.findViewById(R.id.text_goal_description);
        TextView textAmount = dialogView.findViewById(R.id.text_goal_amount);
        TextView textProgress = dialogView.findViewById(R.id.text_goal_progress);
        TextView textTargetDate = dialogView.findViewById(R.id.text_target_date);
        TextView textCreatedDate = dialogView.findViewById(R.id.text_created_date);
        TextView textStatus = dialogView.findViewById(R.id.text_goal_status);
        TextView textMonthlyGoal = dialogView.findViewById(R.id.text_monthly_goal);
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_goal);
        
        textName.setText(goal.getName());
        
        if (goal.getDescription() != null && !goal.getDescription().isEmpty()) {
            textDescription.setText(goal.getDescription());
            textDescription.setVisibility(View.VISIBLE);
        } else {
            textDescription.setVisibility(View.GONE);
        }
        
        textAmount.setText(String.format("%s / %s", 
                currencyFormatter.format(goal.getCurrentAmount()),
                currencyFormatter.format(goal.getTargetAmount())));
        
        double progress = goal.getProgressPercentage();
        progressBar.setProgress((int) progress);
        textProgress.setText(String.format("%.1f%%", progress));
        
        // Add debug info to dates
        Date now = new Date();
        Date targetDate = goal.getTargetDate();
        Date createdDate = goal.getCreatedDate();
        
        // Calculate time difference for debugging
        long diffInMillis = targetDate.getTime() - now.getTime();
        double diffInDays = diffInMillis / (1000.0 * 60 * 60 * 24);
        double diffInMonths = diffInDays / 30.4375;
        
        textTargetDate.setText(String.format("Target Date: %s\nTime left: %.1f days (%.1f months)", 
                dateFormat.format(targetDate), diffInDays, diffInMonths));
                
        textCreatedDate.setText(String.format("Created: %s", dateFormat.format(createdDate)));
        
        if (goal.isCompleted()) {
            textStatus.setText("Status: Completed");
            textStatus.setTextColor(getResources().getColor(R.color.income));
            textMonthlyGoal.setVisibility(View.GONE);
        } else {
            textStatus.setText("Status: In Progress");
            textStatus.setTextColor(getResources().getColor(R.color.primary));
            
            double monthlyGoal = goal.getMonthlyGoal();
            // Add debug info to monthly goal
            textMonthlyGoal.setText(String.format("Suggested Monthly Saving: %s\nDebug: Amount=%s, Months=%.1f", 
                    currencyFormatter.format(monthlyGoal),
                    currencyFormatter.format(goal.getRemainingAmount()),
                    diffInMonths));
            textMonthlyGoal.setVisibility(View.VISIBLE);
        }
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .create();
                
        dialog.show();
        
        // Apply rounded corners and set width
        if (dialog.getWindow() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
        }
    }
    
    /**
     * Show dialog to withdraw funds from a savings goal
     */
    private void showWithdrawFundsDialog(SavingsGoal goal) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_withdraw_funds, null);
        
        TextView textGoalName = dialogView.findViewById(R.id.text_goal_name);
        TextView textCurrentAmount = dialogView.findViewById(R.id.text_current_amount);
        EditText editAmount = dialogView.findViewById(R.id.edit_amount);
        
        textGoalName.setText(goal.getName());
        textCurrentAmount.setText(String.format("Available: %s", currencyFormatter.format(goal.getCurrentAmount())));

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Withdraw Funds")
                .setView(dialogView)
                .setPositiveButton("Withdraw", (dialogInterface, i) -> {
                    String amountStr = editAmount.getText().toString().trim();
                    
                    // Validate input
                    if (amountStr.isEmpty()) {
                        ToastUtils.showWarning(requireContext(), "Amount is required");
                        return;
                    }
                    
                    try {
                        double amount = Double.parseDouble(amountStr);
                        
                        if (amount <= 0) {
                            ToastUtils.showWarning(requireContext(), "Amount must be greater than 0");
                            return;
                        }
                        
                        if (amount > goal.getCurrentAmount()) {
                            ToastUtils.showWarning(requireContext(), "Amount cannot exceed available funds");
                            return;
                        }
                        
                        // Create a copy of the goal with updated amount for immediate UI update
                        SavingsGoal updatedGoal = goal.copy();
                        double newAmount = Math.max(0, updatedGoal.getCurrentAmount() - amount);
                        updatedGoal.setCurrentAmount(newAmount);
                        
                        // Update the adapter directly for immediate feedback
                        adapter.updateGoal(updatedGoal);
                        
                        // Show toast message
                        ToastUtils.showInfo(requireContext(), "Withdrew " + formatCurrency(amount) + " from " + goal.getName());
                        
                        // Update the database (async)
                        savingsViewModel.withdrawFundsFromGoal(goal, amount);
                    } catch (NumberFormatException e) {
                        ToastUtils.showError(requireContext(), "Please enter a valid amount");
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        
        dialog.show();
        
        // Apply rounded corners and set width
        if (dialog.getWindow() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
        }
    }
    
    /**
     * Confirm deletion of a savings goal
     */
    private void confirmDeleteGoal(SavingsGoal goal) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete \"" + goal.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialogInterface, which) -> {
                    savingsViewModel.deleteSavingsGoal(goal);
                    ToastUtils.showInfo(requireContext(), "Goal deleted");
                })
                .setNegativeButton("Cancel", null)
                .create();
                
        dialog.show();
        
        // Apply rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
        }
    }
    
    private void applyCurrentSortMethod() {
        // Get the current list
        List<SavingsGoal> currentList = new ArrayList<>(adapter.getCurrentList());
        
        // Update the sort button text
        switch (currentSortMethod) {
            case 0:
                textSortGoals.setText("Sort: Date");
                break;
            case 1:
                textSortGoals.setText("Sort: Progress");
                break;
            case 2:
                textSortGoals.setText("Sort: Amount");
                break;
            case 3:
                textSortGoals.setText("Sort: Name");
                break;
            case 4:
                textSortGoals.setText("Sort: Priority");
                break;
            default:
                textSortGoals.setText("Sort");
                break;
        }
        
        // Apply the current sort method
        applySortMethod(currentList, currentSortMethod);
    }
    
    private void applySortMethod(List<SavingsGoal> list, int method) {
        switch (method) {
            case 0:
                // Sort by date
                sortSavingsGoalsByDate(list);
                break;
            case 1:
                // Sort by progress
                sortSavingsGoalsByProgress(list);
                break;
            case 2:
                // Sort by amount
                sortSavingsGoalsByAmount(list);
                break;
            case 3:
                // Sort by name
                sortSavingsGoalsByName(list);
                break;
            case 4:
                // Sort by priority
                sortSavingsGoalsByPriority(list);
                break;
        }
    }
    
    private void sortSavingsGoalsByDate(List<SavingsGoal> list) {
        // Sort by target date (ascending)
        Collections.sort(list, (goal1, goal2) -> {
            // Handle null dates and completed goals
                    if (goal1.isCompleted() && !goal2.isCompleted()) {
                return 1; // Completed goals come last
                    } else if (!goal1.isCompleted() && goal2.isCompleted()) {
                return -1; // Incomplete goals come first
            } else if (goal1.getTargetDate() == null && goal2.getTargetDate() == null) {
                return 0;
            } else if (goal1.getTargetDate() == null) {
                return 1;
            } else if (goal2.getTargetDate() == null) {
                        return -1;
                    }
            return goal1.getTargetDate().compareTo(goal2.getTargetDate());
        });
        
        // Update adapter with sorted list
        adapter.submitList(new ArrayList<>(list));
    }
    
    private void sortSavingsGoalsByProgress(List<SavingsGoal> list) {
        // Sort by progress percentage (descending)
        Collections.sort(list, (goal1, goal2) -> {
            // Handle potential division by zero
            double progress1 = goal1.getTargetAmount() == 0 ? 0 : 
                    goal1.getCurrentAmount() / goal1.getTargetAmount();
            double progress2 = goal2.getTargetAmount() == 0 ? 0 : 
                    goal2.getCurrentAmount() / goal2.getTargetAmount();
            // For descending order, compare in reverse
            return Double.compare(progress2, progress1);
        });
        
        // Update adapter with sorted list
        adapter.submitList(new ArrayList<>(list));
    }
    
    private void sortSavingsGoalsByAmount(List<SavingsGoal> list) {
        // Sort by remaining amount (ascending)
        Collections.sort(list, (goal1, goal2) -> {
            double remaining1 = goal1.getTargetAmount() - goal1.getCurrentAmount();
            double remaining2 = goal2.getTargetAmount() - goal2.getCurrentAmount();
            return Double.compare(remaining1, remaining2);
        });
        
        // Update adapter with sorted list
        adapter.submitList(new ArrayList<>(list));
    }
    
    private void sortSavingsGoalsByName(List<SavingsGoal> list) {
        // Sort by name (alphabetically)
        Collections.sort(list, (goal1, goal2) -> {
            // Handle null names and case-insensitive comparison
            String name1 = goal1.getName() != null ? goal1.getName().toLowerCase() : "";
            String name2 = goal2.getName() != null ? goal2.getName().toLowerCase() : "";
            return name1.compareTo(name2);
                });
        
        // Update adapter with sorted list
        adapter.submitList(new ArrayList<>(list));
    }
    
    private void sortSavingsGoalsByPriority(List<SavingsGoal> list) {
        // Sort by priority (high to low) and then by target date
        Collections.sort(list, (goal1, goal2) -> {
            // First compare by priority (high first)
            int priorityComparison = Integer.compare(
                    getPriorityValue(goal2.getPriority()), 
                    getPriorityValue(goal1.getPriority()));
                    
            if (priorityComparison != 0) {
                return priorityComparison;
            }
            
            // For equal priorities, sort by target date
            if (goal1.getTargetDate() == null && goal2.getTargetDate() == null) {
                return 0;
            } else if (goal1.getTargetDate() == null) {
                return 1;
            } else if (goal2.getTargetDate() == null) {
                return -1;
            }
            
            return goal1.getTargetDate().compareTo(goal2.getTargetDate());
        });
        
        // Update adapter with sorted list
        adapter.submitList(new ArrayList<>(list));
    }
    
    private void restorePreferences() {
        try {
            // Get shared preferences
            android.content.SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
            
            // Restore goal period mode (default to monthly if not found)
            goalPeriodMode = sharedPreferences.getInt(KEY_GOAL_PERIOD_MODE, 2);
            
            android.util.Log.d("SavingsFragment", "Restored goal period mode: " + goalPeriodMode);
            
            // If we have an adapter, make sure all goal cards use the same period mode
            if (adapter != null) {
                adapter.setGlobalPeriodMode(goalPeriodMode);
            }
        } catch (Exception e) {
            android.util.Log.e("SavingsFragment", "Error restoring preferences: " + e.getMessage());
            // Default to monthly view if there's an error
            goalPeriodMode = 2;
        }
    }
    
    private void savePreferences() {
        try {
            // Get shared preferences editor
            android.content.SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
            
            // Save goal period mode
            editor.putInt(KEY_GOAL_PERIOD_MODE, goalPeriodMode);
            
            // Apply changes
            editor.apply();
            
            // Also update the adapter to keep everything in sync
            if (adapter != null) {
                adapter.setGlobalPeriodMode(goalPeriodMode);
            }
            
            android.util.Log.d("SavingsFragment", "Saved goal period mode: " + goalPeriodMode);
        } catch (Exception e) {
            android.util.Log.e("SavingsFragment", "Error saving preferences: " + e.getMessage());
        }
    }
    
    /**
     * Set up search functionality for savings goals
     */
    private void setupSearchFunctionality() {
        // Clear button functionality
        buttonClearSearch.setOnClickListener(v -> {
            editSearch.setText("");
            buttonClearSearch.setVisibility(View.GONE);
            // Clear search and show all goals
            clearSearch();
            
            // Hide keyboard only when explicitly clearing search
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                    requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
            
            // Remove focus after clearing
            editSearch.clearFocus();
        });
        
        // Text change listener for search field
        editSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show or hide clear button based on text
                buttonClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                
                // Perform search if text length is at least 1 character
                if (s.length() > 0) {
                    performSearch(s.toString());
                } else {
                    clearSearch();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Not needed
            }
        });

        // Set focus change listener to show/hide keyboard appropriately
        editSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Show keyboard when search gets focus
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                        requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                
                // Change search bar appearance for active state
                cardSearch.setCardBackgroundColor(getResources().getColor(R.color.white));
                cardSearch.setStrokeWidth(1);
                cardSearch.setStrokeColor(getResources().getColor(R.color.primary_light));
                cardSearch.setCardElevation(4);
            } else {
                // Return to normal appearance
                cardSearch.setCardBackgroundColor(getResources().getColor(R.color.search_background));
                cardSearch.setStrokeWidth(0);
                cardSearch.setCardElevation(2);
            }
        });

        // Set action listener for IME search button
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                // Don't hide keyboard or clear focus when pressing search
                // Instead, just perform the search which will keep focus
                if (editSearch.getText().length() > 0) {
                    performSearch(editSearch.getText().toString());
                }
                return true;
            }
            return false;
        });
        
        // Prevent touch events on the recycler view from stealing focus
        recyclerView.setOnTouchListener((v, event) -> {
            if (isSearchActive && editSearch.hasFocus()) {
                // Keep focus on search bar during active search
                v.performClick();
                return true;
            }
            return false;
        });
    }
    
    /**
     * Perform search for savings goals
     * 
     * @param query The search query string
     */
    private void performSearch(String query) {
        isSearchActive = true;
        
        // Don't animate recyclerView when typing to avoid focus loss
        if (query.length() > 2) {
            // Only animate for longer queries to avoid disruption during typing
            recyclerView.setAlpha(0.7f);
            recyclerView.animate()
                .alpha(1.0f)
                .setDuration(300)
                .start();
        }
        
        // Remove previous search results observer if exists
        if (searchResultsLiveData != null) {
            searchResultsLiveData.removeObservers(getViewLifecycleOwner());
        }
        
        // Get new search results
        searchResultsLiveData = savingsViewModel.searchSavingsGoals(query);
        
        // Observe the search results
        searchResultsLiveData.observe(getViewLifecycleOwner(), searchResults -> {
            // Apply current sort method to ensure consistent view
            List<SavingsGoal> sortedResults = new ArrayList<>(searchResults);
            applySortMethod(sortedResults, currentSortMethod);
            adapter.submitList(sortedResults);
            
            boolean isEmpty = searchResults.isEmpty();
            if (isEmpty) {
                // Match same animation style for consistent transitions
                recyclerView.setVisibility(View.GONE);
                
                // Position and show empty state, keeping animation consistent
                layoutEmptyState.setAlpha(0f);
                layoutEmptyState.setVisibility(View.VISIBLE);
                layoutEmptyState.animate()
                    .alpha(1.0f)
                    .setDuration(250)
                    .start();
                
                // Change empty state text for search
                updateEmptyStateForSearch(query);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
            }
            
            // Ensure search edit text keeps focus
            if (editSearch != null) {
                editSearch.requestFocus();
            }
        });
    }
    
    /**
     * Clear search and restore original goal list
     */
    private void clearSearch() {
        // Remove search results observer
        if (searchResultsLiveData != null) {
            searchResultsLiveData.removeObservers(getViewLifecycleOwner());
        }
        
        isSearchActive = false;
        
        // Animate transition back to all goals
        recyclerView.setAlpha(0.7f);
        recyclerView.animate()
            .alpha(1.0f)
            .setDuration(300)
            .start();
        
        // Restore original data observation
        allSavingsGoalsLiveData = savingsViewModel.getAllSavingsGoals();
        allSavingsGoalsLiveData.observe(getViewLifecycleOwner(), savingsGoals -> {
            if (savingsGoals != null) {
                // Apply current sort method
                List<SavingsGoal> sortedList = new ArrayList<>(savingsGoals);
                applySortMethod(sortedList, currentSortMethod);
                adapter.submitList(sortedList);
                
                boolean isEmpty = savingsGoals.isEmpty();
                if (isEmpty) {
                    recyclerView.setVisibility(View.GONE);
                    
                    // Animate empty state
                    layoutEmptyState.setAlpha(0f);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    layoutEmptyState.animate()
                        .alpha(1.0f)
                        .setDuration(250)
                        .start();
                    
                    // Restore original empty state text
                    resetEmptyState();
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);
                }
            }
        });
    }
    
    /**
     * Update empty state UI for search
     */
    private void updateEmptyStateForSearch(String query) {
        // Find TextViews in the empty state layout
        TextView emptyTitle = layoutEmptyState.findViewById(R.id.text_empty_title);
        TextView emptyMessage = layoutEmptyState.findViewById(R.id.text_empty_message);
        ImageView emptyImage = layoutEmptyState.findViewById(R.id.image_empty_state);
        
        if (emptyTitle != null && emptyMessage != null) {
            emptyTitle.setText("No matching savings goals");
            emptyMessage.setText("Try searching with different keywords or check your spelling for \"" + query + "\"");
            
            // Adjust image alpha to indicate search state
            if (emptyImage != null) {
                emptyImage.setAlpha(0.7f);
                // Keep the same size for consistent placement
                emptyImage.getLayoutParams().width = (int) (getResources().getDisplayMetrics().density * 120);
                emptyImage.getLayoutParams().height = (int) (getResources().getDisplayMetrics().density * 120);
                emptyImage.requestLayout();
            }
        }
        
        // Hide the create button since we're using FAB
        buttonCreateFirstGoal.setVisibility(View.GONE);

        // Keep same margin for consistent placement
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layoutEmptyState.getLayoutParams();
        params.topMargin = (int) (getResources().getDisplayMetrics().density * 36);
        layoutEmptyState.setLayoutParams(params);
    }
    
    /**
     * Reset empty state UI to original
     */
    private void resetEmptyState() {
        // Find TextViews in the empty state layout
        TextView emptyTitle = layoutEmptyState.findViewById(R.id.text_empty_title);
        TextView emptyMessage = layoutEmptyState.findViewById(R.id.text_empty_message);
        ImageView emptyImage = layoutEmptyState.findViewById(R.id.image_empty_state);
        
        if (emptyTitle != null && emptyMessage != null) {
            emptyTitle.setText("No savings goals yet");
            emptyMessage.setText("Create your first savings goal to start tracking your progress");
            
            // Reset image alpha
            if (emptyImage != null) {
                emptyImage.setAlpha(0.5f);
                // Ensure original size
                emptyImage.getLayoutParams().width = (int) (getResources().getDisplayMetrics().density * 120);
                emptyImage.getLayoutParams().height = (int) (getResources().getDisplayMetrics().density * 120);
                emptyImage.requestLayout();
            }
        }
        
        // Hide the create button since we're using FAB
        buttonCreateFirstGoal.setVisibility(View.GONE);
        
        // Reset to original margin
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layoutEmptyState.getLayoutParams();
        params.topMargin = (int) (getResources().getDisplayMetrics().density * 36);
        layoutEmptyState.setLayoutParams(params);
    }
} 
