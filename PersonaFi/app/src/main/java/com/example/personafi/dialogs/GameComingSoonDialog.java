package com.example.personafi.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.personafi.R;

public class GameComingSoonDialog extends Dialog {

    public GameComingSoonDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_game_coming_soon);

        // Set dialog width to 90% of screen width
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            int width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setLayout(width, android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // Set up the OK button
        Button buttonOk = findViewById(R.id.button_ok);
        buttonOk.setOnClickListener(v -> dismiss());
    }
} 