package com.example.personafi.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.personafi.Transaction;

import java.util.Date;
import java.util.List;

/**
 * Repository class that abstracts access to data sources.
 */
public class TransactionRepository {
    private TransactionDao transactionDao;
    private LiveData<List<Transaction>> allTransactions;
    private LiveData<List<Transaction>> allIncome;
    private LiveData<List<Transaction>> allExpenses;
    private LiveData<Double> totalIncome;
    private LiveData<Double> totalExpenses;

    /**
     * Constructor for the repository.
     */
    public TransactionRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        transactionDao = db.transactionDao();
        allTransactions = transactionDao.getAllTransactions();
        allIncome = transactionDao.getAllIncome();
        allExpenses = transactionDao.getAllExpenses();
        totalIncome = transactionDao.getTotalIncome();
        totalExpenses = transactionDao.getTotalExpenses();
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
        AppDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.insert(transaction);
        });
    }

    // Update a transaction
    public void update(Transaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.update(transaction);
        });
    }

    // Delete a transaction
    public void delete(Transaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.delete(transaction);
        });
    }

    // Get transactions between two dates
    public LiveData<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate) {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate);
    }

    // Search transactions by category
    public LiveData<List<Transaction>> searchTransactionsByCategory(String searchQuery) {
        return transactionDao.searchTransactionsByCategory(searchQuery);
    }

    // Get transactions by category
    public LiveData<List<Transaction>> getTransactionsByCategory(String category) {
        return transactionDao.getTransactionsByCategory(category);
    }

    // Get income for a specific period
    public LiveData<Double> getIncomeForPeriod(Date startDate, Date endDate) {
        return transactionDao.getIncomeForPeriod(startDate, endDate);
    }

    // Get expenses for a specific period
    public LiveData<Double> getExpensesForPeriod(Date startDate, Date endDate) {
        return transactionDao.getExpensesForPeriod(startDate, endDate);
    }

    // Get top expense category
    public LiveData<String> getTopExpenseCategory() {
        return transactionDao.getTopExpenseCategory();
    }

    // Get transactions by type
    public LiveData<List<Transaction>> getTransactionsByType(Transaction.TransactionType type) {
        return transactionDao.getTransactionsByType(type);
    }
}