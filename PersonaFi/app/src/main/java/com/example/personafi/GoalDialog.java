package com.example.personafi;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.NumberFormat;
import java.util.Locale;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;

public class GoalDialog extends DialogFragment {
    private static final String KEY_NAME = "goal_name";
    private static final String KEY_AMOUNT = "goal_amount";
    private static final String KEY_POSITION = "goal_position";
    
    private TextInputLayout textFieldGoalNameLayout;
    private TextInputLayout textFieldTargetAmountLayout;
    private TextInputEditText editTextGoalName;
    private TextInputEditText editTextTargetAmount;
    private TextView dialogTitle;
    private TextView dialogSubtitle;
    private Button buttonSave;
    private Button buttonCancel;

    private String goalName;
    private double targetAmount;
    private int position = -1;
    private boolean isEdit = false;

    private String parseFormattedNumber(String input) {
        if (input == null || input.isEmpty()) {
            return "0";
        }
        // Remove all non-digit characters except decimal point
        return input.replaceAll("[^\\d.]", "");
    }

    private double parseAmount(String input) {
        try {
            String cleanInput = parseFormattedNumber(input);
            return Double.parseDouble(cleanInput);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public interface OnGoalSetListener {
        void onGoalSet(String name, double targetAmount, int position);
    }

    private OnGoalSetListener listener;

    public static GoalDialog newInstance(String goalName, double targetAmount, int position) {
        GoalDialog dialog = new GoalDialog();
        Bundle args = new Bundle();
        args.putString("goalName", goalName);
        args.putDouble("targetAmount", targetAmount);
        args.putInt("position", position);
        dialog.setArguments(args);
        return dialog;
    }

    public static GoalDialog newInstance() {
        return new GoalDialog();
    }

    public void setOnGoalSetListener(OnGoalSetListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (editTextGoalName != null && editTextTargetAmount != null) {
            outState.putString(KEY_NAME, editTextGoalName.getText().toString());
            outState.putString(KEY_AMOUNT, editTextTargetAmount.getText().toString());
            outState.putInt(KEY_POSITION, position);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_PersonaFi_Dialog);

        if (getArguments() != null) {
            goalName = getArguments().getString("goalName", "");
            targetAmount = getArguments().getDouble("targetAmount", 0);
            position = getArguments().getInt("position", -1);
            isEdit = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_goal, container, false);

        dialogTitle = view.findViewById(R.id.dialogTitle);
        dialogSubtitle = view.findViewById(R.id.dialogSubtitle);
        textFieldGoalNameLayout = view.findViewById(R.id.textFieldGoalNameLayout);
        textFieldTargetAmountLayout = view.findViewById(R.id.textFieldTargetAmountLayout);
        editTextGoalName = view.findViewById(R.id.editTextGoalName);
        editTextTargetAmount = view.findViewById(R.id.editTextTargetAmount);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonCancel = view.findViewById(R.id.buttonCancel);

        // Add number formatting to target amount input
        editTextTargetAmount.addTextChangedListener(new NumberFormattingTextWatcher(editTextTargetAmount));

        // Set up the dialog
        dialogTitle.setText(isEdit ? "Edit Goal" : "New Savings Goal");
        dialogSubtitle.setText(isEdit ? 
            "Update your goal details below" : 
            "Set a target amount and track your progress");
        buttonSave.setText(isEdit ? "Update" : "Create Goal");

        if (savedInstanceState != null) {
            editTextGoalName.setText(savedInstanceState.getString(KEY_NAME));
            editTextTargetAmount.setText(savedInstanceState.getString(KEY_AMOUNT));
        } else if (goalName != null && targetAmount > 0) {
            editTextGoalName.setText(goalName);
            editTextTargetAmount.setText(String.format(Locale.getDefault(), "%.2f", targetAmount));
        }

        // Add text watchers to clear errors as user types
        editTextGoalName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textFieldGoalNameLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editTextTargetAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textFieldTargetAmountLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonSave.setOnClickListener(v -> {
            if (isInputValid()) {
                String name = editTextGoalName.getText().toString().trim();
                String amountStr = editTextTargetAmount.getText().toString().trim();
                double amount = parseAmount(amountStr);
                
                if (listener != null) {
                    listener.onGoalSet(name, amount, position);
                }
                
                dismissDialog();
            }
        });

        buttonCancel.setOnClickListener(v -> dismissDialog());

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            
            // Set dialog to not be cancelable
            getDialog().setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);
        }
    }

    private boolean isInputValid() {
        boolean isValid = true;

        String name = editTextGoalName.getText().toString().trim();
        String amountStr = editTextTargetAmount.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            textFieldGoalNameLayout.setError("Please enter a goal name");
            isValid = false;
        } else if (name.length() < 3) {
            textFieldGoalNameLayout.setError("Goal name must be at least 3 characters");
            isValid = false;
        } else {
            textFieldGoalNameLayout.setError(null);
        }

        if (TextUtils.isEmpty(amountStr)) {
            textFieldTargetAmountLayout.setError("Please enter a target amount");
            isValid = false;
        } else {
            try {
                double amount = parseAmount(amountStr);
                if (amount <= 0) {
                    textFieldTargetAmountLayout.setError("Amount must be greater than 0");
                    isValid = false;
                } else if (amount > 1000000000) {
                    textFieldTargetAmountLayout.setError("Amount is too large");
                    isValid = false;
                } else {
                    textFieldTargetAmountLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                textFieldTargetAmountLayout.setError("Please enter a valid amount");
                isValid = false;
            }
        }

        return isValid;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        cleanup();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanup();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanup();
    }

    private void cleanup() {
        // Clear the listener
        listener = null;
        
        // Clear any saved state
        if (getArguments() != null) {
            getArguments().clear();
        }
        
        // Clear references to views
        textFieldGoalNameLayout = null;
        textFieldTargetAmountLayout = null;
        editTextGoalName = null;
        editTextTargetAmount = null;
        dialogTitle = null;
        dialogSubtitle = null;
        buttonSave = null;
        buttonCancel = null;
    }

    private void dismissDialog() {
        try {
            if (isAdded() && getDialog() != null && getDialog().isShowing()) {
                dismissAllowingStateLoss();
                cleanup();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 