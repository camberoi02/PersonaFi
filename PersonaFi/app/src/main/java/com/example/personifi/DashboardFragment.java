package com.example.personifi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;
import java.util.List;

public class DashboardFragment extends Fragment {
    
    private TransactionViewModel transactionViewModel;
    private RecyclerView categoryBreakdownRecycler;
    private PieChart spendingChart;
    private LineChart trendsChart;
    private ChipGroup timePeriodChipGroup;
    private Chip chipWeek, chipMonth, chipYear;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        categoryBreakdownRecycler = view.findViewById(R.id.category_breakdown_recycler);
        spendingChart = view.findViewById(R.id.spending_chart);
        trendsChart = view.findViewById(R.id.trends_chart);
        timePeriodChipGroup = view.findViewById(R.id.time_period_chip_group);
        chipWeek = view.findViewById(R.id.chip_week);
        chipMonth = view.findViewById(R.id.chip_month);
        chipYear = view.findViewById(R.id.chip_year);
        
        // Setup RecyclerView
        categoryBreakdownRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Initialize ViewModel
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        
        // Load dashboard data
        loadDashboardData();
        
        setupCharts();
        setupRecyclerView();
        setupChipGroup();
    }
    
    private void loadDashboardData() {
        // Observe transaction data from the ViewModel
        transactionViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                // TODO: Show empty state
                return;
            }
            
            // Calculate and display statistics based on transactions
            double totalIncome = 0;
            double totalExpenses = 0;
            
            for (Transaction transaction : transactions) {
                if (transaction.getType() == Transaction.TransactionType.INCOME) {
                    totalIncome += transaction.getAmount();
                } else {
                    totalExpenses += transaction.getAmount();
                }
            }
            
            // Update charts and recycler view with the data
            updateCharts(totalIncome, totalExpenses, transactions);
            updateCategoryBreakdown(transactions);
        });
    }
    
    private void updateCharts(double totalIncome, double totalExpenses, List<Transaction> transactions) {
        // TODO: Update pie chart with spending categories
        // TODO: Update line chart with spending trends
    }
    
    private void updateCategoryBreakdown(List<Transaction> transactions) {
        // TODO: Update category breakdown recycler view
    }
    
    private void setupCharts() {
        // Configure pie chart
        spendingChart.setUsePercentValues(true);
        spendingChart.getDescription().setEnabled(false);
        spendingChart.setDrawHoleEnabled(true);
        spendingChart.setHoleColor(android.graphics.Color.WHITE);
        spendingChart.setTransparentCircleRadius(61f);
        
        // Configure line chart
        trendsChart.getDescription().setEnabled(false);
        trendsChart.setDrawGridBackground(false);
        trendsChart.setDrawBorders(false);
        trendsChart.getAxisLeft().setDrawGridLines(true);
        trendsChart.getAxisRight().setEnabled(false);
        trendsChart.getXAxis().setDrawGridLines(false);
    }
    
    private void setupRecyclerView() {
        // TODO: Setup RecyclerView with adapter for category breakdown
    }
    
    private void setupChipGroup() {
        timePeriodChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_week) {
                // TODO: Update data for weekly view
            } else if (checkedId == R.id.chip_month) {
                // TODO: Update data for monthly view
            } else if (checkedId == R.id.chip_year) {
                // TODO: Update data for yearly view
            }
        });
    }
} 