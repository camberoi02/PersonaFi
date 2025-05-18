package com.example.personifi;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TransactionViewModel transactionViewModel;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private LinearLayout emptyView;
    private TextView textTotalIncome;
    private TextView textTotalExpenses;
    private TextView textBalance;
    private NumberFormat currencyFormatter;
    private BottomNavigationView bottomNavigationView;
    private ProgressBar progressSpending;
    private ImageView imageBalanceIndicator;

    // Activity result launcher for adding a transaction
    private final ActivityResultLauncher<Intent> addTransactionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    // Create transaction from result data
                    double amount = data.getDoubleExtra("TRANSACTION_AMOUNT", 0);
                    String category = data.getStringExtra("TRANSACTION_CATEGORY");
                    String description = data.getStringExtra("TRANSACTION_DESCRIPTION");
                    long dateMillis = data.getLongExtra("TRANSACTION_DATE", System.currentTimeMillis());
                    int typeOrdinal = data.getIntExtra("TRANSACTION_TYPE", 0);
                    Transaction.TransactionType type = Transaction.TransactionType.values()[typeOrdinal];
                    
                    // Create and insert transaction
                    Transaction transaction = new Transaction(amount, category, description, new Date(dateMillis), type);
                    transactionViewModel.insert(transaction);
                    
                    Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Handle edge to edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        recyclerView = findViewById(R.id.recyclerview_transactions);
        emptyView = findViewById(R.id.empty_view);
        textTotalIncome = findViewById(R.id.text_total_income);
        textTotalExpenses = findViewById(R.id.text_total_expenses);
        textBalance = findViewById(R.id.text_balance);
        progressSpending = findViewById(R.id.progress_spending);
        imageBalanceIndicator = findViewById(R.id.image_balance_indicator);
        FloatingActionButton fabAddGoal = findViewById(R.id.fab_add_goal);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Setup currency formatter
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fil", "PH"));

        // Setup RecyclerView
        adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup ViewModel
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Observe transactions
        transactionViewModel.getAllTransactions().observe(this, transactions -> {
            // Update RecyclerView
            adapter.submitList(transactions);
            
            // Show empty state if no transactions
            if (transactions == null || transactions.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });

        // Observe total income
        transactionViewModel.getTotalIncome().observe(this, totalIncome -> {
            if (totalIncome != null) {
                textTotalIncome.setText(currencyFormatter.format(totalIncome));
                updateBalance();
            } else {
                textTotalIncome.setText(currencyFormatter.format(0));
            }
        });

        // Observe total expenses
        transactionViewModel.getTotalExpenses().observe(this, totalExpenses -> {
            if (totalExpenses != null) {
                textTotalExpenses.setText(currencyFormatter.format(totalExpenses));
                updateBalance();
            } else {
                textTotalExpenses.setText(currencyFormatter.format(0));
            }
        });

        // Setup floating action button for adding goals
        fabAddGoal.setOnClickListener(view -> {
            // Add click animation
            android.view.animation.Animation pulseAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fab_pulse);
            view.startAnimation(pulseAnimation);
            
            // Get the current selected tab ID
            int selectedItemId = bottomNavigationView.getSelectedItemId();

            // Perform different actions depending on the selected tab
            if (selectedItemId == R.id.navigation_home) {
                // Savings tab: Add new savings goal
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof SavingsFragment) {
                    ((SavingsFragment) currentFragment).showAddSavingsGoalDialog();
                }
            } 
            else if (selectedItemId == R.id.navigation_dashboard) {
                // Dashboard tab: Open analytics or add category
                Toast.makeText(MainActivity.this, "Add category feature coming soon", Toast.LENGTH_SHORT).show();
            }
            else if (selectedItemId == R.id.navigation_budgets) {
                // Budgets tab: Add new budget
                Toast.makeText(MainActivity.this, "Add budget feature coming soon", Toast.LENGTH_SHORT).show();
            }
            else if (selectedItemId == R.id.navigation_transactions) {
                // Transactions tab: Add transaction
                Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                addTransactionLauncher.launch(intent);
            }
        });

        // Setup bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            // Check if the middle add goal item was clicked, which we want to ignore for navigation
            if (itemId == R.id.navigation_add_goal) {
                // Trigger the center button action without changing the selected tab
                fabAddGoal.performClick();
                // Return false to prevent selecting this item
                return false;
            }
            
            // Update the FAB icon based on the selected tab
            updateFabIcon(itemId, fabAddGoal);
            
            // Hide the home content (financial summary) by default
            findViewById(R.id.home_content).setVisibility(View.GONE);
            // Hide the fragment container by default
            findViewById(R.id.fragment_container).setVisibility(View.GONE);
            // Control FAB visibility - only show on home tab
            FloatingActionButton fab = findViewById(R.id.fab_add_transaction);
            fab.setVisibility(itemId == R.id.navigation_home ? View.VISIBLE : View.GONE);
            
            // Never hide the add goal button, as it's part of the bottom navigation
            
            if (itemId == R.id.navigation_home) {
                // Show savings fragment with a slight delay to prevent flickering during initial load
                findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
                
                // Add slight delay before loading the fragment to avoid flickering
                new android.os.Handler().postDelayed(() -> {
                    loadFragment(new SavingsFragment());
                }, 150); // Short delay to allow UI to stabilize
                
                return true;
            } else if (itemId == R.id.navigation_dashboard) {
                // Show dashboard fragment
                loadFragment(new DashboardFragment());
                findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
                return true;
            } else if (itemId == R.id.navigation_budgets) {
                // Show budgets fragment
                loadFragment(new BudgetsFragment());
                findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
                return true;
            } else if (itemId == R.id.navigation_transactions) {
                // Show home fragment and financial summary
                // Show financial summary for transactions tab
                findViewById(R.id.home_content).setVisibility(View.VISIBLE);
                
                // Load the HomeFragment into transactions_container
                HomeFragment transactionsFragment = new HomeFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.transactions_container, transactionsFragment)
                        .commit();
                
                return true;
            }
            return false;
        });

        // Set active menu item
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        // Update FAB icon based on the selected tab
        updateFabIcon(R.id.navigation_home, fabAddGoal);

        // Add listener to update FAB icon when tab changes
        bottomNavigationView.setOnItemReselectedListener(item -> {
            // Do nothing on reselection to prevent fragment refresh
        });

        // Setup item click handler for editing transactions
        adapter.setOnItemClickListener(transaction -> {
            // TODO: Implement edit transaction functionality
            Toast.makeText(MainActivity.this, "Edit transaction: " + transaction.getDescription(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Update the FAB icon based on the selected tab
     */
    private void updateFabIcon(int selectedItemId, FloatingActionButton fab) {
        try {
            // Set a default icon in case any of these drawable resources don't exist
            fab.setImageResource(R.drawable.ic_add);
            
            // Update the label text in the navigation menu to match current function
            Menu menu = bottomNavigationView.getMenu();
            MenuItem addItem = menu.findItem(R.id.navigation_add_goal);
            
            // Try to set specific icons based on the tab
            if (selectedItemId == R.id.navigation_home) {
                fab.setImageResource(R.drawable.ic_add);
                if (addItem != null) addItem.setTitle("Add Goal");
            } else if (selectedItemId == R.id.navigation_dashboard) {
                fab.setImageResource(R.drawable.ic_add);
                if (addItem != null) addItem.setTitle("Add Category");
            } else if (selectedItemId == R.id.navigation_budgets) {
                fab.setImageResource(R.drawable.ic_add);
                if (addItem != null) addItem.setTitle("Add Budget");
            } else if (selectedItemId == R.id.navigation_transactions) {
                fab.setImageResource(R.drawable.ic_add);
                if (addItem != null) addItem.setTitle("Add Transaction");
            }
        } catch (Exception e) {
            // If any issues with resources, default to the standard add icon
            fab.setImageResource(android.R.drawable.ic_input_add);
        }
    }

    /**
     * Loads a fragment into the fragment container
     */
    private void loadFragment(Fragment fragment) {
        if (fragment instanceof HomeFragment) {
            // For home fragment, don't load it into the main fragment container
            // as we're handling it separately to resolve the layout issue
            return;
        }
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Update the balance display
     */
    private void updateBalance() {
        Double income = 0.0;
        Double expenses = 0.0;
        
        try {
            // Parse income
            String incomeText = textTotalIncome.getText().toString().replace("₱", "")
                    .replace(",", "");
            if (!incomeText.isEmpty()) {
                income = Double.parseDouble(incomeText);
            }
            
            // Parse expenses
            String expensesText = textTotalExpenses.getText().toString().replace("₱", "")
                    .replace(",", "");
            if (!expensesText.isEmpty()) {
                expenses = Double.parseDouble(expensesText);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        // Calculate and display balance
        double balance = income - expenses;
        textBalance.setText(currencyFormatter.format(balance));
        
        // Set color based on positive or negative balance
        if (balance >= 0) {
            textBalance.setTextColor(getResources().getColor(R.color.success));
            imageBalanceIndicator.setImageResource(R.drawable.ic_peso);
        } else {
            textBalance.setTextColor(getResources().getColor(R.color.error));
            imageBalanceIndicator.setImageResource(R.drawable.ic_peso);
        }
        
        // Update progress bar
        if (income > 0) {
            int progress = Math.min(100, (int)(100 - (expenses / income) * 100));
            progressSpending.setProgress(progress);
        } else {
            progressSpending.setProgress(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.menu_dashboard) {
            // Navigate to Dashboard
            bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
            return true;
        } else if (id == R.id.menu_budgets) {
            // Navigate to Budgets
            bottomNavigationView.setSelectedItemId(R.id.navigation_budgets);
            return true;
        } else if (id == R.id.menu_export) {
            // Export data
            exportData();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Exports transaction data to a CSV file
     */
    private void exportData() {
        transactionViewModel.getAllTransactions().observe(this, transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                // Create file
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String timestamp = dateFormat.format(new Date());
                File exportDir = new File(getExternalFilesDir(null), "PersoniFi");
                
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                
                File exportFile = new File(exportDir, "transactions_" + timestamp + ".csv");
                FileWriter writer = new FileWriter(exportFile);
                
                // Write header
                writer.append("ID,Type,Amount,Category,Description,Date\n");
                
                // Write transactions
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                for (Transaction transaction : transactions) {
                    writer.append(String.valueOf(transaction.getId())).append(",");
                    writer.append(transaction.getType().toString()).append(",");
                    writer.append(String.valueOf(transaction.getAmount())).append(",");
                    writer.append(transaction.getCategory()).append(",");
                    writer.append("\"").append(transaction.getDescription().replace("\"", "\"\"")).append("\",");
                    writer.append(outputDateFormat.format(transaction.getDate())).append("\n");
                }
                
                writer.close();
                
                Toast.makeText(this, "Exported to " + exportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}