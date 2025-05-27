package com.example.personafi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class MissionsListFragment extends Fragment implements MissionManager.OnMissionProgressUpdateListener {
    private MissionManager missionManager;
    private MaterialCardView dailyMissionLayout;
    private MaterialCardView weeklyMissionLayout;
    private TextView dailyMissionTitle;
    private TextView dailyMissionDescription;
    private TextView dailyMissionReward;
    private LinearProgressIndicator dailyMissionProgress;
    private TextView weeklyMissionTitle;
    private TextView weeklyMissionDescription;
    private TextView weeklyMissionReward;
    private LinearProgressIndicator weeklyMissionProgress;
    private AnimatedShapesView animatedShapesView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_missions_list, container, false);

        // Initialize and configure the animated shapes view
        animatedShapesView = view.findViewById(R.id.animatedShapesView);
        animatedShapesView.configureSavingsTheme();

        // Initialize views
        dailyMissionLayout = view.findViewById(R.id.dailyMissionLayout);
        weeklyMissionLayout = view.findViewById(R.id.weeklyMissionLayout);
        dailyMissionTitle = view.findViewById(R.id.dailyMissionTitle);
        dailyMissionDescription = view.findViewById(R.id.dailyMissionDescription);
        dailyMissionReward = view.findViewById(R.id.dailyMissionReward);
        dailyMissionProgress = view.findViewById(R.id.dailyMissionProgress);
        weeklyMissionTitle = view.findViewById(R.id.weeklyMissionTitle);
        weeklyMissionDescription = view.findViewById(R.id.weeklyMissionDescription);
        weeklyMissionReward = view.findViewById(R.id.weeklyMissionReward);
        weeklyMissionProgress = view.findViewById(R.id.weeklyMissionProgress);

        // Initialize MissionManager and register for updates
        missionManager = MissionManager.getInstance();
        missionManager.addProgressListener(this);
        
        // Update missions UI
        updateMissionsUI();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMissionsUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister from mission updates when the fragment is destroyed
        if (missionManager != null) {
            missionManager.removeProgressListener(this);
        }
    }

    @Override
    public void onMissionProgressUpdated() {
        // Update the UI when mission progress changes
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(this::updateMissionsUI);
        }
    }

    private void updateMissionsUI() {
        if (missionManager != null) {
            Mission dailyMission = missionManager.getCurrentDailyMission();
            Mission weeklyMission = missionManager.getCurrentWeeklyMission();

            if (dailyMission != null) {
                dailyMissionTitle.setText(dailyMission.getTitle());
                dailyMissionDescription.setText(dailyMission.getDescription());
                dailyMissionReward.setText(String.format("Reward: %d XP", dailyMission.getXpReward()));
                dailyMissionProgress.setProgress(dailyMission.getProgressPercentage());
            }

            if (weeklyMission != null) {
                weeklyMissionTitle.setText(weeklyMission.getTitle());
                weeklyMissionDescription.setText(weeklyMission.getDescription());
                weeklyMissionReward.setText(String.format("Reward: %d XP", weeklyMission.getXpReward()));
                weeklyMissionProgress.setProgress(weeklyMission.getProgressPercentage());
            }
        }
    }
} 