package com.example.personafi;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AchievementPopupDialog extends DialogFragment {
    private static final String ARG_ICON_RESOURCE = "icon_resource";

    public static AchievementPopupDialog newInstance(int iconResource) {
        AchievementPopupDialog dialog = new AchievementPopupDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_ICON_RESOURCE, iconResource);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_PersonaFi_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_achievement_popup, container, false);
        
        ImageView enlargedIcon = view.findViewById(R.id.enlargedAchievementIcon);
        if (getArguments() != null) {
            int iconResource = getArguments().getInt(ARG_ICON_RESOURCE);
            enlargedIcon.setImageResource(iconResource);
        }

        // Close dialog when clicking anywhere
        view.setOnClickListener(v -> dismiss());
        
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
    }
} 