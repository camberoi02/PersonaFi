package com.example.personafi;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.personafi.database.BudgetRepository;

import java.util.Date;
import java.util.List;

/**
 * ViewModel to store and manage UI-related budget data in a lifecycle conscious way.
 */
public class BudgetViewModel extends AndroidViewModel {

    private BudgetRepository repository;
    private final LiveData<List<Budget>> allBudgets;

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        repository = new BudgetRepository(application);
        allBudgets = repository.getAllBudgets();
    }

    // Get all budgets
    public LiveData<List<Budget>> getAllBudgets() {
        return allBudgets;
    }

    // Get active budgets
    public LiveData<List<Budget>> getActiveBudgets() {
        return repository.getActiveBudgets();
    }

    // Get budget by category
    public LiveData<Budget> getBudgetByCategory(String category) {
        return repository.getBudgetByCategory(category);
    }

    // Get budget with spending
    public LiveData<BudgetWithSpending> getBudgetWithSpending(int budgetId) {
        return repository.getBudgetWithSpending(budgetId);
    }

    // Get all budgets with spending
    public LiveData<List<BudgetWithSpending>> getAllBudgetsWithSpending() {
        return repository.getAllBudgetsWithSpending();
    }

    // Insert a budget
    public void insert(Budget budget) {
        repository.insert(budget);
    }

    // Update a budget
    public void update(Budget budget) {
        repository.update(budget);
    }

    // Delete a budget
    public void delete(Budget budget) {
        repository.delete(budget);
    }
} 