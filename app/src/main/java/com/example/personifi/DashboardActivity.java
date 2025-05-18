package com.example.personifi;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TransactionViewModel transactionViewModel;
    private TextView textCurrentMonth;
    private TextView textMonthlyIncome;
    private TextView textMonthlyExpenses;
    private TextView textMonthlyBalance;
    private TextView textTopCategory;
    private TextView textTotalSavings;
    private MaterialCardView cardNoTransactions;
    private MaterialCardView cardStats;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat monthYearFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        
        // Handle edge to edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        textCurrentMonth = findViewById(R.id.text_current_month);
        textMonthlyIncome = findViewById(R.id.text_monthly_income);
        textMonthlyExpenses = findViewById(R.id.text_monthly_expenses);
        textMonthlyBalance = findViewById(R.id.text_monthly_balance);
        textTopCategory = findViewById(R.id.text_top_category);
        textTotalSavings = findViewById(R.id.text_total_savings);
        cardNoTransactions = findViewById(R.id.card_no_transactions);
        cardStats = findViewById(R.id.card_stats);

        // Setup formatters
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fil", "PH"));
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        // Set current month
        textCurrentMonth.setText(monthYearFormat.format(new Date()));

        // Setup ViewModel
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Get first day of current month
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();

        // Get last day of current month
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endDate = calendar.getTime();

        // Observe all transactions to determine if we have any data
        transactionViewModel.getAllTransactions().observe(this, transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                cardNoTransactions.setVisibility(View.VISIBLE);
                cardStats.setVisibility(View.GONE);
            } else {
                cardNoTransactions.setVisibility(View.GONE);
                cardStats.setVisibility(View.VISIBLE);
            }
        });

        // Observe monthly income
        transactionViewModel.getIncomeForPeriod(startDate, endDate).observe(this, monthlyIncome -> {
            if (monthlyIncome != null) {
                textMonthlyIncome.setText(currencyFormatter.format(monthlyIncome));
                updateMonthlyBalance();
            } else {
                textMonthlyIncome.setText(currencyFormatter.format(0));
            }
        });

        // Observe monthly expenses
        transactionViewModel.getExpensesForPeriod(startDate, endDate).observe(this, monthlyExpenses -> {
            if (monthlyExpenses != null) {
                textMonthlyExpenses.setText(currencyFormatter.format(monthlyExpenses));
                updateMonthlyBalance();
            } else {
                textMonthlyExpenses.setText(currencyFormatter.format(0));
            }
        });

        // Observe top expense category
        transactionViewModel.getTopExpenseCategory().observe(this, topCategory -> {
            if (topCategory != null && !topCategory.isEmpty()) {
                textTopCategory.setText(topCategory);
            } else {
                textTopCategory.setText("N/A");
            }
        });

        // Observe total savings (all time income - expenses)
        transactionViewModel.getTotalIncome().observe(this, income -> {
            updateTotalSavings();
        });

        transactionViewModel.getTotalExpenses().observe(this, expenses -> {
            updateTotalSavings();
        });
    }

    /**
     * Update the monthly balance display
     */
    private void updateMonthlyBalance() {
        Double income = 0.0;
        Double expenses = 0.0;
        
        try {
            // Parse income
            String incomeText = textMonthlyIncome.getText().toString().replace("₱", "")
                    .replace(",", "");
            if (!incomeText.isEmpty()) {
                income = Double.parseDouble(incomeText);
            }
            
            // Parse expenses
            String expensesText = textMonthlyExpenses.getText().toString().replace("₱", "")
                    .replace(",", "");
            if (!expensesText.isEmpty()) {
                expenses = Double.parseDouble(expensesText);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        // Calculate and display balance
        double balance = income - expenses;
        textMonthlyBalance.setText(currencyFormatter.format(balance));
        
        // Set color based on positive or negative balance
        if (balance >= 0) {
            textMonthlyBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            textMonthlyBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    /**
     * Update total savings display
     */
    private void updateTotalSavings() {
        transactionViewModel.getTotalIncome().getValue();
        transactionViewModel.getTotalExpenses().getValue();

        Double totalIncome = transactionViewModel.getTotalIncome().getValue();
        Double totalExpenses = transactionViewModel.getTotalExpenses().getValue();
        
        if (totalIncome == null) totalIncome = 0.0;
        if (totalExpenses == null) totalExpenses = 0.0;
        
        double savings = totalIncome - totalExpenses;
        textTotalSavings.setText(currencyFormatter.format(savings));
    }
} 