package com.example.personafi;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.personafi.dialogs.GameComingSoonDialog;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;
    private FloatingActionButton fabAddGoal;

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
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            fragmentContainer = findViewById(R.id.fragment_container);
            fabAddGoal = findViewById(R.id.fab_add_goal);

            // Setup bottom navigation
            setupNavigation();

            // Set active menu item
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);

            // Setup FAB click listener
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
                    android.widget.Toast.makeText(MainActivity.this, "Add category feature coming soon", android.widget.Toast.LENGTH_SHORT).show();
                }
                else if (selectedItemId == R.id.navigation_transactions) {
                    // Transactions tab: Add transaction
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, AddTransactionActivity.class);
                    startActivity(intent);
                }
                else if (selectedItemId == R.id.navigation_game) {
                    // Game tab: Show coming soon dialog
                    GameComingSoonDialog dialog = new GameComingSoonDialog(this);
                    dialog.show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Error initializing app: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
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
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SavingsFragment())
                .commit();
    }

    private void showTransactionsFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new IncomeExpensesFragment())
                .commit();
    }

    private void showGameFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new GameFragment())
                .commit();
    }

    private void showDashboardFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DashboardFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_export) {
            exportData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportData() {
        // TODO: Implement data export functionality
        android.widget.Toast.makeText(this, "Export feature coming soon", android.widget.Toast.LENGTH_SHORT).show();
    }
}