package com.example.personifi.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.example.personifi.R;

/**
 * Utility class for showing customized toast messages
 */
public class ToastUtils {

    public enum ToastType {
        INFO,       // For general information
        SUCCESS,    // For successful operations
        WARNING,    // For warnings
        ERROR       // For errors
    }

    /**
     * Show a custom styled toast message
     *
     * @param context The context
     * @param message The message to display
     * @param type    The type of toast (determines style)
     */
    public static void showToast(Context context, String message, ToastType type) {
        // Inflate the custom toast layout
        View toastView = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);

        // Find the views
        CardView cardView = toastView.findViewById(R.id.toast_container);
        ImageView iconView = toastView.findViewById(R.id.toast_icon);
        TextView textView = toastView.findViewById(R.id.toast_text);

        // Set the text message
        textView.setText(message);

        // Configure appearance based on toast type
        switch (type) {
            case SUCCESS:
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.income));
                iconView.setImageResource(R.drawable.ic_success);
                break;
            case WARNING:
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.expense));
                iconView.setImageResource(R.drawable.ic_warning);
                break;
            case ERROR:
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.red));
                iconView.setImageResource(R.drawable.ic_error);
                break;
            case INFO:
            default:
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.primary));
                iconView.setImageResource(R.drawable.ic_info);
                break;
        }

        // Create and show the toast
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastView);
        toast.show();
    }

    /**
     * Show a success toast message
     */
    public static void showSuccess(Context context, String message) {
        showToast(context, message, ToastType.SUCCESS);
    }

    /**
     * Show an error toast message
     */
    public static void showError(Context context, String message) {
        showToast(context, message, ToastType.ERROR);
    }

    /**
     * Show a warning toast message
     */
    public static void showWarning(Context context, String message) {
        showToast(context, message, ToastType.WARNING);
    }

    /**
     * Show an info toast message
     */
    public static void showInfo(Context context, String message) {
        showToast(context, message, ToastType.INFO);
    }
} 