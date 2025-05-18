package com.example.personifi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import android.animation.ObjectAnimator;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;
import android.app.Activity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import com.example.personifi.SavingsViewModel;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import java.util.ArrayList;
import com.example.personifi.utils.ToastUtils;
import com.example.personifi.CircularProgressView;
import com.example.personifi.TimeConstants;

/**
 * Adapter for displaying savings goals in a RecyclerView.
 */
public class SavingsGoalAdapter extends ListAdapter<SavingsGoal, SavingsGoalAdapter.SavingsGoalViewHolder> {

    private final NumberFormat currencyFormatter;
    private final SimpleDateFormat dateFormat;
    private OnAddFundsClickListener addFundsListener;
    
    // Diff callback for efficient list updates
    private static final DiffUtil.ItemCallback<SavingsGoal> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<SavingsGoal>() {
        @Override
        public boolean areItemsTheSame(@NonNull SavingsGoal oldItem, @NonNull SavingsGoal newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SavingsGoal oldItem, @NonNull SavingsGoal newItem) {
            // Check if relevant fields are the same
            boolean sameContent = Double.compare(oldItem.getCurrentAmount(), newItem.getCurrentAmount()) == 0 &&
                   Double.compare(oldItem.getTargetAmount(), newItem.getTargetAmount()) == 0 &&
                   oldItem.getName().equals(newItem.getName()) &&
                   oldItem.isCompleted() == newItem.isCompleted() &&
                   oldItem.getTargetDate().equals(newItem.getTargetDate());
            
            return sameContent;
        }
        
        @Override
        public Object getChangePayload(@NonNull SavingsGoal oldItem, @NonNull SavingsGoal newItem) {
            // Only update the specific parts that changed to avoid complete rebind
            Bundle payload = new Bundle();
            
            if (Double.compare(oldItem.getCurrentAmount(), newItem.getCurrentAmount()) != 0) {
                payload.putDouble("currentAmount", newItem.getCurrentAmount());
            }
            
            if (Double.compare(oldItem.getTargetAmount(), newItem.getTargetAmount()) != 0) {
                payload.putDouble("targetAmount", newItem.getTargetAmount());
            }
            
            if (oldItem.isCompleted() != newItem.isCompleted()) {
                payload.putBoolean("completed", newItem.isCompleted());
            }
            
            if (payload.size() == 0) {
                return null;
            }
            
            return payload;
        }
    };
    
    public interface OnAddFundsClickListener {
        void onAddFundsClick(SavingsGoal savingsGoal, View sourceView);
    }

    // Constructor
    public SavingsGoalAdapter() {
        super(DIFF_CALLBACK);
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fil", "PH"));
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }
    
    public void setOnAddFundsClickListener(OnAddFundsClickListener listener) {
        this.addFundsListener = listener;
    }

