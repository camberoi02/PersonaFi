package com.example.personifi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

public class BudgetActivity extends AppCompatActivity {

    private BudgetViewModel budgetViewModel;
    private RecyclerView recyclerView;
    private TextView textEmptyBudgets;
    private FloatingActionButton fabAddBudget;
    private NumberFormat currencyFormatter;

    // Activity result launcher for adding a budget
    private final ActivityResultLauncher<Intent> addBudgetLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    // Create budget from result data
                    String category = data.getStringExtra("BUDGET_CATEGORY");
                    double amount = data.getDoubleExtra("BUDGET_AMOUNT", 0);
                    long startDateMillis = data.getLongExtra("BUDGET_START_DATE", System.currentTimeMillis());
                    long endDateMillis = data.getLongExtra("BUDGET_END_DATE", System.currentTimeMillis());
                    int periodOrdinal = data.getIntExtra("BUDGET_PERIOD", 0);
                    Budget.BudgetPeriod period = Budget.BudgetPeriod.values()[periodOrdinal];
                    
                    // Create and insert budget
                    Budget budget = new Budget(category, amount, new Date(startDateMillis), 
                            new Date(endDateMillis), period);
                    budgetViewModel.insert(budget);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budget);

        // Handle edge to edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.budget_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        recyclerView = findViewById(R.id.recyclerview_budgets);
        textEmptyBudgets = findViewById(R.id.text_empty_budgets);
        fabAddBudget = findViewById(R.id.fab_add_budget);

        // Setup currency formatter
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fil", "PH"));

        // Setup RecyclerView
        final BudgetAdapter adapter = new BudgetAdapter(currencyFormatter);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup ViewModel
        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        // Observe budgets with spending
        budgetViewModel.getAllBudgetsWithSpending().observe(this, budgetsWithSpending -> {
            // Update UI
            if (budgetsWithSpending != null && !budgetsWithSpending.isEmpty()) {
                adapter.submitList(budgetsWithSpending);
                recyclerView.setVisibility(View.VISIBLE);
                textEmptyBudgets.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                textEmptyBudgets.setVisibility(View.VISIBLE);
            }
        });

        // Setup FAB
        fabAddBudget.setOnClickListener(v -> {
            Intent intent = new Intent(BudgetActivity.this,     AddBudgetActivity.class);
            addBudgetLauncher.launch(intent);
        });
    }
} 