package com.example.personifi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class GameFragment extends Fragment {
    private ProgressBar levelProgress;
    private TextView levelText;
    private RecyclerView achievementsRecycler;
    private RecyclerView missionsRecycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        levelProgress = view.findViewById(R.id.level_progress);
        levelText = view.findViewById(R.id.level_text);
        achievementsRecycler = view.findViewById(R.id.achievements_recycler);
        missionsRecycler = view.findViewById(R.id.missions_recycler);

        setupLevelProgress();
        setupRecyclerViews();
    }

    private void setupLevelProgress() {
        // TODO: Load user's level progress from database
        levelProgress.setProgress(75);
        levelText.setText("Level 5 - 75% to next level");
    }

    private void setupRecyclerViews() {
        // TODO: Setup adapters for achievements and missions
    }
} 