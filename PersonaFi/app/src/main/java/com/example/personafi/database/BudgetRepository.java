package com.example.personafi.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.personafi.Budget;
import com.example.personafi.BudgetWithSpending;

import java.util.Date;
import java.util.List;

/**
 * Repository class that abstracts access to budget data sources.
 */
public class BudgetRepository {
    private BudgetDao budgetDao;
    private LiveData<List<Budget>> allBudgets;

    /**
     * Constructor for the repository.
     */
    public BudgetRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        budgetDao = db.budgetDao();
        allBudgets = budgetDao.getAllBudgets();
    }

    // Get all budgets
    public LiveData<List<Budget>> getAllBudgets() {
        return allBudgets;
    }

    // Get active budgets
    public LiveData<List<Budget>> getActiveBudgets() {
        return budgetDao.getActiveBudgets(new Date());
    }

    // Get budget by category
    public LiveData<Budget> getBudgetByCategory(String category) {
        return budgetDao.getBudgetByCategory(category);
    }

    // Get budget with spending
    public LiveData<BudgetWithSpending> getBudgetWithSpending(int budgetId) {
        return budgetDao.getBudgetWithSpending(budgetId);
    }

    // Get all budgets with spending
    public LiveData<List<BudgetWithSpending>> getAllBudgetsWithSpending() {
        return budgetDao.getAllBudgetsWithSpending();
    }

    // Insert a budget
    public void insert(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.insert(budget);
        });
    }

    // Update a budget
    public void update(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.update(budget);
        });
    }

    // Delete a budget
    public void delete(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.delete(budget);
        });
    }
} 