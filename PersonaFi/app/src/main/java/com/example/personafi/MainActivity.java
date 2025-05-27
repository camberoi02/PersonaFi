package com.example.personafi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class MainActivity extends AppCompatActivity implements SavingsGoalsFragment.OnGoalInteractionListener {

    private static final String PREFS_NAME = "PersonaFiPrefs";
    private static final String SAVINGS_GOALS_KEY = "savings_goals";
    private static final String ACHIEVEMENTS_KEY = "achievements";
    private static final String TUTORIAL_COMPLETED_KEY = "tutorial_completed";
    
    private AchievementManager achievementManager;
    private List<SavingsGoal> savingsGoalList = new ArrayList<>();
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAddGoal;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if tutorial needs to be shown
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean tutorialCompleted = prefs.getBoolean(TUTORIAL_COMPLETED_KEY, false);
        
        if (!tutorialCompleted) {
            startActivity(new Intent(this, TutorialActivity.class));
            finish();
            return;
        }
        
        gson = new Gson();
        
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabAddGoal = findViewById(R.id.fab_add_goal);

        achievementManager = new AchievementManager(this);
        MissionManager.initialize(this);

        if (savingsGoalList == null) {
            savingsGoalList = new ArrayList<>();
        }

        loadSavingsGoals();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int navigationBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            
            viewPager.setPadding(0, statusBarHeight, 0, 0);
            
            ViewGroup.MarginLayoutParams fabParams = (ViewGroup.MarginLayoutParams) fabAddGoal.getLayoutParams();
            fabParams.bottomMargin = navigationBarHeight + (int) (16 * getResources().getDisplayMetrics().density);
            fabAddGoal.setLayoutParams(fabParams);
            
            return windowInsets;
        });

        setupViewPager();

        checkAllAchievements();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewPager != null && pageChangeCallback != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    private void loadAchievements() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String achievementsJson = prefs.getString(ACHIEVEMENTS_KEY, null);
        if (achievementsJson != null) {
            Type type = new TypeToken<List<Achievement>>(){}.getType();
            List<Achievement> savedAchievements = gson.fromJson(achievementsJson, type);
            if (savedAchievements != null) {
                for (Achievement savedAchievement : savedAchievements) {
                    Achievement currentAchievement = achievementManager.getAchievementByName(savedAchievement.getName());
                    if (currentAchievement != null) {
                        currentAchievement.setUnlocked(savedAchievement.isUnlocked());
                    }
                }
            }
        }
    }

    private void saveAchievements() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String achievementsJson = gson.toJson(achievementManager.getAchievements());
        prefs.edit().putString(ACHIEVEMENTS_KEY, achievementsJson).apply();
    }

    private void checkAllAchievements() {
        loadAchievements();
        
        int totalGoals = savingsGoalList.size();
        int completedGoals = 0;
        double totalProgress = 0;
        
        for (SavingsGoal goal : savingsGoalList) {
            if (goal.getCurrentAmount() >= goal.getTargetAmount()) {
                completedGoals++;
            }
            totalProgress += goal.getProgress();
        }
        
        double averageProgress = totalGoals > 0 ? totalProgress / totalGoals : 0;
        
        // Check achievements
        if (totalGoals >= 1 && achievementManager.unlockAchievement(AchievementManager.ACHIEVEMENT_FIRST_GOAL_CREATED)) {
            showAchievementToast(AchievementManager.ACHIEVEMENT_FIRST_GOAL_CREATED);
        }
        if (totalGoals >= 3 && achievementManager.unlockAchievement(AchievementManager.ACHIEVEMENT_GOAL_GETTER)) {
            showAchievementToast(AchievementManager.ACHIEVEMENT_GOAL_GETTER);
        }
        if (completedGoals >= 1 && achievementManager.unlockAchievement(AchievementManager.ACHIEVEMENT_GOAL_MASTER)) {
            showAchievementToast(AchievementManager.ACHIEVEMENT_GOAL_MASTER);
        }
        if (completedGoals >= 1 && achievementManager.unlockAchievement(AchievementManager.ACHIEVEMENT_GOAL_GETTER)) {
            showAchievementToast(AchievementManager.ACHIEVEMENT_GOAL_GETTER);
        }
        if (completedGoals >= 3 && achievementManager.unlockAchievement(AchievementManager.ACHIEVEMENT_SAVER_PRO)) {
            showAchievementToast(AchievementManager.ACHIEVEMENT_SAVER_PRO);
        }
        if (averageProgress >= 0.5 && achievementManager.unlockAchievement(AchievementManager.ACHIEVEMENT_HALF_WAY_THERE)) {
            showAchievementToast(AchievementManager.ACHIEVEMENT_HALF_WAY_THERE);
        }
        
        // Check total savings achievements
        achievementManager.checkTotalSavedAchievements(savingsGoalList);
        
        saveAchievements();
    }

    private void showAchievementToast(String achievementName) {
        CustomToast.showSuccess(this, "Achievement Unlocked: " + achievementName);
    }

    @Override
    public void onGoalAddedOrProgressUpdated() {
        saveSavingsGoals();
        checkAllAchievements();
        
        // Force reload the main view
        runOnUiThread(() -> {
            // Force reload the ViewPager first
            viewPager.setAdapter(null);
            viewPager.setAdapter(viewPagerAdapter);
            
            // Then refresh the fragments
            viewPagerAdapter.refreshFragment(1); // Refresh achievements first
            viewPagerAdapter.refreshFragment(0); // Then refresh savings
            
            // Finally set the current item
            viewPager.setCurrentItem(0, false);
        });
    }

    @Override
    public List<SavingsGoal> getSavingsGoalList() {
        return savingsGoalList;
    }

    @Override
    public void setSavingsGoalList(List<SavingsGoal> goals) {
        this.savingsGoalList = goals;
        saveSavingsGoals();
    }

    public void showProfileMenu() {
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.navigation_profile));
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_settings) {
                // Handle settings
                return true;
            } else if (itemId == R.id.menu_about) {
                // Handle about
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_settings) {
            // Handle settings
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSavingsGoals() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String goalsJson = prefs.getString(SAVINGS_GOALS_KEY, null);
        if (goalsJson != null) {
            Type type = new TypeToken<List<SavingsGoal>>(){}.getType();
            List<SavingsGoal> savedGoals = gson.fromJson(goalsJson, type);
            if (savedGoals != null) {
                savingsGoalList = savedGoals;
            }
        }
    }

    private void saveSavingsGoals() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String goalsJson = gson.toJson(savingsGoalList);
        prefs.edit().putString(SAVINGS_GOALS_KEY, goalsJson).apply();
    }

    private void setupViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setUserInputEnabled(false); // Disable swipe between tabs
        viewPager.setOffscreenPageLimit(3); // Keep all fragments in memory

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_savings) {
                viewPager.setCurrentItem(0);
                fabAddGoal.show();
                viewPagerAdapter.refreshFragment(0);
                return true;
            } else if (itemId == R.id.navigation_achievements) {
                viewPager.setCurrentItem(1);
                fabAddGoal.hide();
                // Use post to ensure the page change is complete before refreshing
                viewPager.post(() -> {
                    viewPagerAdapter.refreshFragment(1);
                });
                return true;
            } else if (itemId == R.id.navigation_profile) {
                viewPager.setCurrentItem(2);
                fabAddGoal.hide();
                return true;
            }
            return false;
        });

        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigation.setSelectedItemId(R.id.navigation_savings);
                        fabAddGoal.show();
                        viewPagerAdapter.refreshFragment(0);
                        break;
                    case 1:
                        bottomNavigation.setSelectedItemId(R.id.navigation_achievements);
                        fabAddGoal.hide();
                        // Use post to ensure the page change is complete before refreshing
                        viewPager.post(() -> {
                            viewPagerAdapter.refreshFragment(1);
                        });
                        break;
                    case 2:
                        bottomNavigation.setSelectedItemId(R.id.navigation_profile);
                        fabAddGoal.hide();
                        break;
                }
            }
        };
        viewPager.registerOnPageChangeCallback(pageChangeCallback);

        setupFabAddGoal();
    }

    private void setupFabAddGoal() {
        fabAddGoal.setOnClickListener(view -> {
            if (viewPager.getCurrentItem() == 0) {
                Fragment currentFragment = viewPagerAdapter.getFragmentAtPosition(0);
                if (currentFragment instanceof SavingsGoalsFragment) {
                    ((SavingsGoalsFragment) currentFragment).showAddGoalDialog();
                } else {
                    // If fragment is not created yet, create and show it
                    Fragment fragment = viewPagerAdapter.createFragment(0);
                    if (fragment instanceof SavingsGoalsFragment) {
                        getSupportFragmentManager()
                            .beginTransaction()
                            .add(fragment, "f0")
                            .commitNow();
                        ((SavingsGoalsFragment) fragment).showAddGoalDialog();
                    }
                }
            }
        });
    }
}