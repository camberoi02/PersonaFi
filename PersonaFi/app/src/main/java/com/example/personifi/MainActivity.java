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
    private View homeContent;
    private View fragmentContainer;

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
        try {
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
            homeContent = findViewById(R.id.home_content);
            fragmentContainer = findViewById(R.id.fragment_container);

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
                    // Home tab: Add new savings goal
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (currentFragment instanceof SavingsFragment) {
                        ((SavingsFragment) currentFragment).showAddSavingsGoalDialog();
                    }
                } 
                else if (selectedItemId == R.id.navigation_dashboard) {
                    // Dashboard tab: Open analytics or add category
                    Toast.makeText(MainActivity.this, "Add category feature coming soon", Toast.LENGTH_SHORT).show();
                }
                else if (selectedItemId == R.id.navigation_transactions) {
                    // Transactions tab: Add transaction
                    Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                    addTransactionLauncher.launch(intent);
                }
            });

            // Setup bottom navigation
            setupNavigation();

            // Set active menu item
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);

            // Setup item click handler for editing transactions
            adapter.setOnItemClickListener(transaction -> {
                // TODO: Implement edit transaction functionality
                Toast.makeText(MainActivity.this, "Edit transaction: " + transaction.getDescription(), Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int selectedItemId = item.getItemId();
            
            if (selectedItemId == R.id.navigation_home) {
                showHomeFragment();
                return true;
            }
            else if (selectedItemId == R.id.navigation_transactions) {
                showTransactionsFragment();
                return true;
            }
            else if (selectedItemId == R.id.navigation_game) {
                showGameFragment();
                return true;
            }
            else if (selectedItemId == R.id.navigation_dashboard) {
                showDashboardFragment();
                return true;
            }
            
            return false;
        });
    }

    private void showHomeFragment() {
        homeContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SavingsFragment())
                .commit();
    }

    private void showTransactionsFragment() {
        homeContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new IncomeExpensesFragment())
                .commit();
    }

    private void showGameFragment() {
        homeContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new GameFragment())
                .commit();
    }

    private void showDashboardFragment() {
        homeContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DashboardFragment())
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
        } else if (id == R.id.menu_transactions) {
            // Navigate to Transactions
            bottomNavigationView.setSelectedItemId(R.id.navigation_transactions);
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