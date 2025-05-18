package com.example.personifi;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    // UI Components
    private RadioGroup radioGroupTransactionType;
    private TextInputEditText editTextAmount;
    private AutoCompleteTextView autoCompleteTextViewCategory;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextDate;
    private MaterialButton buttonSave;

    // Variables
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    // Category suggestions
    private String[] incomeCategories = {"Salary", "Allowance", "Freelance", "Investments", "Gift", "Other Income"};
    private String[] expenseCategories = {"Food", "Housing", "Transportation", "Entertainment", "Utilities", 
                                        "Healthcare", "Education", "Shopping", "Personal Care", "Travel", 
                                        "Debt Payments", "Other Expenses"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize UI components
        radioGroupTransactionType = findViewById(R.id.radioGroup_transaction_type);
        editTextAmount = findViewById(R.id.editText_amount);
        autoCompleteTextViewCategory = findViewById(R.id.autoCompleteTextView_category);
        editTextDescription = findViewById(R.id.editText_description);
        editTextDate = findViewById(R.id.editText_date);
        buttonSave = findViewById(R.id.button_save);

        // Initialize date handling
        selectedDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        editTextDate.setText(dateFormat.format(selectedDate.getTime()));

        // Setup category suggestions for income by default
        ArrayAdapter<String> incomeCategoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, incomeCategories);
        autoCompleteTextViewCategory.setAdapter(incomeCategoryAdapter);

        // Setup listeners
        setupListeners();
    }

    private void setupListeners() {
        // Date picker dialog for date selection
        editTextDate.setOnClickListener(v -> showDatePickerDialog());

        // Radio button listener to change category suggestions based on transaction type
        radioGroupTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            // Change category suggestions based on transaction type
            if (checkedId == R.id.radio_income) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, incomeCategories);
                autoCompleteTextViewCategory.setAdapter(adapter);
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, expenseCategories);
                autoCompleteTextViewCategory.setAdapter(adapter);
            }
        });

        // Save button click handler
        buttonSave.setOnClickListener(v -> saveTransaction());
    }

    private void showDatePickerDialog() {
        // Show date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Set the selected date
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // Update the date EditText
                    editTextDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveTransaction() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Get transaction type
        boolean isIncome = radioGroupTransactionType.getCheckedRadioButtonId() == R.id.radio_income;
        Transaction.TransactionType type = isIncome ? Transaction.TransactionType.INCOME : Transaction.TransactionType.EXPENSE;

        // Get amount
        double amount = Double.parseDouble(editTextAmount.getText().toString());

        // Get category
        String category = autoCompleteTextViewCategory.getText().toString();

        // Get description
        String description = editTextDescription.getText().toString();

        // Get date
        Date date = selectedDate.getTime();

        // Create transaction
        Transaction transaction = new Transaction(amount, category, description, date, type);

        // Pass the transaction back to MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("TRANSACTION_AMOUNT", amount);
        resultIntent.putExtra("TRANSACTION_CATEGORY", category);
        resultIntent.putExtra("TRANSACTION_DESCRIPTION", description);
        resultIntent.putExtra("TRANSACTION_DATE", date.getTime());
        resultIntent.putExtra("TRANSACTION_TYPE", type.ordinal());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate amount
        if (TextUtils.isEmpty(editTextAmount.getText())) {
            editTextAmount.setError("Amount is required");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(editTextAmount.getText().toString());
                if (amount <= 0) {
                    editTextAmount.setError("Amount must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                editTextAmount.setError("Invalid amount");
                isValid = false;
            }
        }

        // Validate category
        if (TextUtils.isEmpty(autoCompleteTextViewCategory.getText())) {
            autoCompleteTextViewCategory.setError("Category is required");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}