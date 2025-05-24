package com.example.personafi.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.personafi.SavingsGoal;
import com.example.personafi.database.SavingsGoalDao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Repository class for the savings feature.
 * Manages data operations between the ViewModel and the database.
 */
public class SavingsRepository {

    private final SavingsGoalDao savingsGoalDao;
    private final LiveData<List<SavingsGoal>> allSavingsGoals;
    private final LiveData<List<SavingsGoal>> activeSavingsGoals;
    private final LiveData<Double> totalSavings;
    private final LiveData<Double> totalSavingsTarget;
    private final LiveData<List<SavingsGoal>> upcomingSavingsGoals;
    private final LiveData<List<SavingsGoal>> highPrioritySavingsGoals;

    /**
     * Constructor initializes the repository with the application context.
     */
    public SavingsRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        savingsGoalDao = database.savingsGoalDao();
        
        allSavingsGoals = savingsGoalDao.getAllSavingsGoals();
        activeSavingsGoals = savingsGoalDao.getActiveSavingsGoals();
        totalSavings = savingsGoalDao.getTotalSavings();
        totalSavingsTarget = savingsGoalDao.getTotalSavingsTarget();
        
        // Calculate dates for upcoming goals (next 30 days)
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date future = calendar.getTime();
        
        upcomingSavingsGoals = savingsGoalDao.getUpcomingSavingsGoals(today, future);
        highPrioritySavingsGoals = savingsGoalDao.getHighPrioritySavingsGoals();
    }

    // Database operations
    
    /**
     * Insert a new savings goal.
     */
    public void insert(SavingsGoal savingsGoal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            savingsGoalDao.insert(savingsGoal);
        });
    }
    
    /**
     * Update an existing savings goal.
     */
    public void update(SavingsGoal savingsGoal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            savingsGoalDao.update(savingsGoal);
        });
    }
    
    /**
     * Delete a savings goal.
     */
    public void delete(SavingsGoal savingsGoal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            savingsGoalDao.delete(savingsGoal);
        });
    }
    
    /**
     * Add funds to a savings goal.
     */
    public void addFundsToGoal(SavingsGoal savingsGoal, double amount) {
        android.util.Log.d("SavingsRepository", "Adding funds to goal: " + savingsGoal.getName() + ", amount: " + amount);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Get the most current version from the database first
                SavingsGoal currentGoal = savingsGoalDao.getSavingsGoalById(savingsGoal.getId());
                if (currentGoal != null) {
                    // Use the most current data
                    double newAmount = currentGoal.getCurrentAmount() + amount;
                    currentGoal.setCurrentAmount(newAmount);
                    
                    // Check if this completes the goal
                    if (newAmount >= currentGoal.getTargetAmount() && !currentGoal.isCompleted()) {
                        currentGoal.setCompleted(true);
                        android.util.Log.d("SavingsRepository", "Goal completed: " + currentGoal.getName());
                    }
                    
                    // Update in DB
                    savingsGoalDao.update(currentGoal);
                    android.util.Log.d("SavingsRepository", "Updated goal in DB: " + currentGoal.getName() + 
                        ", new amount: " + currentGoal.getCurrentAmount());
                } else {
                    // Fall back to the provided goal if can't find in DB
                    double newAmount = savingsGoal.getCurrentAmount() + amount;
                    savingsGoal.setCurrentAmount(newAmount);
                    
                    // Check if this completes the goal
                    if (newAmount >= savingsGoal.getTargetAmount() && !savingsGoal.isCompleted()) {
                        savingsGoal.setCompleted(true);
                        android.util.Log.d("SavingsRepository", "Goal completed: " + savingsGoal.getName());
                    }
                    
                    // Update in DB
                    savingsGoalDao.update(savingsGoal);
                    android.util.Log.d("SavingsRepository", "Fallback update: " + savingsGoal.getName() + 
                        ", new amount: " + savingsGoal.getCurrentAmount());
                }
                android.util.Log.d("SavingsRepository", "Funds added successfully.");
            } catch (Exception e) {
                android.util.Log.e("SavingsRepository", "Error adding funds: " + e.getMessage());
            }
        });
    }
    
    /**
     * Withdraw funds from a savings goal.
     */
    public void withdrawFundsFromGoal(SavingsGoal savingsGoal, double amount) {
        android.util.Log.d("SavingsRepository", "Withdrawing funds from goal: " + savingsGoal.getName() + ", amount: " + amount);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Get the most current version from the database first
                SavingsGoal currentGoal = savingsGoalDao.getSavingsGoalById(savingsGoal.getId());
                if (currentGoal != null) {
                    // Use the most current data
                    double newAmount = Math.max(0, currentGoal.getCurrentAmount() - amount);
                    currentGoal.setCurrentAmount(newAmount);
                    
                    // Check if the goal is no longer completed after withdrawal
                    boolean wasCompleted = currentGoal.isCompleted();
                    if (wasCompleted && newAmount < currentGoal.getTargetAmount()) {
                        currentGoal.setCompleted(false);
                        android.util.Log.d("SavingsRepository", "Goal no longer completed: " + currentGoal.getName());
                    }
                    
                    // Update in DB
                    savingsGoalDao.update(currentGoal);
                    android.util.Log.d("SavingsRepository", "Updated goal in DB: " + currentGoal.getName() + 
                        ", new amount: " + currentGoal.getCurrentAmount());
                } else {
                    // Fall back to the provided goal if can't find in DB
                    double newAmount = Math.max(0, savingsGoal.getCurrentAmount() - amount);
                    savingsGoal.setCurrentAmount(newAmount);
                    
                    // Check if the goal is no longer completed after withdrawal
                    boolean wasCompleted = savingsGoal.isCompleted();
                    if (wasCompleted && newAmount < savingsGoal.getTargetAmount()) {
                        savingsGoal.setCompleted(false);
                        android.util.Log.d("SavingsRepository", "Goal no longer completed: " + savingsGoal.getName());
                    }
                    
                    // Update in DB
                    savingsGoalDao.update(savingsGoal);
                    android.util.Log.d("SavingsRepository", "Fallback update: " + savingsGoal.getName() + 
                        ", new amount: " + savingsGoal.getCurrentAmount());
                }
                android.util.Log.d("SavingsRepository", "Funds withdrawn successfully.");
            } catch (Exception e) {
                android.util.Log.e("SavingsRepository", "Error withdrawing funds: " + e.getMessage());
            }
        });
    }
    
    /**
     * Mark a savings goal as completed.
     */
    public void markGoalCompleted(SavingsGoal savingsGoal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            savingsGoal.setCompleted(true);
            savingsGoalDao.update(savingsGoal);
        });
    }
    
    // Data access methods
    
    /**
     * Get all savings goals.
     */
    public LiveData<List<SavingsGoal>> getAllSavingsGoals() {
        return allSavingsGoals;
    }
    
    /**
     * Get active (incomplete) savings goals.
     */
    public LiveData<List<SavingsGoal>> getActiveSavingsGoals() {
        return activeSavingsGoals;
    }
    
    /**
     * Get the total amount across all savings goals.
     */
    public LiveData<Double> getTotalSavings() {
        return totalSavings;
    }
    
    /**
     * Get the total target amount across all active savings goals.
     */
    public LiveData<Double> getTotalSavingsTarget() {
        return totalSavingsTarget;
    }
    
    /**
     * Get savings goals with upcoming target dates (within next 30 days).
     */
    public LiveData<List<SavingsGoal>> getUpcomingSavingsGoals() {
        return upcomingSavingsGoals;
    }
    
    /**
     * Get high priority savings goals.
     */
    public LiveData<List<SavingsGoal>> getHighPrioritySavingsGoals() {
        return highPrioritySavingsGoals;
    }
    
    /**
     * Search for savings goals by name.
     */
    public LiveData<List<SavingsGoal>> searchSavingsGoals(String query) {
        return savingsGoalDao.searchSavingsGoals(query);
    }
} 