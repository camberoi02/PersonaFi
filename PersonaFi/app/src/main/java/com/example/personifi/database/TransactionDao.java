package com.example.personifi.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.personifi.Transaction;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for the Transaction entity.
 */
@Dao
public interface TransactionDao {

    // Insert a new transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Transaction transaction);

    // Update an existing transaction
    @Update
    void update(Transaction transaction);

    // Delete a transaction
    @Delete
    void delete(Transaction transaction);

    // Get all transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions();

    // Get all income transactions
    @Query("SELECT * FROM transactions WHERE type = 0 ORDER BY date DESC") // INCOME is 0 in the enum
    LiveData<List<Transaction>> getAllIncome();

    // Get all expense transactions
    @Query("SELECT * FROM transactions WHERE type = 1 ORDER BY date DESC") // EXPENSE is 1 in the enum
    LiveData<List<Transaction>> getAllExpenses();

    // Get transactions between two dates
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate);

    // Get total income
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 0") // INCOME is 0 in the enum
    LiveData<Double> getTotalIncome();

    // Get total expenses
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 1") // EXPENSE is 1 in the enum
    LiveData<Double> getTotalExpenses();

    // Search transactions by category
    @Query("SELECT * FROM transactions WHERE category LIKE '%' || :searchQuery || '%' ORDER BY date DESC")
    LiveData<List<Transaction>> searchTransactionsByCategory(String searchQuery);

    // Get transactions by category
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(String category);

    // Get income for a specific period
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 0 AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getIncomeForPeriod(Date startDate, Date endDate);

    // Get expenses for a specific period
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 1 AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getExpensesForPeriod(Date startDate, Date endDate);

    // Get top expense category
    @Query("SELECT category FROM transactions WHERE type = 1 GROUP BY category ORDER BY SUM(amount) DESC LIMIT 1")
    LiveData<String> getTopExpenseCategory();
}