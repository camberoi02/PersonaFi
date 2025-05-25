package com.example.personafi;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.personafi.database.AppDatabase;
import com.example.personafi.database.SavingsRepository;

import java.util.Date;
import java.util.List;

/**
 * ViewModel for the savings feature to manage UI-related data.
 */
public class SavingsViewModel extends AndroidViewModel {
    
    private final SavingsRepository repository;
    
    private final LiveData<List<SavingsGoal>> allSavingsGoals;
    private final LiveData<List<SavingsGoal>> activeSavingsGoals;
    private final LiveData<Double> totalSavings;
    private final LiveData<Double> totalSavingsTarget;
    private final LiveData<List<SavingsGoal>> upcomingSavingsGoals;
    private final LiveData<List<SavingsGoal>> highPrioritySavingsGoals;
    
    public SavingsViewModel(@NonNull Application application) {
        super(application);
        repository = new SavingsRepository(application);
        
        allSavingsGoals = repository.getAllSavingsGoals();
        activeSavingsGoals = repository.getActiveSavingsGoals();
        totalSavings = repository.getTotalSavings();
        totalSavingsTarget = repository.getTotalSavingsTarget();
        upcomingSavingsGoals = repository.getUpcomingSavingsGoals();
        highPrioritySavingsGoals = repository.getHighPrioritySavingsGoals();
    }
    
    // Create a new savings goal
    public void createSavingsGoal(String name, String description, double targetAmount,
                                  double initialAmount, Date targetDate, 
                                  SavingsGoal.Priority priority) {
        // The SavingsGoal constructor will now automatically capitalize the name
        SavingsGoal savingsGoal = new SavingsGoal(
                name,
                description,
                targetAmount,
                initialAmount,
                new Date(), // created date is now
                targetDate,
                priority
        );
        
        repository.insert(savingsGoal);
    }
    