    @NonNull
    @Override
    public SavingsGoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_savings_goal, parent, false);
        return new SavingsGoalViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SavingsGoalViewHolder holder, int position) {
        SavingsGoal currentGoal = getItem(position);
        if (currentGoal != null) {
            // Set the period mode to the global default
            holder.goalPeriodMode = defaultPeriodMode;
            // Then bind the data
            holder.bind(currentGoal);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SavingsGoalViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            // No payload, do a full rebind
            onBindViewHolder(holder, position);
        } else {
            // Handle partial updates with payload
            Bundle payload = (Bundle) payloads.get(0);
            SavingsGoal currentGoal = getItem(position);
            
            // Only update specific parts that changed
            if (currentGoal != null) {
                holder.applyPayload(currentGoal, payload);
            }
        }
    }

    // ViewHolder class
    class SavingsGoalViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView textGoalName;
        private final TextView textGoalCurrentAmount;
        private final TextView textGoalTargetAmount;
        private final TextView textGoalProgressAboveBar;
        private final TextView textTargetDate;
        private final TextView textMonthlyRecommended;
        private final ProgressBar progressBar;
        private final MaterialButton buttonAddFunds;
        private final MaterialButton buttonViewDetails;
        private final ImageView imageAchieved;
        private final ImageView imageMoreDetails;
        private final Chip chipGoalCategory;
        private final View priorityIndicator;
        private final View progressIndicatorDot;
        private ObjectAnimator progressDotAnimator;
        
        // Track individual goal period display settings
        protected int goalPeriodMode = 2; // 0: Daily, 1: Weekly, 2: Monthly (default)
        
        // Constants for SharedPreferences
        private static final String PREFS_NAME = "SavingsGoalPreferences";
        private static final String KEY_GOAL_PERIOD_PREFIX = "goalPeriod_";

        public SavingsGoalViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_savings_goal);
            textGoalName = itemView.findViewById(R.id.text_goal_name);
            textGoalCurrentAmount = itemView.findViewById(R.id.text_goal_current_amount);
            textGoalTargetAmount = itemView.findViewById(R.id.text_goal_target_amount);
            textGoalProgressAboveBar = itemView.findViewById(R.id.text_goal_progress_above_bar);
            textTargetDate = itemView.findViewById(R.id.text_target_date);
            textMonthlyRecommended = itemView.findViewById(R.id.text_monthly_recommended);
            progressBar = itemView.findViewById(R.id.progress_goal);
            buttonAddFunds = itemView.findViewById(R.id.button_add_funds);
            buttonViewDetails = itemView.findViewById(R.id.button_view_details);
            imageAchieved = itemView.findViewById(R.id.image_achieved);
            imageMoreDetails = itemView.findViewById(R.id.image_more_details);
            chipGoalCategory = itemView.findViewById(R.id.chip_goal_category);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            progressIndicatorDot = itemView.findViewById(R.id.progress_indicator_dot);
            
            // Make the card non-clickable
            if (cardView != null) {
                cardView.setClickable(false);
                cardView.setFocusable(false);
            }
            
            // Add click listener to monthly recommended text to cycle between time periods
            if (textMonthlyRecommended != null) {
                textMonthlyRecommended.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // Cycle through daily, weekly, and monthly
                        goalPeriodMode = (goalPeriodMode + 1) % 3;
                        // Re-bind with the new period mode
                        SavingsGoal goal = getItem(position);
                        updateMonthlyGoalDisplay(goal);
                        
                        // Save the period mode for this goal
                        savePeriodMode(goal.getId());
                    }
                });
            }
            
            // Set click listener for the add funds button
            if (buttonAddFunds != null) {
                buttonAddFunds.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // Get the goal at this position
                        final SavingsGoal goal = getItem(position);
                        
                        // Create the dialog directly
                        Context context = v.getContext();
                        android.util.Log.d("SavingsGoal", "Creating add funds dialog for: " + goal.getName());
                        
                        // Inflate custom dialog view
                        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_funds, null);
                        
                        TextView textGoalName = dialogView.findViewById(R.id.text_goal_name);
                        TextView textCurrentAmount = dialogView.findViewById(R.id.text_current_amount);
                        TextView textTargetAmount = dialogView.findViewById(R.id.text_target_amount);
                        TextView textProgressPercentage = dialogView.findViewById(R.id.text_progress_percentage);
                        ProgressBar progressBar = dialogView.findViewById(R.id.progress_goal);
                        EditText editAmount = dialogView.findViewById(R.id.edit_amount);
                        
                        // Calculate the progress percentage
                        double progressPercentage = goal.getProgressPercentage();
                        
                        // Set text values
                        textGoalName.setText(goal.getName());
                        textCurrentAmount.setText(String.format("Current: %s", currencyFormatter.format(goal.getCurrentAmount())));
                        textTargetAmount.setText(String.format("Target: %s", currencyFormatter.format(goal.getTargetAmount())));
                        textProgressPercentage.setText(String.format("%.0f%%", progressPercentage));
                        progressBar.setProgress((int) progressPercentage);
                        
                        // Update both progress text views
                        String progressText = String.format(Locale.getDefault(), "%d%%", (int) progressPercentage);
                        textGoalProgressAboveBar.setText(progressText);
                        
                        // Set up quick amount chips
                        com.google.android.material.chip.Chip chip20 = dialogView.findViewById(R.id.chip_amount_20);
                        com.google.android.material.chip.Chip chip50 = dialogView.findViewById(R.id.chip_amount_50);
                        com.google.android.material.chip.Chip chip100 = dialogView.findViewById(R.id.chip_amount_100);
                        com.google.android.material.chip.Chip chip500 = dialogView.findViewById(R.id.chip_amount_500);
                        com.google.android.material.chip.Chip chip1000 = dialogView.findViewById(R.id.chip_amount_1000);
                        
                        // Set up quick amount button listeners
                        if (chip20 != null) chip20.setOnClickListener(view -> editAmount.setText("20"));
                        if (chip50 != null) chip50.setOnClickListener(view -> editAmount.setText("50"));
                        if (chip100 != null) chip100.setOnClickListener(view -> editAmount.setText("100"));
                        if (chip500 != null) chip500.setOnClickListener(view -> editAmount.setText("500"));
                        if (chip1000 != null) chip1000.setOnClickListener(view -> editAmount.setText("1000"));
                        
                        // Build and show dialog
                        AlertDialog dialog = new AlertDialog.Builder(context)
                            .setView(dialogView)
                            .setPositiveButton("Add", (dialogInterface, which) -> {
                                android.util.Log.d("SavingsGoal", "Add button clicked");
                                
                                // Get amount value
                                String amountStr = editAmount.getText().toString().trim();
                                if (amountStr.isEmpty()) {
                                    ToastUtils.showWarning(context, "Please enter an amount");
                                    return;
                                }
                                
                                try {
                                    double amount = Double.parseDouble(amountStr);
                                    if (amount <= 0) {
                                        ToastUtils.showWarning(context, "Amount must be positive");
                                        return;
                                    }
                                    
                                    // Get ViewModel and update via proper channels
                                    if (context instanceof ViewModelStoreOwner) {
                                        ViewModelStoreOwner viewModelStoreOwner = (ViewModelStoreOwner) context;
                                        SavingsViewModel viewModel = new ViewModelProvider(viewModelStoreOwner).get(SavingsViewModel.class);
                                        
                                        // Create a local copy with updated amount for immediate UI feedback
                                        SavingsGoal updatedGoal = goal.copy();
                                        double newAmount = updatedGoal.getCurrentAmount() + amount;
                                        updatedGoal.setCurrentAmount(newAmount);
                                        
                                        // Check if goal is now completed
                                        if (newAmount >= updatedGoal.getTargetAmount() && !updatedGoal.isCompleted()) {
                                            updatedGoal.setCompleted(true);
                                        }
                                        
                                        // Update UI immediately using our own method
                                        updateGoal(updatedGoal);
                                        
                                        // Then update in the database (async)
                                        viewModel.addFundsToGoal(goal, amount);
                                        
                                        android.util.Log.d("SavingsGoal", "Added " + amount + " to goal via ViewModel");
                                    }
                                    
                                    // Try to find and trigger confetti celebration in the parent activity
                                    if (context instanceof Activity) {
                                        Activity activity = (Activity) context;
                                        View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
                                        ConfettiView confettiView = rootView.findViewById(R.id.confetti_view);
                                        
                                        if (confettiView != null) {
                                            android.util.Log.d("SavingsGoalAdapter", "Found confetti view, celebrating");
                                            // First stop any ongoing animation
                                            confettiView.stop();
                                            // Make it visible if it isn't already
                                            confettiView.setVisibility(View.VISIBLE);
                                            // Use the start method with a parameter of 100 particles
                                            confettiView.start(100);
                                        } else {
                                            android.util.Log.e("SavingsGoalAdapter", "Confetti view not found");
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    ToastUtils.showError(context, "Invalid amount");
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create();
                            
                        dialog.show();
                        
                        // Apply rounded corners to dialog window - do this AFTER dialog.show()
                        if (dialog.getWindow() != null) {
                            int width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.9);
                            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
                        }
                    }
                });
            }
            
            // Set click listener for the view details button
            if (buttonViewDetails != null) {
                buttonViewDetails.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // Show details dialog directly from the adapter
                        showDetailsDialog(getItem(position), v);
                    }
                });
            }

            // Set up ripple effect for the more details icon
            if (imageMoreDetails != null) {
                // Create a ripple drawable programmatically
                android.content.res.ColorStateList rippleColor = 
                    android.content.res.ColorStateList.valueOf(
                        itemView.getContext().getResources().getColor(R.color.ripple_color));
                
                android.graphics.drawable.RippleDrawable ripple = 
                    new android.graphics.drawable.RippleDrawable(
                        rippleColor,
                        null,
                        new android.graphics.drawable.ShapeDrawable(new android.graphics.drawable.shapes.OvalShape()));
                
                // Set the background with ripple effect
                imageMoreDetails.setBackground(ripple);
                
                // Make it clickable and focusable
                imageMoreDetails.setClickable(true);
                imageMoreDetails.setFocusable(true);
                
                // Add padding for better touch target
                int padding = (int) (itemView.getContext().getResources().getDisplayMetrics().density * 8);
                imageMoreDetails.setPadding(padding, padding, padding, padding);
                
                // Set click listener
                imageMoreDetails.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        SavingsGoal goal = getItem(position);
                        showOptionsMenu(goal, v);
                    }
                });
            }
        }
        
        // Show details dialog for the savings goal
        private void showDetailsDialog(SavingsGoal savingsGoal, View sourceView) {
            Context context = sourceView.getContext();
            
            // Create dialog view
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_goal_details, null);
            
            // Find views in the dialog
            TextView textName = dialogView.findViewById(R.id.text_goal_name);
            TextView textDescription = dialogView.findViewById(R.id.text_goal_description);
            TextView textAmount = dialogView.findViewById(R.id.text_goal_amount);
            TextView textProgress = dialogView.findViewById(R.id.text_goal_progress);
            TextView textTargetDate = dialogView.findViewById(R.id.text_target_date);
            TextView textCreatedDate = dialogView.findViewById(R.id.text_created_date);
            TextView textStatus = dialogView.findViewById(R.id.text_goal_status);
            TextView textMonthlyGoal = dialogView.findViewById(R.id.text_monthly_goal);
            ProgressBar progressBar = dialogView.findViewById(R.id.progress_goal);
            
            // Set text values
            textName.setText(savingsGoal.getName());
            
            if (savingsGoal.getDescription() != null && !savingsGoal.getDescription().isEmpty()) {
                textDescription.setText(savingsGoal.getDescription());
                textDescription.setVisibility(View.VISIBLE);
            } else {
                textDescription.setVisibility(View.GONE);
            }
            
            textAmount.setText(String.format("%s / %s", 
                    currencyFormatter.format(savingsGoal.getCurrentAmount()),
                    currencyFormatter.format(savingsGoal.getTargetAmount())));
            
            double progress = savingsGoal.getProgressPercentage();
            progressBar.setProgress((int) progress);
            textProgress.setText(String.format("%.1f%%", progress));
            
            textTargetDate.setText(String.format("Target Date: %s", dateFormat.format(savingsGoal.getTargetDate())));
            textCreatedDate.setText(String.format("Created: %s", dateFormat.format(savingsGoal.getCreatedDate())));
            
            if (savingsGoal.isCompleted()) {
                textStatus.setText("Status: Completed");
                textStatus.setTextColor(ContextCompat.getColor(context, R.color.income));
                textMonthlyGoal.setVisibility(View.GONE);
            } else {
                textStatus.setText("Status: In Progress");
                textStatus.setTextColor(ContextCompat.getColor(context, R.color.primary));
                
                double monthlyGoal = savingsGoal.getMonthlyGoal();
                textMonthlyGoal.setText(String.format("Suggested Monthly Saving: %s", 
                        currencyFormatter.format(monthlyGoal)));
                textMonthlyGoal.setVisibility(View.VISIBLE);
            }
            
            // Create and show dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogView)
                   .setPositiveButton("Close", null);
                   
            AlertDialog dialog = builder.create();
            dialog.show();
            
            // Apply rounded corners to dialog window
            if (dialog.getWindow() != null) {
                int width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.9);
                dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
            }
        }
        
        // Bind data to the views
        public void bind(SavingsGoal savingsGoal) {
            if (savingsGoal == null) {
                return;
            }
            
            // Set goal name
            if (textGoalName != null) {
                textGoalName.setText(savingsGoal.getName());
            }
            
            // Get priority color first since we'll use it for multiple elements
            int priorityColor;
            Context context = itemView.getContext();
            switch (savingsGoal.getPriority()) {
                case HIGH:
                    priorityColor = context.getResources().getColor(R.color.high_priority);
                    break;
                case MEDIUM:
                    priorityColor = context.getResources().getColor(R.color.primary);
                    break;
                default:
                    priorityColor = context.getResources().getColor(R.color.low_priority);
                    break;
            }
            
            // Set priority indicator color
            if (priorityIndicator != null) {
                priorityIndicator.setBackgroundColor(priorityColor);
            }
            
            // Set category chip with matching priority color
            if (chipGoalCategory != null) {
                String category = extractCategory(savingsGoal.getDescription());
                if (category != null && !category.isEmpty()) {
                    chipGoalCategory.setText(category);
                } else {
                    chipGoalCategory.setText("General");
                }
                chipGoalCategory.setVisibility(View.VISIBLE);
                
                // Set chip colors to match priority
                chipGoalCategory.setTextColor(priorityColor);
                chipGoalCategory.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.argb(30, 
                        android.graphics.Color.red(priorityColor),
                        android.graphics.Color.green(priorityColor),
                        android.graphics.Color.blue(priorityColor)
                    )
                ));
                chipGoalCategory.setChipStrokeColor(android.content.res.ColorStateList.valueOf(priorityColor));
            }
            
            // Set current and target amounts with compact formatting
            if (textGoalCurrentAmount != null) {
                textGoalCurrentAmount.setText(formatCurrency(savingsGoal.getCurrentAmount()));
            }
            
            if (textGoalTargetAmount != null) {
                textGoalTargetAmount.setText(formatCurrency(savingsGoal.getTargetAmount()));
            }
            
            // Format progress percentage - ensure it doesn't exceed 100%
            double progress = Math.min(100, savingsGoal.getProgressPercentage());
            if (textGoalProgressAboveBar != null) {
                String progressText = String.format(Locale.getDefault(), "%d%%", (int) progress);
                textGoalProgressAboveBar.setText(progressText);
            }
            
            // Set progress bar
            if (progressBar != null) {
                progressBar.setProgress((int) progress);
                
                // Set up animated progress dot for active goals
                if (progressIndicatorDot != null) {
                    if (!savingsGoal.isCompleted() && progress > 0 && progress < 100) {
                        progressIndicatorDot.setVisibility(View.VISIBLE);
                        
                        // Calculate dot position based on progress
                        float maxWidth = progressBar.getWidth() - progressIndicatorDot.getWidth();
                        float translationX = (maxWidth * (float)progress) / 100f;
                        
                        // Check if we have a valid width before animating
                        if (maxWidth > 0) {
                            // Animate the dot slightly to draw attention
                            if (progressDotAnimator != null) {
                                progressDotAnimator.cancel();
                            }
                            progressDotAnimator = ObjectAnimator.ofFloat(progressIndicatorDot, 
                                "translationX", translationX);
                            progressDotAnimator.setDuration(300);
                            progressDotAnimator.start();
                        } else {
                            // Position without animation if sizes aren't ready
                            progressIndicatorDot.post(() -> {
                                float updatedMaxWidth = progressBar.getWidth() - progressIndicatorDot.getWidth();
                                float updatedTranslationX = (updatedMaxWidth * (float)progress) / 100f;
                                progressIndicatorDot.setTranslationX(updatedTranslationX);
                            });
                        }
                    } else {
                        progressIndicatorDot.setVisibility(View.GONE);
                    }
                }
            }
            
            // Set target date
            if (textTargetDate != null && savingsGoal.getTargetDate() != null) {
                textTargetDate.setText(dateFormat.format(savingsGoal.getTargetDate()));
            }
            
            // Set monthly recommended with compact formatting
            // Handle negative values appropriately
            if (textMonthlyRecommended != null) {
                updateMonthlyGoalDisplay(savingsGoal);
            }
            
            // Show/hide achievement icon
            if (imageAchieved != null) {
                if (savingsGoal.isCompleted()) {
                    imageAchieved.setVisibility(View.VISIBLE);
                    // Add sparkle animation
                    Animation sparkleAnimation = AnimationUtils.loadAnimation(
                        itemView.getContext(), R.anim.sparkle);
                    imageAchieved.startAnimation(sparkleAnimation);
                    
                    if (cardView != null) {
                        cardView.setStrokeColor(itemView.getContext().getResources().getColor(R.color.income));
                        cardView.setStrokeWidth(2);
                    }
                    
                    if (buttonAddFunds != null) {
                        buttonAddFunds.setVisibility(View.GONE);
                    }
                } else {
                    imageAchieved.setVisibility(View.GONE);
                    imageAchieved.clearAnimation();
                    
                    if (cardView != null) {
                        cardView.setStrokeWidth(0);
                    }
                    
                    if (buttonAddFunds != null) {
                        buttonAddFunds.setVisibility(View.VISIBLE);
                    }
                }
            }
            
            // Set card background color based on priority
            if (cardView != null) {
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.card_background));
            }
        }
        
        // Handle partial updates without full rebind for better performance
        public void applyPayload(SavingsGoal savingsGoal, Bundle payload) {
            if (payload.containsKey("currentAmount")) {
                double currentAmount = payload.getDouble("currentAmount");
                if (textGoalCurrentAmount != null) {
                    textGoalCurrentAmount.setText(formatCurrency(currentAmount));
                }
                
                // Update progress-related fields if we have the target amount
                if (progressBar != null && savingsGoal != null) {
                    double targetAmount = savingsGoal.getTargetAmount();
                    // Ensure progress doesn't exceed 100%
                    int progress = (int) Math.min(100, (currentAmount / targetAmount) * 100);
                    progressBar.setProgress(progress);
                    
                    if (textGoalProgressAboveBar != null) {
                        String progressText = String.format(Locale.getDefault(), "%d%%", progress);
                        textGoalProgressAboveBar.setText(progressText);
                    }
                    
                    // Update progress dot position if needed
                    if (progressIndicatorDot != null && progress > 0 && progress < 100) {
                        progressIndicatorDot.setVisibility(View.VISIBLE);
                        
                        // Calculate dot position based on progress
                        float maxWidth = progressBar.getWidth() - progressIndicatorDot.getWidth();
                        float translationX = (progress / 100f) * maxWidth;
                        
                        // Animate dot to new position
                        if (progressDotAnimator != null) {
                            progressDotAnimator.cancel();
                        }
                        progressDotAnimator = ObjectAnimator.ofFloat(progressIndicatorDot, 
                                "translationX", translationX);
                        progressDotAnimator.setDuration(300);
                        progressDotAnimator.start();
                    } else {
                        if (progressIndicatorDot != null) {
                            progressIndicatorDot.setVisibility(View.GONE);
                        }
                    }
                    
                    // Also update monthly goal text since it might have changed
                    if (textMonthlyRecommended != null) {
                        updateMonthlyGoalDisplay(savingsGoal);
                    }
                }
            }
            
            if (payload.containsKey("targetAmount") && textGoalTargetAmount != null) {
                textGoalTargetAmount.setText(formatCurrency(payload.getDouble("targetAmount")));
            }
            
            if (payload.containsKey("completed")) {
                boolean completed = payload.getBoolean("completed");
                
                if (imageAchieved != null) {
                    if (completed) {
                        imageAchieved.setVisibility(View.VISIBLE);
                        // Add sparkle animation
                        Animation sparkleAnimation = AnimationUtils.loadAnimation(
                            itemView.getContext(), R.anim.sparkle);
                        imageAchieved.startAnimation(sparkleAnimation);
                        
                        if (cardView != null) {
                            cardView.setStrokeColor(itemView.getContext().getResources().getColor(R.color.income));
                            cardView.setStrokeWidth(2);
                        }
                        
                        if (buttonAddFunds != null) {
                            buttonAddFunds.setVisibility(View.GONE);
                        }
                    } else {
                        imageAchieved.setVisibility(View.GONE);
                        imageAchieved.clearAnimation();
                        
                        if (cardView != null) {
                            cardView.setStrokeWidth(0);
                        }
                        
                        if (buttonAddFunds != null) {
                            buttonAddFunds.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }
        
        // Helper method to extract category from description
        private String extractCategory(String description) {
            if (description == null || description.isEmpty()) {
                return null;
            }
            
            // Look for "Category: X" in the description
            int categoryIndex = description.indexOf("Category:");
            if (categoryIndex != -1) {
                int startIndex = categoryIndex + 10; // "Category: ".length()
                int endIndex = description.indexOf("\n", startIndex);
                if (endIndex == -1) {
                    endIndex = description.length();
                }
                
                if (startIndex < endIndex && startIndex < description.length()) {
                    return description.substring(startIndex, endIndex).trim();
                }
            }
            
            return null;
        }

        // Helper method to update the monthly goal display with the current period mode
        private void updateMonthlyGoalDisplay(SavingsGoal goal) {
            if (textMonthlyRecommended == null || goal == null) {
                return;
            }
            
            // First ensure the goal amounts are calculated by calling getMonthlyGoal
            goal.getMonthlyGoal();
            
            if (goal.isCompleted() || goal.getRemainingAmount() <= 0) {
                textMonthlyRecommended.setText("Completed");
                textMonthlyRecommended.setTextColor(itemView.getContext().getResources().getColor(R.color.income));
                return;
            }
            
            // Check if goal is overdue
            Date now = new Date();
            boolean isOverdue = goal.getTargetDate().before(now);
            
            // Use the pre-calculated amounts based on selected period
            double displayAmount;
            String periodLabel;
            
            // Get total amounts and periods
            double targetAmount = goal.getTargetAmount();
            double currentAmount = goal.getCurrentAmount();
            double remainingAmount = goal.getRemainingAmount();
            
            // Calculate time periods
            long diffInMillis = goal.getTargetDate().getTime() - now.getTime();
            double totalDays = Math.max(1, diffInMillis / (1000.0 * 60 * 60 * 24)); // Minimum 1 day
            double totalWeeks = Math.max(0.143, totalDays / 7.0); // Minimum ~1 day
            double totalMonths = Math.max(0.0333, totalDays / TimeConstants.DAYS_IN_MONTH); // Minimum ~1 day
            
            // If goal is overdue, suggest completing it within a month
            if (isOverdue) {
                switch (goalPeriodMode) {
                    case 0: // Daily
                        displayAmount = remainingAmount / 30; // Spread over 30 days
                        periodLabel = "Daily";
                        break;
                    case 1: // Weekly
                        displayAmount = remainingAmount / 4; // Spread over 4 weeks
                        periodLabel = "Weekly";
                        break;
                    case 2: // Monthly (default)
                    default:
                        displayAmount = remainingAmount; // Complete within a month
                        periodLabel = "Monthly";
                        break;
                }
            } else {
                switch (goalPeriodMode) {
                    case 0: // Daily
                        // For daily savings, calculate how many full regular payments are left
                        double regularDailyAmount = goal.getDailyAmount();
                        int fullDays = (int) Math.ceil(totalDays);
                        
                        if (fullDays <= 1) {
                            // If it's exactly one day or less, show the full remaining amount
                            displayAmount = remainingAmount;
                        } else {
                            displayAmount = regularDailyAmount;
                        }
                        periodLabel = "Daily";
                        break;
                        
                    case 1: // Weekly
                        // Check if it's an exact number of weeks (allowing for small decimal differences)
                        boolean isExactWeeks = Math.abs(Math.round(totalWeeks) - totalWeeks) < 0.1;
                        
                        if (isExactWeeks) {
                            // If it's exactly X weeks, divide target amount equally
                            displayAmount = targetAmount / Math.round(totalWeeks);
                        } else {
                            double regularWeeklyAmount = goal.getWeeklyAmount();
                            int fullWeeks = (int) Math.ceil(totalWeeks);
                            
                            if (fullWeeks <= 1) {
                                // If it's less than or exactly one week, show the full remaining amount
                                displayAmount = remainingAmount;
                            } else {
                                displayAmount = regularWeeklyAmount;
                            }
                        }
                        periodLabel = "Weekly";
                        break;
                        
                    case 2: // Monthly (default)
                    default:
                        // Check if it's an exact number of months (allowing for small decimal differences)
                        boolean isExactMonths = Math.abs(Math.round(totalMonths) - totalMonths) < 0.1;
                        
                        if (isExactMonths) {
                            // If it's exactly X months, divide target amount equally
                            displayAmount = targetAmount / Math.round(totalMonths);
                        } else {
                            double regularMonthlyAmount = goal.getMonthlyAmount();
                            int fullMonths = (int) Math.ceil(totalMonths);
                            
                            if (fullMonths <= 1) {
                                // If it's less than or exactly one month, show the full remaining amount
                                displayAmount = remainingAmount;
                            } else {
                                displayAmount = regularMonthlyAmount;
                            }
                        }
                        periodLabel = "Monthly";
                        break;
                }
            }
            
            // Display amount with simple period label
            if (displayAmount > 0 && !Double.isInfinite(displayAmount) && !Double.isNaN(displayAmount)) {
                String displayText = String.format("%s %s", formatCurrency(displayAmount), periodLabel);
                textMonthlyRecommended.setText(displayText);
                textMonthlyRecommended.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary));
            } else {
                textMonthlyRecommended.setText("Set a goal");
                textMonthlyRecommended.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary));
            }
        }

        // Method to restore the saved period mode for this specific goal
        private void restorePeriodMode(long goalId) {
            try {
                Context context = itemView.getContext();
                android.content.SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                // Get the saved period mode with the default value of 2 (monthly)
                goalPeriodMode = prefs.getInt(KEY_GOAL_PERIOD_PREFIX + goalId, 2);
                android.util.Log.d("SavingsGoalAdapter", "Restored period mode " + goalPeriodMode + " for goal " + goalId);
            } catch (Exception e) {
                android.util.Log.e("SavingsGoalAdapter", "Error restoring period mode: " + e.getMessage());
                // Default to monthly view
                goalPeriodMode = 2;
            }
        }

        // Method to save the period mode for this specific goal
        private void savePeriodMode(long goalId) {
            try {
                Context context = itemView.getContext();
                android.content.SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                android.content.SharedPreferences.Editor editor = prefs.edit();
                
                // Save the current period mode for this goal
                editor.putInt(KEY_GOAL_PERIOD_PREFIX + goalId, goalPeriodMode);
                editor.apply();
                
                android.util.Log.d("SavingsGoalAdapter", "Saved period mode " + goalPeriodMode + " for goal " + goalId);
            } catch (Exception e) {
                android.util.Log.e("SavingsGoalAdapter", "Error saving period mode: " + e.getMessage());
            }
        }

        /**
         * Show options menu for the savings goal
         */
        private void showOptionsMenu(SavingsGoal goal, View anchorView) {
            Context context = anchorView.getContext();
            String[] options;
            
            if (goal.isCompleted()) {
                options = new String[]{"View Details", "Edit Goal", "Delete Goal"};
            } else {
                options = new String[]{"View Details", "Add Funds", "Withdraw Funds", "Edit Goal", "Mark as Completed", "Delete Goal"};
            }
            
            androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                    .setTitle(goal.getName())
                    .setItems(options, (dialogInterface, which) -> {
                        switch (which) {
                            case 0:  // View Details
                                showDetailsDialog(goal, anchorView);
                                break;
                            case 1:  // Add Funds / Edit Goal
                                if (goal.isCompleted()) {
                                    // TODO: Implement edit goal functionality
                                    break;
                                }
                                // Show add funds dialog
                                if (context instanceof Activity) {
                                    Activity activity = (Activity) context;
                                    ViewModelProvider provider = new ViewModelProvider((ViewModelStoreOwner) activity);
                                    SavingsViewModel viewModel = provider.get(SavingsViewModel.class);
                                    showAddFundsDialog(goal, anchorView, viewModel);
                                }
                                break;
                            case 2:  // Withdraw Funds / Delete Goal
                                if (goal.isCompleted()) {
                                    confirmDeleteGoal(goal, context);
                                    break;
                                }
                                showWithdrawFundsDialog(goal, anchorView);
                                break;
                            case 3:  // Edit Goal
                                // TODO: Implement edit goal functionality
                                break;
                            case 4:  // Mark as Completed
                                if (context instanceof Activity) {
                                    Activity activity = (Activity) context;
                                    ViewModelProvider provider = new ViewModelProvider((ViewModelStoreOwner) activity);
                                    SavingsViewModel viewModel = provider.get(SavingsViewModel.class);
                                    viewModel.markGoalCompleted(goal);
                                    ToastUtils.showSuccess(context, "Goal marked as completed");
                                }
                                break;
                            case 5:  // Delete Goal
                                confirmDeleteGoal(goal, context);
                                break;
                        }
                    })
                    .create();
            
            dialog.show();
            
            // Apply rounded corners to dialog window
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
            }
        }

        /**
         * Confirm deletion of a savings goal
         */
        private void confirmDeleteGoal(SavingsGoal goal, Context context) {
            androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Goal")
                    .setMessage("Are you sure you want to delete \"" + goal.getName() + "\"? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialogInterface, which) -> {
                        if (context instanceof Activity) {
                            Activity activity = (Activity) context;
                            ViewModelProvider provider = new ViewModelProvider((ViewModelStoreOwner) activity);
                            SavingsViewModel viewModel = provider.get(SavingsViewModel.class);
                            viewModel.deleteSavingsGoal(goal);
                            ToastUtils.showInfo(context, "Goal deleted");
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
                    
            dialog.show();
            
            // Apply rounded corners
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
            }
        }

        /**
         * Show dialog to add funds to a savings goal
         */
        private void showAddFundsDialog(SavingsGoal goal, View sourceView, SavingsViewModel viewModel) {
            Context context = sourceView.getContext();
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_funds, null);
            
            TextView textGoalName = dialogView.findViewById(R.id.text_goal_name);
            TextView textCurrentAmount = dialogView.findViewById(R.id.text_current_amount);
            TextView textTargetAmount = dialogView.findViewById(R.id.text_target_amount);
            TextView textProgressPercentage = dialogView.findViewById(R.id.text_progress_percentage);
            ProgressBar progressBar = dialogView.findViewById(R.id.progress_goal);
            EditText editAmount = dialogView.findViewById(R.id.edit_amount);
            
            // Calculate progress percentage
            double progressPercentage = goal.getProgressPercentage();
            
            // Set text values
            textGoalName.setText(goal.getName());
            textCurrentAmount.setText(String.format("Current: %s", currencyFormatter.format(goal.getCurrentAmount())));
            textTargetAmount.setText(String.format("Target: %s", currencyFormatter.format(goal.getTargetAmount())));
            textProgressPercentage.setText(String.format("%.0f%%", progressPercentage));
            progressBar.setProgress((int) progressPercentage);
            
            // Set up quick amount chips
            com.google.android.material.chip.Chip chip20 = dialogView.findViewById(R.id.chip_amount_20);
            com.google.android.material.chip.Chip chip50 = dialogView.findViewById(R.id.chip_amount_50);
            com.google.android.material.chip.Chip chip100 = dialogView.findViewById(R.id.chip_amount_100);
            com.google.android.material.chip.Chip chip500 = dialogView.findViewById(R.id.chip_amount_500);
            com.google.android.material.chip.Chip chip1000 = dialogView.findViewById(R.id.chip_amount_1000);
            
            // Set up quick amount button listeners
            if (chip20 != null) chip20.setOnClickListener(view -> editAmount.setText("20"));
            if (chip50 != null) chip50.setOnClickListener(view -> editAmount.setText("50"));
            if (chip100 != null) chip100.setOnClickListener(view -> editAmount.setText("100"));
            if (chip500 != null) chip500.setOnClickListener(view -> editAmount.setText("500"));
            if (chip1000 != null) chip1000.setOnClickListener(view -> editAmount.setText("1000"));
            
            androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .setPositiveButton("Add", (dialogInterface, which) -> {
                    String amountStr = editAmount.getText().toString().trim();
                    if (amountStr.isEmpty()) {
                        ToastUtils.showWarning(context, "Please enter an amount");
                        return;
                    }
                    
                    try {
                        double amount = Double.parseDouble(amountStr);
                        if (amount <= 0) {
                            ToastUtils.showWarning(context, "Amount must be positive");
                            return;
                        }
                        
                        // Create a local copy with updated amount for immediate UI feedback
                        SavingsGoal updatedGoal = goal.copy();
                        double newAmount = updatedGoal.getCurrentAmount() + amount;
                        updatedGoal.setCurrentAmount(newAmount);
                        
                        // Check if goal is now completed
                        if (newAmount >= updatedGoal.getTargetAmount() && !updatedGoal.isCompleted()) {
                            updatedGoal.setCompleted(true);
                        }
                        
                        // Update UI immediately using our own method
                        updateGoal(updatedGoal);
                        
                        // Then update in the database (async)
                        viewModel.addFundsToGoal(goal, amount);
                        
                        // Try to show confetti celebration
                        if (context instanceof Activity) {
                            Activity activity = (Activity) context;
                            View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
                            ConfettiView confettiView = rootView.findViewById(R.id.confetti_view);
                            
                            if (confettiView != null) {
                                confettiView.stop();
                                confettiView.setVisibility(View.VISIBLE);
                                confettiView.start(100);
                            }
                        }
                    } catch (NumberFormatException e) {
                        ToastUtils.showError(context, "Invalid amount");
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
            
            dialog.show();
            
            if (dialog.getWindow() != null) {
                int width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.9);
                dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
            }
        }
        
        /**
         * Show dialog to withdraw funds from a savings goal
         */
        private void showWithdrawFundsDialog(SavingsGoal goal, View sourceView) {
            Context context = sourceView.getContext();
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_withdraw_funds, null);
            
            TextView textGoalName = dialogView.findViewById(R.id.text_goal_name);
            TextView textCurrentAmount = dialogView.findViewById(R.id.text_current_amount);
            EditText editAmount = dialogView.findViewById(R.id.edit_amount);
            
            textGoalName.setText(goal.getName());
            textCurrentAmount.setText(String.format("Available: %s", currencyFormatter.format(goal.getCurrentAmount())));
            
            androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle("Withdraw Funds")
                .setView(dialogView)
                .setPositiveButton("Withdraw", (dialogInterface, i) -> {
                    String amountStr = editAmount.getText().toString().trim();
                    
                    if (amountStr.isEmpty()) {
                        ToastUtils.showWarning(context, "Amount is required");
                        return;
                    }
                    
                    try {
                        double amount = Double.parseDouble(amountStr);
                        
                        if (amount <= 0) {
                            ToastUtils.showWarning(context, "Amount must be greater than 0");
                            return;
                        }
                        
                        if (amount > goal.getCurrentAmount()) {
                            ToastUtils.showWarning(context, "Amount cannot exceed available funds");
                            return;
                        }
                        
                        if (context instanceof Activity) {
                            Activity activity = (Activity) context;
                            ViewModelProvider provider = new ViewModelProvider((ViewModelStoreOwner) activity);
                            SavingsViewModel viewModel = provider.get(SavingsViewModel.class);
                            
                            // Create a copy of the goal with updated amount for immediate UI update
                            SavingsGoal updatedGoal = goal.copy();
                            double newAmount = Math.max(0, updatedGoal.getCurrentAmount() - amount);
                            updatedGoal.setCurrentAmount(newAmount);
                            
                            // Update the adapter directly for immediate feedback
                            updateGoal(updatedGoal);
                            
                            // Show toast message
                            ToastUtils.showInfo(context, "Withdrew " + formatCurrency(amount) + " from " + goal.getName());
                            
                            // Update the database (async)
                            viewModel.withdrawFundsFromGoal(goal, amount);
                        }
                    } catch (NumberFormatException e) {
                        ToastUtils.showError(context, "Please enter a valid amount");
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
            
            dialog.show();
            
            if (dialog.getWindow() != null) {
                int width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.9);
                dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
            }
        }
    }

    /**
     * Update a specific goal in the list to avoid full list refresh
     * @param updatedGoal The updated goal
     */
    public void updateGoal(SavingsGoal updatedGoal) {
        List<SavingsGoal> currentList = getCurrentList();
        if (currentList == null) return;
        
        // Log the update
        android.util.Log.d("SavingsGoalAdapter", "updateGoal called for: " + updatedGoal.getName() + 
                ", amount: " + updatedGoal.getCurrentAmount());
        
        // Find the goal's position in the list
        int position = -1;
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getId() == updatedGoal.getId()) {
                position = i;
                break;
            }
        }
        
        if (position >= 0) {
            // Create bundle of changes
            Bundle payload = new Bundle();
            SavingsGoal oldGoal = currentList.get(position);
            
            // Only include actually changed fields in the payload
            if (Double.compare(oldGoal.getCurrentAmount(), updatedGoal.getCurrentAmount()) != 0) {
                payload.putDouble("currentAmount", updatedGoal.getCurrentAmount());
            }
            
            if (Double.compare(oldGoal.getTargetAmount(), updatedGoal.getTargetAmount()) != 0) {
                payload.putDouble("targetAmount", updatedGoal.getTargetAmount());
            }
            
            if (oldGoal.isCompleted() != updatedGoal.isCompleted()) {
                payload.putBoolean("completed", updatedGoal.isCompleted());
            }
            
            // Make a new list copy with updated goal
            List<SavingsGoal> newList = new ArrayList<>(currentList);
            newList.set(position, updatedGoal);
            
            // First try to use payload update for efficiency and better animation
            final int finalPosition = position;
            if (payload.size() > 0) {
                // Update the internal list
                submitList(newList);
                
                // Also notify with payload for smooth animation
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    android.util.Log.d("SavingsGoalAdapter", "Notifying position " + finalPosition + " with payload");
                    notifyItemChanged(finalPosition, payload);
                });
            } else {
                // If no changes detected or for full refresh
                submitList(newList);
            }
        } else {
            android.util.Log.w("SavingsGoalAdapter", "Goal not found in list: " + updatedGoal.getId());
        }
    }

    /**
     * Format currency values to be more compact for larger numbers
     */
    private String formatCurrency(double amount) {
        if (amount < 0) {
            // For negative values, apply the same formatting but add the minus sign
            return "-" + formatCurrency(-amount);
        }
        
        // Use the improved formatting method from CircularProgressView without peso sign
        return CircularProgressView.formatLargeNumber(amount);
    }

    /**
     * Set the period mode for all cards to keep them in sync with the overall view
     * @param periodMode The period mode to set for all cards (0: daily, 1: weekly, 2: monthly)
     */
    public void setGlobalPeriodMode(int periodMode) {
        android.util.Log.d("SavingsGoalAdapter", "Setting global period mode: " + periodMode);
        
        // Store the default period mode for when new ViewHolders are created
        this.defaultPeriodMode = periodMode;
        
        // Notify dataset changed to refresh the display
        notifyDataSetChanged();
    }
    
    // Member variable to track the default period mode
    private int defaultPeriodMode = 2; // Default to monthly
    
    /**
     * Helper method to find ViewHolder for a specific goal
     */
    private RecyclerView.ViewHolder getViewHolderForPosition(SavingsGoal goal) {
        try {
            RecyclerView recyclerView = null;
            
            // Try to find the RecyclerView this adapter is attached to
            for (RecyclerView.ViewHolder holder : new ArrayList<>(getAttachedViewHolders())) {
                if (holder.itemView.getParent() instanceof RecyclerView) {
                    recyclerView = (RecyclerView) holder.itemView.getParent();
                    break;
                }
            }
            
            if (recyclerView != null) {
                // Find position of the goal
                List<SavingsGoal> list = getCurrentList();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId() == goal.getId()) {
                        return recyclerView.findViewHolderForAdapterPosition(i);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SavingsGoalAdapter", "Error finding ViewHolder: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Helper method to find all currently attached ViewHolders
     */
    private List<RecyclerView.ViewHolder> getAttachedViewHolders() {
        List<RecyclerView.ViewHolder> holders = new ArrayList<>();
        try {
            // This is a best-effort approach, returning empty list if it fails
            for (RecyclerView rv : findAttachedRecyclerViews()) {
                for (int i = 0; i < rv.getChildCount(); i++) {
                    RecyclerView.ViewHolder holder = rv.getChildViewHolder(rv.getChildAt(i));
                    if (holder != null) {
                        holders.add(holder);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SavingsGoalAdapter", "Error finding ViewHolders: " + e.getMessage());
        }
        return holders;
    }
    
    /**
     * Helper method to find all RecyclerViews this adapter might be attached to
     */
    private List<RecyclerView> findAttachedRecyclerViews() {
        List<RecyclerView> recyclerViews = new ArrayList<>();
        // Best effort approach - since we can't directly determine which RecyclerView we're attached to
        // Without a context reference, we return an empty list
        return recyclerViews;
    }
} 