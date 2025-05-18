package com.example.personifi;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddBudgetActivity extends AppCompatActivity {

    private EditText editCategory;
    private EditText editAmount;
    private EditText editStartDate;
    private EditText editEndDate;
    private AutoCompleteTextView spinnerPeriod;
    private Button buttonSave;
    
    private Calendar startCalendar;
    private Calendar endCalendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_budget);
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Handle edge to edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_budget_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        editCategory = findViewById(R.id.edit_budget_category);
        editAmount = findViewById(R.id.edit_budget_amount);
        editStartDate = findViewById(R.id.edit_start_date);
        editEndDate = findViewById(R.id.edit_end_date);
        spinnerPeriod = findViewById(R.id.spinner_period);
        buttonSave = findViewById(R.id.button_save_budget);

        // Setup date formatter
        dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        
        // Setup calendars
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        
        // Set default end date to last day of current month
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        
        // Update date displays
        updateStartDateDisplay();
        updateEndDateDisplay();
        
        // Setup period spinner with material dropdown
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.budget_periods, R.layout.item_dropdown);
        spinnerPeriod.setAdapter(adapter);
        
        // Select default period
        spinnerPeriod.setText(adapter.getItem(2).toString(), false); // Monthly by default
        
        // Setup date pickers
        editStartDate.setOnClickListener(v -> showStartDatePicker());
        editEndDate.setOnClickListener(v -> showEndDatePicker());
        
        // Setup save button with animation
        buttonSave.setOnClickListener(v -> {
            if (validateInputs()) {
                // Add animation to button
                buttonSave.setEnabled(false);
                buttonSave.setText(R.string.saving);
                
                // Delay to show animation before saving
                new Handler().postDelayed(() -> saveBudget(), 800);
            }
        });
    }
    
    private void showStartDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            startCalendar.set(Calendar.YEAR, year);
            startCalendar.set(Calendar.MONTH, month);
            startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateStartDateDisplay();
            
            // If start date is after end date, update end date
            if (startCalendar.after(endCalendar)) {
                endCalendar.setTime(startCalendar.getTime());
                updateEndDateDisplay();
            }
        }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
           startCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    
    private void showEndDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            endCalendar.set(Calendar.YEAR, year);
            endCalendar.set(Calendar.MONTH, month);
            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateEndDateDisplay();
        }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
           endCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    
    private void updateStartDateDisplay() {
        editStartDate.setText(dateFormat.format(startCalendar.getTime()));
    }
    
    private void updateEndDateDisplay() {
        editEndDate.setText(dateFormat.format(endCalendar.getTime()));
    }
    
    private boolean validateInputs() {
        boolean hasError = false;
        
        if (TextUtils.isEmpty(editCategory.getText())) {
            TextInputLayout categoryLayout = findViewById(R.id.layout_category);
            categoryLayout.setError(getString(R.string.error_category_required));
            hasError = true;
        } else {
            TextInputLayout categoryLayout = findViewById(R.id.layout_category);
            categoryLayout.setError(null);
        }
        
        if (TextUtils.isEmpty(editAmount.getText())) {
            TextInputLayout amountLayout = findViewById(R.id.layout_amount);
            amountLayout.setError(getString(R.string.error_amount_required));
            hasError = true;
        } else {
            TextInputLayout amountLayout = findViewById(R.id.layout_amount);
            amountLayout.setError(null);
        }
        
        if (endCalendar.before(startCalendar)) {
            TextInputLayout endDateLayout = findViewById(R.id.layout_end_date);
            endDateLayout.setError(getString(R.string.error_end_date));
            hasError = true;
        } else {
            TextInputLayout endDateLayout = findViewById(R.id.layout_end_date);
            endDateLayout.setError(null);
        }
        
        if (TextUtils.isEmpty(spinnerPeriod.getText())) {
            TextInputLayout spinnerLayout = findViewById(R.id.spinner_layout);
            spinnerLayout.setError(getString(R.string.error_period_required));
            hasError = true;
        } else {
            TextInputLayout spinnerLayout = findViewById(R.id.spinner_layout);
            spinnerLayout.setError(null);
        }
        
        return !hasError;
    }
    
    private void saveBudget() {
        try {
            // Get values
            String category = editCategory.getText().toString().trim();
            double amount = Double.parseDouble(editAmount.getText().toString().trim());
            Date startDate = startCalendar.getTime();
            Date endDate = endCalendar.getTime();
            
            // Get budget period
            Budget.BudgetPeriod period;
            String periodText = spinnerPeriod.getText().toString();
            if (periodText.equals(getString(R.string.period_daily))) {
                period = Budget.BudgetPeriod.DAILY;
            } else if (periodText.equals(getString(R.string.period_weekly))) {
                period = Budget.BudgetPeriod.WEEKLY;
            } else if (periodText.equals(getString(R.string.period_monthly))) {
                period = Budget.BudgetPeriod.MONTHLY;
            } else {
                period = Budget.BudgetPeriod.YEARLY;
            }
            
            // Prepare result intent
            Intent resultIntent = new Intent();
            resultIntent.putExtra("BUDGET_CATEGORY", category);
            resultIntent.putExtra("BUDGET_AMOUNT", amount);
            resultIntent.putExtra("BUDGET_START_DATE", startDate.getTime());
            resultIntent.putExtra("BUDGET_END_DATE", endDate.getTime());
            resultIntent.putExtra("BUDGET_PERIOD", period.ordinal());
            
            // Set result and finish
            setResult(RESULT_OK, resultIntent);
            finish();
            
        } catch (NumberFormatException e) {
            TextInputLayout amountLayout = findViewById(R.id.layout_amount);
            amountLayout.setError(getString(R.string.error_invalid_amount));
        }
    }
} 