    // Add funds to a savings goal
    public void addFundsToGoal(SavingsGoal savingsGoal, double amount) {
        // Add logging
        android.util.Log.d("SavingsViewModel", "Adding funds: " + amount + " to goal: " + savingsGoal.getName());
        
        // First update the goal amount directly (for immediate UI refresh)
        double newAmount = savingsGoal.getCurrentAmount() + amount;
        savingsGoal.setCurrentAmount(newAmount);
        
        // Check if goal is now completed
        if (newAmount >= savingsGoal.getTargetAmount() && !savingsGoal.isCompleted()) {
            savingsGoal.setCompleted(true);
            android.util.Log.d("SavingsViewModel", "Goal completed: " + savingsGoal.getName());
        }
        
        // Then update in DB (which will trigger LiveData refresh)
        repository.addFundsToGoal(savingsGoal, amount);
        
        // Force LiveData update to ensure UI updates
        // This is important to notify observers even when the content change might not be detected
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Slight delay to ensure DB operation completes
                Thread.sleep(50);
                // Force a refresh of LiveData
                // We need to do this on main thread since LiveData is main-thread-only
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    // We'll let Room's observer handle the update, but we need to ensure the 
                    // operation is complete first
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    
    // Withdraw funds from a savings goal
    public void withdrawFundsFromGoal(SavingsGoal savingsGoal, double amount) {
        // First update the goal amount directly (for immediate UI refresh)
        double newAmount = Math.max(0, savingsGoal.getCurrentAmount() - amount);
        savingsGoal.setCurrentAmount(newAmount);
        
        // Check if goal is no longer completed after withdrawal
        boolean wasCompleted = savingsGoal.isCompleted();
        if (wasCompleted && newAmount < savingsGoal.getTargetAmount()) {
            savingsGoal.setCompleted(false);
            android.util.Log.d("SavingsViewModel", "Goal became incomplete after withdrawal: " + savingsGoal.getName());
        }
        
        // Then update in DB (which will trigger LiveData refresh)
        repository.withdrawFundsFromGoal(savingsGoal, amount);
        
        // Force LiveData update to ensure UI updates
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Slight delay to ensure DB operation completes
                Thread.sleep(50);
                // Force a refresh of LiveData on main thread
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    // We'll let Room's observer handle the update, but we need to ensure the 
                    // operation is complete first
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    
    // Mark a savings goal as completed
    public void markGoalCompleted(SavingsGoal savingsGoal) {
        // Clone the goal and update it immediately in memory for UI
        SavingsGoal updatedGoal = savingsGoal.copy();
        updatedGoal.setCompleted(true);
        
        // Update in DB
        repository.markGoalCompleted(savingsGoal);
    }
    
    // Update a savings goal
    public void updateSavingsGoal(SavingsGoal savingsGoal) {
        repository.update(savingsGoal);
    }
    
    // Delete a savings goal
    public void deleteSavingsGoal(SavingsGoal savingsGoal) {
        repository.delete(savingsGoal);
    }
    
    // Get all savings goals
    public LiveData<List<SavingsGoal>> getAllSavingsGoals() {
        return allSavingsGoals;
    }
    
    // Get active savings goals
    public LiveData<List<SavingsGoal>> getActiveSavingsGoals() {
        return activeSavingsGoals;
    }
    
    // Get total savings
    public LiveData<Double> getTotalSavings() {
        return totalSavings;
    }
    
    // Get total savings target
    public LiveData<Double> getTotalSavingsTarget() {
        return totalSavingsTarget;
    }
    
    // Get upcoming savings goals
    public LiveData<List<SavingsGoal>> getUpcomingSavingsGoals() {
        return upcomingSavingsGoals;
    }
    
    // Get high priority savings goals
    public LiveData<List<SavingsGoal>> getHighPrioritySavingsGoals() {
        return highPrioritySavingsGoals;
    }
    
    // Search savings goals by name
    public LiveData<List<SavingsGoal>> searchSavingsGoals(String query) {
        return repository.searchSavingsGoals(query);
    }
    
    // Calculate overall savings progress
    public double getOverallProgress() {
        Double total = totalSavings.getValue();
        Double target = totalSavingsTarget.getValue();
        
        if (total == null || target == null || target == 0) {
            return 0;
        }
        
        return (total / target) * 100;
    }
    
    /**
     * Calculate the suggested monthly savings amount to reach all active goals
     * @return Suggested monthly savings amount
     */
    public double getSuggestedMonthlySavings() {
        // Get the active goals to calculate total monthly goal
        List<SavingsGoal> goals = activeSavingsGoals.getValue();
        if (goals == null || goals.isEmpty()) {
            android.util.Log.d("SavingsViewModel", "No active goals, returning 0");
            return 0; // No goals, no monthly savings needed
        }
        
        // Sum up the individual monthly goals for each active goal
        // This ensures the total exactly matches the sum of individual cards
        double totalMonthlyGoal = 0;
        for (SavingsGoal goal : goals) {
            if (!goal.isCompleted()) {
                // Get this goal's individual monthly goal - this is the exact same calculation 
                // used in individual goal cards
                double monthlyGoal = goal.getMonthlyGoal();
                
                // Get target date info for debugging
                Date targetDate = goal.getTargetDate();
                Date currentDate = new Date();
                String targetDateStr = targetDate != null ? 
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(targetDate) : "null";
                String currentDateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(currentDate);
                    
                // Add this goal's contribution to the total
                totalMonthlyGoal += monthlyGoal;
                
                // Log details for debugging
                android.util.Log.d("SavingsViewModel", "Goal: " + goal.getName() + 
                                   ", Target: " + goal.getTargetAmount() +
                                   ", Current: " + goal.getCurrentAmount() +
                                   ", Remaining: " + goal.getRemainingAmount() +
                                   ", Target date: " + targetDateStr +
                                   ", Current date: " + currentDateStr +
                                   ", Monthly goal: " + monthlyGoal + 
                                   ", Running total: " + totalMonthlyGoal);
            }
        }
        
        android.util.Log.d("SavingsViewModel", "Total monthly savings goal: " + totalMonthlyGoal);
        return totalMonthlyGoal;
    }
} 