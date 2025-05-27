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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;
import java.util.List;
import android.os.Handler;
import android.os.Looper;

public class AchievementsListFragment extends Fragment {
    private RecyclerView recyclerViewAchievements;
    private AchievementAdapter achievementAdapter;
    private AchievementManager achievementManager;
    private AnimatedShapesView animatedShapesView;
    private ChipGroup filterChipGroup;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isViewCreated = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements_list, container, false);
        
        // Initialize and configure the animated shapes view
        animatedShapesView = view.findViewById(R.id.animatedShapesView);
        animatedShapesView.configureSavingsTheme();
        
        recyclerViewAchievements = view.findViewById(R.id.recyclerViewAchievements);
        recyclerViewAchievements.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize filter chips
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        setupFilterChips();
        
        // Initialize adapter with current achievements from AchievementManager
        if (getActivity() instanceof MainActivity) {
            achievementManager = ((MainActivity) getActivity()).getAchievementManager();
            if (achievementManager != null) {
                achievementAdapter = new AchievementAdapter(achievementManager.getAchievements(), getActivity());
                recyclerViewAchievements.setAdapter(achievementAdapter);
            }
        }
        
        isViewCreated = true;
        return view;
    }

    private void setupFilterChips() {
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (achievementAdapter != null) {
                if (checkedId == R.id.chipCompleted) {
                    achievementAdapter.setFilter(AchievementAdapter.FilterType.COMPLETED);
                } else if (checkedId == R.id.chipInProgress) {
                    achievementAdapter.setFilter(AchievementAdapter.FilterType.IN_PROGRESS);
                } else {
                    achievementAdapter.setFilter(AchievementAdapter.FilterType.ALL);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAchievements();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isAdded() && isViewCreated) {
            refreshAchievements();
        }
    }

    public void refreshAchievements() {
        if (!isAdded() || !isViewCreated) return;
        
        mainHandler.post(() -> {
            try {
                if (achievementManager != null && achievementAdapter != null) {
                    List<Achievement> currentAchievements = achievementManager.getAchievements();
                    achievementAdapter.updateAchievements(currentAchievements);
                    if (recyclerViewAchievements != null) {
                        recyclerViewAchievements.invalidate();
                        recyclerViewAchievements.requestLayout();
                        recyclerViewAchievements.scrollToPosition(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void updateAchievementsList(List<Achievement> achievements) {
        if (!isAdded() || !isViewCreated) return;
        
        mainHandler.post(() -> {
            try {
                if (achievementAdapter != null) {
                    achievementAdapter.updateAchievements(achievements);
                    if (recyclerViewAchievements != null) {
                        recyclerViewAchievements.invalidate();
                        recyclerViewAchievements.requestLayout();
                        recyclerViewAchievements.scrollToPosition(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
} 