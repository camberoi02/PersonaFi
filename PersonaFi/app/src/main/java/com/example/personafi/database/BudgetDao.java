package com.example.personafi.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.personafi.Budget;
import com.example.personafi.BudgetWithSpending;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for the Budget entity.
 */
@Dao
public interface BudgetDao {

    // Insert a new budget
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Budget budget);

    // Update an existing budget
    @Update
    void update(Budget budget);

    // Delete a budget
    @Delete
    void delete(Budget budget);

    // Get all budgets
    @Query("SELECT * FROM budgets ORDER BY startDate DESC")
    LiveData<List<Budget>> getAllBudgets();

    // Get budget by category
    @Query("SELECT * FROM budgets WHERE category = :category LIMIT 1")
    LiveData<Budget> getBudgetByCategory(String category);
    
    // Get active budgets for current date
    @Query("SELECT * FROM budgets WHERE :currentDate BETWEEN startDate AND endDate")
    LiveData<List<Budget>> getActiveBudgets(Date currentDate);
    
    // Get budget spending vs. target
    @Query("SELECT b.*, SUM(t.amount) as spent FROM budgets b " +
           "LEFT JOIN transactions t ON t.category = b.category AND " +
           "t.date BETWEEN b.startDate AND b.endDate AND t.type = 1 " + 
           "WHERE b.id = :budgetId GROUP BY b.id")
    LiveData<BudgetWithSpending> getBudgetWithSpending(int budgetId);
    
    // Get all budgets with spending
    @Query("SELECT b.*, COALESCE(SUM(t.amount), 0) as spent FROM budgets b " +
           "LEFT JOIN transactions t ON t.category = b.category AND " +
           "t.date BETWEEN b.startDate AND b.endDate AND t.type = 1 " + 
           "GROUP BY b.id")
    LiveData<List<BudgetWithSpending>> getAllBudgetsWithSpending();
} 