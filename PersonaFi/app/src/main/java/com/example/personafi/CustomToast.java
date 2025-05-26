package com.example.personafi;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

public class CustomToast {
    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_INFO = 3;

    private static Toast currentToast;

    public static void show(Context context, String message, int type) {
        try {
            // Cancel any existing toast
            if (currentToast != null) {
                currentToast.cancel();
            }

            // Inflate custom layout
            View layout = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
            TextView messageView = layout.findViewById(R.id.toastMessage);
            ImageView iconView = layout.findViewById(R.id.toastIcon);

            // Set message
            messageView.setText(message);

            // Configure icon and colors based on type
            @DrawableRes int iconRes;
            int iconTint;
            switch (type) {
                case TYPE_SUCCESS:
                    iconRes = R.drawable.ic_check_circle;
                    iconTint = context.getColor(R.color.primary);
                    break;
                case TYPE_ERROR:
                    iconRes = R.drawable.ic_error;
                    iconTint = context.getColor(R.color.error);
                    break;
                case TYPE_INFO:
                default:
                    iconRes = R.drawable.ic_info;
                    iconTint = context.getColor(R.color.primary);
                    break;
            }

            // Set icon and make it visible
            iconView.setImageResource(iconRes);
            iconView.setImageTintList(ColorStateList.valueOf(iconTint));
            iconView.setVisibility(View.VISIBLE);

            // Create and show toast
            currentToast = new Toast(context);
            currentToast.setDuration(Toast.LENGTH_SHORT);
            currentToast.setView(layout);
            currentToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 64);
            currentToast.show();
        } catch (Exception e) {
            // Fallback to standard toast if custom toast fails
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showSuccess(Context context, String message) {
        show(context, message, TYPE_SUCCESS);
    }

    public static void showError(Context context, String message) {
        show(context, message, TYPE_ERROR);
    }

    public static void showInfo(Context context, String message) {
        show(context, message, TYPE_INFO);
    }
} 