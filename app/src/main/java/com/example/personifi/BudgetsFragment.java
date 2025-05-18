package com.example.personifi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BudgetsFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private TextView textSelectedMonth;
    private MaterialButton btnChangeMonth;
    private FloatingActionButton fabAddBudget;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budgets, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerview_budgets);
        textSelectedMonth = view.findViewById(R.id.text_selected_month);
        btnChangeMonth = view.findViewById(R.id.btn_change_month);
        fabAddBudget = view.findViewById(R.id.fab_add_budget);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Set current month
        updateDisplayedMonth(Calendar.getInstance().getTime());
        
        // Setup month selection button
        btnChangeMonth.setOnClickListener(v -> showMonthPicker());
        
        // Setup add budget button
        fabAddBudget.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Add budget feature coming soon", Toast.LENGTH_SHORT).show();
            // TODO: Implement add budget dialog
        });
        
        // Load budgets data
        loadBudgets();
    }
    
    private void showMonthPicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Month")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            updateDisplayedMonth(calendar.getTime());
            loadBudgets();
        });
        
        datePicker.show(getChildFragmentManager(), "MONTH_PICKER");
    }
    
    private void updateDisplayedMonth(Date date) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        textSelectedMonth.setText(monthFormat.format(date));
    }
    
    private void loadBudgets() {
        // TODO: Load budgets from database based on selected month
    }
} 