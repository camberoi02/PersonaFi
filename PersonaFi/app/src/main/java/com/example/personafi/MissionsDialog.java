package com.example.personafi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;

public class MissionsDialog extends DialogFragment {

    private Mission dailyMission;
    private Mission weeklyMission;

    public static MissionsDialog newInstance(Mission dailyMission, Mission weeklyMission) {
        MissionsDialog dialog = new MissionsDialog();
        dialog.dailyMission = dailyMission;
        dialog.weeklyMission = weeklyMission;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_PersonaFi_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_missions, container, false);

        // Set up close button
        ImageButton btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        // Set up daily mission views
        TextView dailyTitle = view.findViewById(R.id.dailyMissionTitle);
        TextView dailyDescription = view.findViewById(R.id.dailyMissionDescription);
        LinearProgressIndicator dailyProgress = view.findViewById(R.id.dailyMissionProgress);
        TextView dailyReward = view.findViewById(R.id.dailyMissionReward);

        dailyTitle.setText(dailyMission.getTitle());
        dailyDescription.setText(dailyMission.getDescription());
        dailyProgress.setProgress(dailyMission.getProgressPercentage());
        dailyReward.setText(String.format("Reward: %d XP", dailyMission.getXpReward()));

        // Set up weekly mission views
        TextView weeklyTitle = view.findViewById(R.id.weeklyMissionTitle);
        TextView weeklyDescription = view.findViewById(R.id.weeklyMissionDescription);
        LinearProgressIndicator weeklyProgress = view.findViewById(R.id.weeklyMissionProgress);
        TextView weeklyReward = view.findViewById(R.id.weeklyMissionReward);

        weeklyTitle.setText(weeklyMission.getTitle());
        weeklyDescription.setText(weeklyMission.getDescription());
        weeklyProgress.setProgress(weeklyMission.getProgressPercentage());
        weeklyReward.setText(String.format("Reward: %d XP", weeklyMission.getXpReward()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            // Get screen width
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            
            // Set dialog width to 85% of screen width
            int width = (int) (screenWidth * 0.85);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            
            getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
} 