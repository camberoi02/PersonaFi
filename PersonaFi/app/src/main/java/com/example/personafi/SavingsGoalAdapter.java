package com.example.personafi;

import android.content.Context;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public class SavingsGoalAdapter extends RecyclerView.Adapter<SavingsGoalAdapter.ViewHolder> {

    private List<SavingsGoal> savingsGoals;
    private Context context;
    private MissionManager missionManager;
    private NumberFormat currencyFormatter;
    private OnProgressUpdatedListener progressUpdatedListener;

    // Listener for when progress is updated, so MainActivity can react (e.g., check achievements)
    public interface OnProgressUpdatedListener {
        void onProgressUpdated();
        void onEditGoal(SavingsGoal goal, int position);
        void onDeleteGoal(int position);
    }

    public SavingsGoalAdapter(List<SavingsGoal> savingsGoals, Context context, OnProgressUpdatedListener listener) {
        this.savingsGoals = savingsGoals;
        this.context = context;
        this.progressUpdatedListener = listener;
        this.missionManager = MissionManager.getInstance();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    }

    public void updateGoals(List<SavingsGoal> newGoals) {
        if (newGoals == null) {
            this.savingsGoals = new ArrayList<>();
        } else {
            this.savingsGoals = new ArrayList<>(newGoals);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_savings_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= savingsGoals.size()) return;
        
        SavingsGoal goal = savingsGoals.get(position);
        if (goal == null) return;

        // Set basic goal information
        holder.textViewGoalName.setText(goal.getName());
        
        // Format currency amounts with compact numbers
        String formattedCurrent = "₱" + NumberFormatter.formatCompactNumber(goal.getCurrentAmount());
        String formattedTarget = "₱" + NumberFormatter.formatCompactNumber(goal.getTargetAmount());
        holder.textViewGoalProgress.setText(String.format("%s / %s", formattedCurrent, formattedTarget));
        
        // Calculate and display percentage
        int percentage = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);
        holder.textViewPercentage.setText(String.format("%d%%", percentage));
        
        // Update progress bar
        holder.progressBarGoal.setMax((int) goal.getTargetAmount());
        holder.progressBarGoal.setProgress((int) goal.getCurrentAmount());

        // Apply visual differences for completed goals
        if (goal.isAchieved()) {
            // Completed goal styling
            holder.itemView.setAlpha(1.0f);
            holder.progressBarGoal.setProgressTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.primary, null)));
            holder.textViewPercentage.setTextColor(holder.itemView.getResources().getColor(R.color.primary, null));
            holder.textViewGoalName.setTextColor(holder.itemView.getResources().getColor(R.color.primary, null));
            holder.buttonAddProgress.setEnabled(false);
            holder.buttonAddProgress.setText("Goal Completed!");
            holder.buttonAddProgress.setBackgroundTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.primary_container, null)));
            holder.buttonAddProgress.setTextColor(holder.itemView.getResources().getColor(R.color.primary, null));
            holder.buttonEditGoal.setImageResource(R.drawable.ic_check_circle);
            holder.buttonEditGoal.setImageTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.primary, null)));
        } else {
            // In-progress goal styling
            holder.itemView.setAlpha(1.0f);
            holder.progressBarGoal.setProgressTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.primary, null)));
            holder.textViewPercentage.setTextColor(holder.itemView.getResources().getColor(R.color.on_surface, null));
            holder.textViewGoalName.setTextColor(holder.itemView.getResources().getColor(R.color.on_surface, null));
            holder.buttonAddProgress.setEnabled(true);
            holder.buttonAddProgress.setText("Add Progress");
            holder.buttonAddProgress.setBackgroundTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.primary, null)));
            holder.buttonAddProgress.setTextColor(holder.itemView.getResources().getColor(R.color.on_primary, null));
            holder.buttonEditGoal.setImageResource(R.drawable.ic_edit);
            holder.buttonEditGoal.setImageTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.primary, null)));
        }

        // Set click listeners
        holder.buttonAddProgress.setOnClickListener(v -> {
            if (!goal.isAchieved()) {
                AddProgressDialog dialog = AddProgressDialog.newInstance(goal.getName(), goal.getLastAddedAmount());
                dialog.setOnProgressAddedListener(amount -> {
                    goal.setCurrentAmount(goal.getCurrentAmount() + amount);
                    goal.setLastAddedAmount(amount);
                    notifyItemChanged(position);
                    updateMissionProgress(amount);
                    if (progressUpdatedListener != null) {
                        progressUpdatedListener.onProgressUpdated();
                    }
                    if (goal.isAchieved()) {
                        CustomToast.showSuccess(context, "🎉 " + goal.getName() + " achieved!");
                    }
                });
                dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "add_progress");
            }
        });

        holder.buttonEditGoal.setOnClickListener(v -> {
            if (!goal.isAchieved()) {
                if (progressUpdatedListener != null) {
                    progressUpdatedListener.onEditGoal(goal, position);
                }
            } else {
                CustomToast.showInfo(context, "Completed goals cannot be edited");
            }
        });

        holder.buttonDeleteGoal.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete this savings goal?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (progressUpdatedListener != null) {
                        progressUpdatedListener.onDeleteGoal(position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    private void updateMissionProgress(double amount) {
        if (missionManager != null && amount > 0) {  // Only update if amount is greater than 0
            Mission dailyMission = missionManager.getCurrentDailyMission();
            if (dailyMission != null) {
                // Check which mission it is and update accordingly
                switch (dailyMission.getTitle()) {
                    case "Quick Save":
                        if (amount >= 20) {
                            missionManager.updateMissionProgress(true, (int) amount);
                        }
                        break;
                    case "Big Saver":
                        if (amount >= 30) {
                            missionManager.updateMissionProgress(true, (int) amount);
                        }
                        break;
                    case "Perfect Save":
                        if (amount == 25) {
                            missionManager.updateMissionProgress(true, (int) amount);
                        }
                        break;
                    case "Early Bird":
                        // Check if it's before noon
                        if (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) < 12) {
                            missionManager.updateMissionProgress(true, 1);
                        }
                        break;
                    case "Multi-Goal":
                        // For Multi-Goal, we only increment by 1 since we're tracking number of goals
                        missionManager.updateMissionProgress(true, 1);
                        break;
                    case "Goal Contributor":
                        // Check if all goals have received an amount today
                        boolean allGoalsUpdated = true;
                        for (SavingsGoal goal : savingsGoals) {
                            if (goal.getLastAddedAmount() <= 0) {
                                allGoalsUpdated = false;
                                break;
                            }
                        }
                        if (allGoalsUpdated) {
                            missionManager.updateMissionProgress(true, 1);
                        }
                        break;
                    case "Progress Maker":
                        // For Progress Maker, we only increment by 1 since we're tracking number of goals
                        missionManager.updateMissionProgress(true, 1);
                        break;
                    case "Growth Spurt":
                        // For Growth Spurt, we only increment by 1 since we're tracking number of goals
                        missionManager.updateMissionProgress(true, 1);
                        break;
                }
            }

            // Handle weekly missions
            Mission weeklyMission = missionManager.getCurrentWeeklyMission();
            if (weeklyMission != null) {
                // Check which weekly mission it is and update accordingly
                switch (weeklyMission.getTitle()) {
                    case "Friday Bonus":
                        if (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY && amount >= 20) {
                            missionManager.updateMissionProgress(false, (int) amount);
                        }
                        break;
                    case "Weekly Goal":
                        missionManager.updateMissionProgress(false, (int) amount);
                        break;
                    case "Wednesday Special":
                        if (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.WEDNESDAY && amount >= 30) {
                            missionManager.updateMissionProgress(false, (int) amount);
                        }
                        break;
                    case "Weekly Growth":
                        // For Weekly Growth, we only increment by 1 since we're tracking number of goals
                        missionManager.updateMissionProgress(false, 1);
                        break;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return savingsGoals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGoalName;
        ProgressBar progressBarGoal;
        TextView textViewGoalProgress;
        TextView textViewPercentage;
        Button buttonAddProgress;
        ImageButton buttonEditGoal;
        Button buttonDeleteGoal;

        ViewHolder(View itemView) {
            super(itemView);
            textViewGoalName = itemView.findViewById(R.id.textViewGoalName);
            progressBarGoal = itemView.findViewById(R.id.progressBarGoal);
            textViewGoalProgress = itemView.findViewById(R.id.textViewGoalProgress);
            textViewPercentage = itemView.findViewById(R.id.textViewPercentage);
            buttonAddProgress = itemView.findViewById(R.id.buttonAddProgress);
            buttonEditGoal = itemView.findViewById(R.id.buttonEditGoal);
            buttonDeleteGoal = itemView.findViewById(R.id.buttonDeleteGoal);
        }
    }
} 