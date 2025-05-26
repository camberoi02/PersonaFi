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

public class GoalDialog extends DialogFragment {
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

        if (isEdit) {
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
            if (validateInput()) {
                String name = editTextGoalName.getText().toString().trim();
                String amountStr = NumberFormattingTextWatcher.parseAmount(editTextTargetAmount.getText().toString().trim());
                double amount = Double.parseDouble(amountStr);
                if (listener != null) {
                    listener.onGoalSet(name, amount, position);
                }
                dismiss();
            }
        });

        buttonCancel.setOnClickListener(v -> dismiss());

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
        }
    }

    private boolean validateInput() {
        boolean isValid = true;

        String name = editTextGoalName.getText().toString().trim();
        String amountStr = NumberFormattingTextWatcher.parseAmount(editTextTargetAmount.getText().toString().trim());

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
                double amount = Double.parseDouble(amountStr);
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
} 