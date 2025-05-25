package com.example.personafi.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.personafi.SavingsGoal;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for the SavingsGoal entity.
 */
@Dao
public interface SavingsGoalDao {

    // Insert a new savings goal
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SavingsGoal savingsGoal);

    // Update an existing savings goal
    @Update
    void update(SavingsGoal savingsGoal);

    // Delete a savings goal
    @Delete
    void delete(SavingsGoal savingsGoal);

    // Get all savings goals
    @Query("SELECT * FROM savings_goals ORDER BY targetDate ASC")
    LiveData<List<SavingsGoal>> getAllSavingsGoals();

    // Get active (incomplete) savings goals
    @Query("SELECT * FROM savings_goals WHERE isCompleted = 0 ORDER BY targetDate ASC")
    LiveData<List<SavingsGoal>> getActiveSavingsGoals();

    // Get completed savings goals
    @Query("SELECT * FROM savings_goals WHERE isCompleted = 1 ORDER BY targetDate DESC")
    LiveData<List<SavingsGoal>> getCompletedSavingsGoals();

    // Get total current amount across all savings goals
    @Query("SELECT SUM(currentAmount) FROM savings_goals")
    LiveData<Double> getTotalSavings();
    
    // Get total target amount across all active savings goals
    @Query("SELECT SUM(targetAmount) FROM savings_goals WHERE isCompleted = 0")
    LiveData<Double> getTotalSavingsTarget();
    
    // Get savings goals with upcoming target dates (within next 30 days)
    @Query("SELECT * FROM savings_goals WHERE isCompleted = 0 AND targetDate BETWEEN :today AND :future ORDER BY targetDate ASC")
    LiveData<List<SavingsGoal>> getUpcomingSavingsGoals(Date today, Date future);
    
    // Find savings goal by name (for search functionality)
    @Query("SELECT * FROM savings_goals WHERE name LIKE '%' || :search || '%'")
    LiveData<List<SavingsGoal>> searchSavingsGoals(String search);
    
    // Get high priority savings goals
    @Query("SELECT * FROM savings_goals WHERE isCompleted = 0 AND priority = 2 ORDER BY targetDate ASC") // HIGH priority is 2 in the enum
    LiveData<List<SavingsGoal>> getHighPrioritySavingsGoals();

    // Get savings goal by ID (for direct access)
    @Query("SELECT * FROM savings_goals WHERE id = :goalId LIMIT 1")
    SavingsGoal getSavingsGoalById(int goalId);
} 