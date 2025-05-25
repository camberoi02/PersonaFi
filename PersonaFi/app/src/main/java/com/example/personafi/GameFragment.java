package com.example.personafi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personafi.adapters.AchievementAdapter;
import com.example.personafi.models.Achievement;
import java.util.ArrayList;
import java.util.List;

public class GameFragment extends Fragment {
    private View view;
    private RecyclerView achievementsRecycler;
    private AchievementAdapter achievementAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_game, container, false);
        initializeViews();
        setupRecyclerViews();
        loadAchievements();
        return view;
    }
    private void initializeViews() {
        achievementsRecycler = view.findViewById(R.id.achievements_recycler);
    }

    private void setupRecyclerViews() {
        // Set up achievements RecyclerView
        achievementsRecycler.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        achievementAdapter = new AchievementAdapter();
        achievementsRecycler.setAdapter(achievementAdapter);
    }

    private void loadAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        // Add achievements
        achievements.add(new Achievement(R.drawable.achievement, "Save 1000 pesos", false));
        achievements.add(new Achievement(R.drawable.achievement1, "Save 5000 pesos", false));
        achievements.add(new Achievement(R.drawable.achievement2, "Reach 50% of savings goal", false));
        achievements.add(new Achievement(R.drawable.achievement3, "Complete 1st saving goal", false));
        achievements.add(new Achievement(R.drawable.achievement4, "7 days streak", false));
        achievements.add(new Achievement(R.drawable.achievement5, "1 month streak", false));
        achievements.add(new Achievement(R.drawable.achievement6, "Complete 10 daily missions", false));
        achievements.add(new Achievement(R.drawable.achievement7, "Complete 5 weekly missions", false));
        achievements.add(new Achievement(R.drawable.achievement8, "Complete 5th saving goal", false));
        achievementAdapter.setAchievements(achievements);
    }
} 