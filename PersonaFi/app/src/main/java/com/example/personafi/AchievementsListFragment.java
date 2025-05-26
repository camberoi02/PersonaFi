package com.example.personafi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.os.Handler;
import android.os.Looper;

public class AchievementsListFragment extends Fragment {
    private RecyclerView recyclerViewAchievements;
    private AchievementAdapter achievementAdapter;
    private AchievementManager achievementManager;
    private AnimatedShapesView animatedShapesView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements_list, container, false);
        
        // Initialize and configure the animated shapes view
        animatedShapesView = view.findViewById(R.id.animatedShapesView);
        animatedShapesView.configureSavingsTheme();
        
        recyclerViewAchievements = view.findViewById(R.id.recyclerViewAchievements);
        recyclerViewAchievements.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize adapter with current achievements from AchievementManager
        if (getActivity() instanceof MainActivity) {
            achievementManager = ((MainActivity) getActivity()).getAchievementManager();
            if (achievementManager != null) {
                achievementAdapter = new AchievementAdapter(achievementManager.getAchievements(), getActivity());
                recyclerViewAchievements.setAdapter(achievementAdapter);
            }
        }
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAchievements();
    }

    private void refreshAchievements() {
        // Update achievements when fragment becomes visible
        if (achievementManager != null && achievementAdapter != null && isAdded()) {
            mainHandler.post(() -> {
                List<Achievement> currentAchievements = achievementManager.getAchievements();
                achievementAdapter.updateAchievements(currentAchievements);
                recyclerViewAchievements.scrollToPosition(0); // Scroll to top to ensure visibility
            });
        }
    }

    public void updateAchievementsList(List<Achievement> achievements) {
        if (achievementAdapter != null && isAdded()) {
            mainHandler.post(() -> {
                achievementAdapter.updateAchievements(achievements);
                recyclerViewAchievements.scrollToPosition(0); // Scroll to top to ensure visibility
            });
        }
    }
} 