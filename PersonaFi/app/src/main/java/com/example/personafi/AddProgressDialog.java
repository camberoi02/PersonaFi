package com.example.personafi;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.util.DisplayMetrics;
import com.example.personafi.NumberFormattingTextWatcher;
import java.text.NumberFormat;
import java.util.Locale;

public class AddProgressDialog extends DialogFragment {
    private String goalName;
    private double previousAmount;
    private OnProgressAddedListener listener;
    private TextInputLayout textInputLayout;
    private TextInputEditText editTextAmount;
    private NumberFormat currencyFormatter;

    public interface OnProgressAddedListener {
        void onProgressAdded(double amount);
    }

    public static AddProgressDialog newInstance(String goalName, double previousAmount) {
        AddProgressDialog dialog = new AddProgressDialog();
        Bundle args = new Bundle();
        args.putString("goalName", goalName);
        args.putDouble("previousAmount", previousAmount);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_PersonaFi_Dialog);
        if (getArguments() != null) {
            goalName = getArguments().getString("goalName");
            previousAmount = getArguments().getDouble("previousAmount", 0.0);
        }
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_progress, container, false);

        // Set up title
        TextView dialogTitle = view.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Add Progress to " + goalName);

        // Add previous amount text
        TextView textViewPreviousAmount = view.findViewById(R.id.textViewPreviousAmount);
        if (previousAmount > 0) {
            textViewPreviousAmount.setText("Last added: " + currencyFormatter.format(previousAmount));
        } else {
            textViewPreviousAmount.setText("No previous additions");
        }

        // Set up input field
        textInputLayout = view.findViewById(R.id.textInputLayout);
        editTextAmount = view.findViewById(R.id.editTextAmount);

        // Add number formatting to amount input
        editTextAmount.addTextChangedListener(new NumberFormattingTextWatcher(editTextAmount));

        // Add text change listener for validation
        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set up buttons
        MaterialButton buttonCancel = view.findViewById(R.id.buttonCancel);
        MaterialButton buttonAdd = view.findViewById(R.id.buttonAdd);

        buttonCancel.setOnClickListener(v -> dismiss());
        buttonAdd.setOnClickListener(v -> validateAndAdd());

        return view;
    }

    private void validateAndAdd() {
        String amountStr = NumberFormattingTextWatcher.parseAmount(editTextAmount.getText().toString().trim());
        if (amountStr.isEmpty()) {
            textInputLayout.setError("Please enter an amount");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                textInputLayout.setError("Amount must be greater than 0");
                return;
            }
            if (amount > 1000000000) {
                textInputLayout.setError("Amount is too large");
                return;
            }
            if (listener != null) {
                listener.onProgressAdded(amount);
            }
            dismiss();
        } catch (NumberFormatException e) {
            textInputLayout.setError("Invalid amount");
        }
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

    public void setOnProgressAddedListener(OnProgressAddedListener listener) {
        this.listener = listener;
    }
} 