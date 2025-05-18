package com.example.personifi;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.personifi.utils.ToastUtils;

/**
 * Activity for managing savings goals.
 */
public class SavingsActivity extends AppCompatActivity {

    private SavingsViewModel savingsViewModel;
    private SavingsGoalAdapter allGoalsAdapter;
    private SavingsGoalAdapter upcomingGoalsAdapter;
    private RecyclerView recyclerViewAllGoals;
    private RecyclerView recyclerViewUpcomingGoals;
    private TextView textTotalSavingsAmount;
    private TextView textOverallProgress;
    private TextView textCompletedCount;
    private View emptyView;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormat;
    private ConfettiView confettiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_savings);
        
        // Enable verbose logging
        android.util.Log.d("SavingsActivity", "Activity onCreate started");
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Initialize views
        recyclerViewAllGoals = findViewById(R.id.recyclerview_savings_goals);
        recyclerViewUpcomingGoals = findViewById(R.id.recyclerview_upcoming_goals);
        textTotalSavingsAmount = findViewById(R.id.text_total_savings_amount);
        textOverallProgress = findViewById(R.id.text_overall_progress);
        textCompletedCount = findViewById(R.id.text_completed_count);
        emptyView = findViewById(R.id.empty_view);
        FloatingActionButton fabAddSavingsGoal = findViewById(R.id.fab_add_savings_goal);
        confettiView = findViewById(R.id.confetti_view);
        
        // Setup formatters
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fil", "PH"));
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        
        // Setup view model
        savingsViewModel = new ViewModelProvider(this).get(SavingsViewModel.class);
        
        // Setup adapters
        allGoalsAdapter = new SavingsGoalAdapter();
        upcomingGoalsAdapter = new SavingsGoalAdapter();
        
        // Setup recycler views
        recyclerViewAllGoals.setAdapter(allGoalsAdapter);
        recyclerViewAllGoals.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAllGoals.setItemAnimator(new DefaultItemAnimator());
        
        // Use horizontal layout for upcoming goals
        LinearLayoutManager horizontalLayoutManager = 
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewUpcomingGoals.setAdapter(upcomingGoalsAdapter);
        recyclerViewUpcomingGoals.setLayoutManager(horizontalLayoutManager);
        recyclerViewUpcomingGoals.setItemAnimator(new DefaultItemAnimator());
        
        // Setup click listeners for both adapters
        allGoalsAdapter.setOnAddFundsClickListener((goal, sourceView) -> {
            showAddFundsDialog(goal);
        });
        
        upcomingGoalsAdapter.setOnAddFundsClickListener((goal, sourceView) -> {
            showAddFundsDialog(goal);
        });
        
        // Observe data changes and update UI
        savingsViewModel.getAllSavingsGoals().observe(this, this::updateUI);
        
        // Observe upcoming savings goals
        savingsViewModel.getUpcomingSavingsGoals().observe(this, upcomingGoals -> {
            if (upcomingGoals != null) {
                upcomingGoalsAdapter.submitList(new ArrayList<>(upcomingGoals));
            }
        });
        
        // Observe total savings and target amounts for progress bar
        savingsViewModel.getTotalSavings().observe(this, this::updateProgressBar);
        savingsViewModel.getTotalSavingsTarget().observe(this, target -> updateProgressBar(null));
        
        // Set click listeners
        fabAddSavingsGoal.setOnClickListener(v -> showAddSavingsGoalDialog());
    }
    
    /**
     * Update UI based on savings goals list
     */
    private void updateUI(List<SavingsGoal> savingsGoals) {
        if (savingsGoals == null || savingsGoals.isEmpty()) {
            recyclerViewAllGoals.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerViewAllGoals.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            
            // Submit list directly without creating a new copy to avoid flickering
            allGoalsAdapter.submitList(savingsGoals);
            
            // Update completed count
            long completedCount = savingsGoals.stream().filter(SavingsGoal::isCompleted).count();
            textCompletedCount.setText(String.format("%d Completed", completedCount));
        }
    }
    
    /**
     * Update overall progress bar based on savings goals
     */
    private void updateOverallProgress() {
        Double totalSavings = savingsViewModel.getTotalSavings().getValue();
        Double totalTarget = savingsViewModel.getTotalSavingsTarget().getValue();
        
        if (totalSavings == null) totalSavings = 0.0;
        if (totalTarget == null || totalTarget == 0) totalTarget = 1.0;
        
        double progress = Math.min(100, (totalSavings / totalTarget) * 100);
        
        // Set progress bar
        ((ProgressBar) findViewById(R.id.progress_overall_savings)).setProgress((int) progress);
        
        // Set progress text
        textOverallProgress.setText(String.format("%s / %s (%.1f%%)", 
                currencyFormatter.format(totalSavings),
                currencyFormatter.format(totalTarget),
                progress));
    }
    
    /**
     * Update progress bar with animation to prevent flickering
     */
    private void updateProgressBar(Double totalSavings) {
        if (totalSavings != null) {
            textTotalSavingsAmount.setText(currencyFormatter.format(totalSavings));
        } else {
            Double currentTotal = savingsViewModel.getTotalSavings().getValue();
            if (currentTotal != null) {
                textTotalSavingsAmount.setText(currencyFormatter.format(currentTotal));
            } else {
                textTotalSavingsAmount.setText(currencyFormatter.format(0));
            }
        }
            
        // Use a slight delay to ensure smooth UI updates
        new Handler(Looper.getMainLooper()).post(this::updateOverallProgress);
    }
    
    /**
     * Show dialog to add a new savings goal
     */
    private void showAddSavingsGoalDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_savings_goal, null);
        
        EditText editName = dialogView.findViewById(R.id.edit_goal_name);
        EditText editDescription = dialogView.findViewById(R.id.edit_goal_description);
        EditText editTargetAmount = dialogView.findViewById(R.id.edit_target_amount);
        EditText editInitialAmount = dialogView.findViewById(R.id.edit_initial_amount);
        TextView textTargetDate = dialogView.findViewById(R.id.text_target_date);
        
        // Default target date to 3 months from now
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 3);
        Date targetDate = calendar.getTime();
        textTargetDate.setText(dateFormat.format(targetDate));
        
        // Setup date picker
        textTargetDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Target Date")
                    .setSelection(targetDate.getTime())
                    .build();
            
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Date selectedDate = new Date(selection);
                textTargetDate.setText(dateFormat.format(selectedDate));
                textTargetDate.setTag(selectedDate);  // Store date object for later retrieval
            });
            
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
        textTargetDate.setTag(targetDate);  // Store initial date
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Add New Savings Goal")
                .setView(dialogView)
                .setPositiveButton("Add", (dialogInterface, i) -> {
                    // Get input values
                    String name = editName.getText().toString().trim();
                    String description = editDescription.getText().toString().trim();
                    String targetAmountStr = editTargetAmount.getText().toString().trim();
                    String initialAmountStr = editInitialAmount.getText().toString().trim();
                    Date date = (Date) textTargetDate.getTag();
                    
                    // Check if required fields are not empty
                    if (name.isEmpty() || targetAmountStr.isEmpty()) {
                        ToastUtils.showWarning(this, "Name and target amount are required");
                        return;
                    }
                    
                    try {
                        double targetAmount = Double.parseDouble(targetAmountStr);
                        double initialAmount = initialAmountStr.isEmpty() ? 0 : Double.parseDouble(initialAmountStr);
                        
                        // Validate amounts
                        if (targetAmount <= 0) {
                            ToastUtils.showWarning(this, "Target amount must be greater than 0");
                            return;
                        }
                        
                        if (initialAmount < 0 || initialAmount > targetAmount) {
                            ToastUtils.showWarning(this, "Initial amount must be between 0 and target amount");
                            return;
                        }
                        
                        // Create savings goal
                        savingsViewModel.createSavingsGoal(name, description, targetAmount, initialAmount, 
                                date, SavingsGoal.Priority.MEDIUM);
                        
                        // Show success message
                        ToastUtils.showSuccess(this, "Savings goal created");
                    } catch (NumberFormatException e) {
                        ToastUtils.showError(this, "Please enter valid amounts");
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        
        dialog.show();
        
        // IMPORTANT: Apply rounded corners to dialog window - do this AFTER dialog.show()
        if (dialog.getWindow() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
        }
    }
    
    /**
     * Show confetti animation to celebrate adding funds
     */
    private void showConfettiCelebration() {
        if (confettiView != null) {
            // First stop any ongoing animation
            confettiView.stop();
            // Make it visible if it isn't already
            confettiView.setVisibility(View.VISIBLE);
            // Use the start method with a parameter of 100 particles
            confettiView.start(100);
            
            // Log for debugging
            android.util.Log.d("SavingsActivity", "Showing confetti celebration");
        } else {
            android.util.Log.e("SavingsActivity", "confettiView is null");
        }
    }
    
    /**
     * Show dialog to add funds to a savings goal
     */
    private void showAddFundsDialog(SavingsGoal goal) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_funds, null);
        
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
        textCurrentAmount.setText("Current: " + currencyFormatter.format(goal.getCurrentAmount()));
        textTargetAmount.setText("Target: " + currencyFormatter.format(goal.getTargetAmount()));
        if (textProgressPercentage != null) {
            textProgressPercentage.setText(String.format("%.0f%%", progressPercentage));
            textProgressPercentage.setVisibility(View.VISIBLE);
            
            // Make it bolder and larger to ensure visibility
            textProgressPercentage.setTextSize(18);
            textProgressPercentage.setTextColor(getResources().getColor(R.color.income));
            textProgressPercentage.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        progressBar.setProgress((int) progressPercentage);
        
        // Debug logging
        android.util.Log.d("SavingsActivity", "Progress Percentage: " + progressPercentage);
        android.util.Log.d("SavingsActivity", "Progress Text: " + textProgressPercentage.getText());
        android.util.Log.d("SavingsActivity", "Progress Visibility: " + (textProgressPercentage.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
        
        // Set up quick amount button listeners
        chip100.setOnClickListener(v -> editAmount.setText("100"));
        chip500.setOnClickListener(v -> editAmount.setText("500"));
        chip1000.setOnClickListener(v -> editAmount.setText("1000"));
        
        // Create dialog builder with rounded corners theme - match exactly with SavingsFragment
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView)
               .setPositiveButton("Add", (dialogInterface, i) -> {
                    String amountStr = editAmount.getText().toString().trim();
                    
                    // Validate input
                    if (amountStr.isEmpty()) {
                        ToastUtils.showWarning(this, "Amount is required");
                        return;
                    }
                    
                    try {
                        double amount = Double.parseDouble(amountStr);
                        
                        if (amount <= 0) {
                            ToastUtils.showWarning(this, "Amount must be greater than 0");
                            return;
                        }
                        
                        // Clone the goal to update locally for immediate UI feedback
                        SavingsGoal updatedGoal = goal.copy();
                        double newAmount = updatedGoal.getCurrentAmount() + amount;
                        updatedGoal.setCurrentAmount(newAmount);
                        
                        // Check if goal is now completed
                        if (newAmount >= updatedGoal.getTargetAmount() && !updatedGoal.isCompleted()) {
                            updatedGoal.setCompleted(true);
                        }
                        
                        // Update UI immediately
                        allGoalsAdapter.updateGoal(updatedGoal);
                        
                        // Also update in the upcoming goals adapter if present
                        boolean foundInUpcoming = false;
                        for (int j = 0; j < upcomingGoalsAdapter.getCurrentList().size(); j++) {
                            if (upcomingGoalsAdapter.getCurrentList().get(j).getId() == goal.getId()) {
                                upcomingGoalsAdapter.updateGoal(updatedGoal);
                                foundInUpcoming = true;
                                break;
                            }
                        }
                        
                        // Force progress bar update
                        double totalSaved = 0;
                        for (SavingsGoal g : allGoalsAdapter.getCurrentList()) {
                            if (g.getId() == updatedGoal.getId()) {
                                totalSaved += updatedGoal.getCurrentAmount();
                            } else {
                                totalSaved += g.getCurrentAmount();
                            }
                        }
                        
                        // Update the total savings amount text
                        textTotalSavingsAmount.setText(currencyFormatter.format(totalSaved));
                        
                        // Recalculate progress percentage
                        Double totalTarget = savingsViewModel.getTotalSavingsTarget().getValue();
                        if (totalTarget != null && totalTarget > 0) {
                            double progress = Math.min(100, (totalSaved / totalTarget) * 100);
                            ProgressBar overallProgressBar = findViewById(R.id.progress_overall_savings);
                            if (overallProgressBar != null) {
                                overallProgressBar.setProgress((int) progress);
                            }
                            textOverallProgress.setText(String.format("%s / %s (%.1f%%)", 
                                currencyFormatter.format(totalSaved),
                                currencyFormatter.format(totalTarget),
                                progress));
                        }
                        
                        // Update in the database (async)
                        savingsViewModel.addFundsToGoal(goal, amount);
                        
                        // Show confetti celebration
                        new Handler(Looper.getMainLooper()).postDelayed(this::showConfettiCelebration, 300);
                        
                    } catch (NumberFormatException e) {
                        ToastUtils.showError(this, "Please enter a valid amount");
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
            options = new String[]{"View Details", "Delete Goal"};
        } else {
            options = new String[]{"View Details", "Add Funds", "Withdraw Funds", "Mark as Completed", "Delete Goal"};
        }
        
        new MaterialAlertDialogBuilder(this)
                .setTitle(goal.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:  // View Details
                            showGoalDetailsView(goal);
                            break;
                        case 1:  // Add Funds or Delete Goal (if completed)
                            if (goal.isCompleted()) {
                                // Delete goal
                                confirmDeleteGoal(goal);
                            } else {
                                // Add funds
                                showAddFundsDialog(goal);
                            }
                            break;
                        case 2:  // Withdraw Funds
                            showWithdrawFundsDialog(goal);
                            break;
                        case 3:  // Mark as Completed
                            savingsViewModel.markGoalCompleted(goal);
                            ToastUtils.showSuccess(this, "Goal marked as completed");
                            break;
                        case 4:  // Delete Goal
                            confirmDeleteGoal(goal);
                            break;
                    }
                })
                .show();
    }
    
    /**
     * Show detailed view of a savings goal
     */
    private void showGoalDetailsView(SavingsGoal goal) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_goal_details, null);
        
        TextView textName = dialogView.findViewById(R.id.text_goal_name);
        TextView textDescription = dialogView.findViewById(R.id.text_goal_description);
        TextView textAmount = dialogView.findViewById(R.id.text_goal_amount);
        TextView textProgress = dialogView.findViewById(R.id.text_goal_progress);
        TextView textTargetDate = dialogView.findViewById(R.id.text_target_date);
        TextView textCreatedDate = dialogView.findViewById(R.id.text_created_date);
        TextView textStatus = dialogView.findViewById(R.id.text_goal_status);
        TextView textMonthlyGoal = dialogView.findViewById(R.id.text_monthly_goal);
        
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
        textProgress.setText(String.format("%.1f%%", progress));
        
        textTargetDate.setText(String.format("Target Date: %s", dateFormat.format(goal.getTargetDate())));
        textCreatedDate.setText(String.format("Created: %s", dateFormat.format(goal.getCreatedDate())));
        
        if (goal.isCompleted()) {
            textStatus.setText("Status: Completed");
            textStatus.setTextColor(getResources().getColor(R.color.income));
            textMonthlyGoal.setVisibility(View.GONE);
        } else {
            textStatus.setText("Status: In Progress");
            textStatus.setTextColor(getResources().getColor(R.color.primary));
            
            double monthlyGoal = goal.getMonthlyGoal();
            textMonthlyGoal.setText(String.format("Suggested Monthly Saving: %s", 
                    currencyFormatter.format(monthlyGoal)));
            textMonthlyGoal.setVisibility(View.VISIBLE);
        }
        
        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
    
    /**
     * Show dialog to withdraw funds from a savings goal
     */
    private void showWithdrawFundsDialog(SavingsGoal goal) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_withdraw_funds, null);
        
        TextView textGoalName = dialogView.findViewById(R.id.text_goal_name);
        TextView textCurrentAmount = dialogView.findViewById(R.id.text_current_amount);
        EditText editAmount = dialogView.findViewById(R.id.edit_amount);
        
        textGoalName.setText(goal.getName());
        textCurrentAmount.setText(String.format("Available: %s", currencyFormatter.format(goal.getCurrentAmount())));
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Withdraw Funds")
                .setView(dialogView)
                .setPositiveButton("Withdraw", (dialogInterface, i) -> {
                    String amountStr = editAmount.getText().toString().trim();
                    
                    // Validate input
                    if (amountStr.isEmpty()) {
                        ToastUtils.showWarning(this, "Amount is required");
                        return;
                    }
                    
                    try {
                        double amount = Double.parseDouble(amountStr);
                        
                        if (amount <= 0) {
                            ToastUtils.showWarning(this, "Amount must be greater than 0");
                            return;
                        }
                        
                        if (amount > goal.getCurrentAmount()) {
                            ToastUtils.showWarning(this, "Amount cannot exceed available funds");
                            return;
                        }
                        
                        // Create a copy of the goal with updated amount for immediate UI update
                        SavingsGoal updatedGoal = goal.copy();
                        double newAmount = Math.max(0, updatedGoal.getCurrentAmount() - amount);
                        updatedGoal.setCurrentAmount(newAmount);
                        
                        // Update the UI immediately using the adapter
                        allGoalsAdapter.updateGoal(updatedGoal);
                        
                        // If this goal is also in the upcoming goals adapter, update it there too
                        for (int j = 0; j < upcomingGoalsAdapter.getCurrentList().size(); j++) {
                            if (upcomingGoalsAdapter.getCurrentList().get(j).getId() == goal.getId()) {
                                upcomingGoalsAdapter.updateGoal(updatedGoal);
                                break;
                            }
                        }
                        
                        // Force update progress
                        updateOverallProgress();
                        
                        // Update in database (async operation)
                        savingsViewModel.withdrawFundsFromGoal(goal, amount);
                        
                    } catch (NumberFormatException e) {
                        ToastUtils.showError(this, "Please enter a valid amount");
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        
        dialog.show();
        
        // IMPORTANT: Apply rounded corners to dialog window - do this AFTER dialog.show()
        if (dialog.getWindow() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
        }
    }
    
    /**
     * Confirm goal deletion
     */
    private void confirmDeleteGoal(SavingsGoal goal) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Savings Goal")
                .setMessage("Are you sure you want to delete this savings goal? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    savingsViewModel.deleteSavingsGoal(goal);
                    ToastUtils.showSuccess(this, "Savings goal deleted");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
} 