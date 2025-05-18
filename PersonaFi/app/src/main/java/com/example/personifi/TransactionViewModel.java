package com.example.personifi;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.personifi.database.TransactionRepository;

import java.util.Date;
import java.util.List;

/**
 * ViewModel to store and manage UI-related data in a lifecycle conscious way.
 */
public class TransactionViewModel extends AndroidViewModel {

    private TransactionRepository repository;
    private final LiveData<List<Transaction>> allTransactions;
    private final LiveData<List<Transaction>> allIncome;
    private final LiveData<List<Transaction>> allExpenses;
    private final LiveData<Double> totalIncome;
    private final LiveData<Double> totalExpenses;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
        allTransactions = repository.getAllTransactions();
        allIncome = repository.getAllIncome();
        allExpenses = repository.getAllExpenses();
        totalIncome = repository.getTotalIncome();
        totalExpenses = repository.getTotalExpenses();
    }

    // Get all transactions
    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    // Get all income transactions
    public LiveData<List<Transaction>> getAllIncome() {
        return allIncome;
    }

    // Get all expense transactions
    public LiveData<List<Transaction>> getAllExpenses() {
        return allExpenses;
    }

    // Get total income
    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    // Get total expenses
    public LiveData<Double> getTotalExpenses() {
        return totalExpenses;
    }

    // Insert a transaction
    public void insert(Transaction transaction) {
        repository.insert(transaction);
    }

    // Update a transaction
    public void update(Transaction transaction) {
        repository.update(transaction);
    }

    // Delete a transaction
    public void delete(Transaction transaction) {
        repository.delete(transaction);
    }

    // Get transactions between two dates
    public LiveData<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate) {
        return repository.getTransactionsBetweenDates(startDate, endDate);
    }

    // Search transactions by category
    public LiveData<List<Transaction>> searchTransactionsByCategory(String searchQuery) {
        return repository.searchTransactionsByCategory(searchQuery);
    }

    // Get transactions by category
    public LiveData<List<Transaction>> getTransactionsByCategory(String category) {
        return repository.getTransactionsByCategory(category);
    }

    // Get income for a specific period
    public LiveData<Double> getIncomeForPeriod(Date startDate, Date endDate) {
        return repository.getIncomeForPeriod(startDate, endDate);
    }

    // Get expenses for a specific period
    public LiveData<Double> getExpensesForPeriod(Date startDate, Date endDate) {
        return repository.getExpensesForPeriod(startDate, endDate);
    }

    // Get top expense category
    public LiveData<String> getTopExpenseCategory() {
        return repository.getTopExpenseCategory();
    }
}