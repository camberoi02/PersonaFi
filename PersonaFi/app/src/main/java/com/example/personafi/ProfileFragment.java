package com.example.personafi;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private TextView textViewLevel;
    private TextView textViewXp;
    private TextView textViewXpToNextLevel;
    private TextView textViewNextLevel;
    private TextView textViewTotalSavings;
    private LinearProgressIndicator progressIndicator;
    private int currentXp = 0;
    private AnimatedShapesView animatedShapesView;
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String KEY_CURRENT_XP = "current_xp";
    private static ProfileFragment instance;
    private NumberFormat currencyFormatter;

    public static ProfileFragment getInstance() {
        if (instance == null) {
            instance = new ProfileFragment();
        }
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_profile, container, false);

        // Initialize currency formatter
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

        // Initialize and configure the animated shapes view
        animatedShapesView = view.findViewById(R.id.animatedShapesView);
        animatedShapesView.configureSavingsTheme();

        // Initialize views
        textViewLevel = view.findViewById(R.id.textViewLevel);
        textViewXp = view.findViewById(R.id.textViewXp);
        textViewXpToNextLevel = view.findViewById(R.id.textViewXpToNextLevel);
        textViewNextLevel = view.findViewById(R.id.textViewNextLevel);
        textViewTotalSavings = view.findViewById(R.id.textViewTotalSavings);
        progressIndicator = view.findViewById(R.id.progressIndicator);

        // Load saved XP
        loadXp();

        // Update the display
        updateLevelDisplay();
        updateTotalSavings();

        return view;
    }

    private void loadXp() {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            currentXp = prefs.getInt(KEY_CURRENT_XP, 0);
        }
    }

    private void saveXp() {
        if (getContext() != null) {
            SharedPreferences.Editor editor = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            editor.putInt(KEY_CURRENT_XP, currentXp);
            editor.apply();
        }
    }

    private void updateLevelDisplay() {
        int level = LevelManager.getLevelForXp(currentXp);
        int progress = LevelManager.getXpProgressForCurrentLevel(currentXp);
        int xpToNextLevel = LevelManager.getXpRemainingForNextLevel(currentXp);

        // Update level display
        textViewLevel.setText(String.valueOf(level));

        // Update XP display with formatted number
        textViewXp.setText(String.format("%,d XP", currentXp));

        // Animate progress indicator
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressIndicator, "progress", 0, progress);
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();

        // Update XP to next level and next level text
        if (LevelManager.isMaxLevel(currentXp)) {
            textViewXpToNextLevel.setText("MAX LEVEL REACHED");
            textViewNextLevel.setVisibility(View.GONE);
        } else {
            textViewXpToNextLevel.setText(String.format("%,d XP to next level", xpToNextLevel));
            textViewNextLevel.setText(String.format("Next: Level %d", level + 1));
            textViewNextLevel.setVisibility(View.VISIBLE);
        }
    }

    private void updateTotalSavings() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            List<SavingsGoal> savingsGoals = activity.getSavingsGoalList();
            double totalSavings = 0;
            
            for (SavingsGoal goal : savingsGoals) {
                totalSavings += goal.getCurrentAmount();
            }
            
            textViewTotalSavings.setText("₱" + NumberFormatter.formatCompactNumber(totalSavings));
        }
    }

    // Method to add XP (can be called from other parts of the app)
    public void addXp(int xp) {
        currentXp += xp;
        updateLevelDisplay();
        saveXp();
    }

    public int getCurrentXp() {
        return currentXp;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTotalSavings();
    }
} 