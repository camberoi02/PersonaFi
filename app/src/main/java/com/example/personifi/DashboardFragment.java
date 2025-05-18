package com.example.personifi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardFragment extends Fragment {
    
    private TransactionViewModel transactionViewModel;
    private RecyclerView recyclerView;
    private TextView textStatsContent;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerview_categories);
        textStatsContent = view.findViewById(R.id.text_stats_content);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Initialize ViewModel
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        
        // Load dashboard data
        loadDashboardData();
    }
    
    private void loadDashboardData() {
        // Observe transaction data from the ViewModel
        transactionViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                textStatsContent.setText("No transactions to analyze. Add some transactions to see statistics.");
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
            
            double balance = totalIncome - totalExpenses;
            double savingsRate = totalIncome > 0 ? (totalIncome - totalExpenses) / totalIncome * 100 : 0;
            
            // Display statistics
            StringBuilder stats = new StringBuilder();
            stats.append("Total Income: ").append(formatCurrency(totalIncome)).append("\n");
            stats.append("Total Expenses: ").append(formatCurrency(totalExpenses)).append("\n");
            stats.append("Current Balance: ").append(formatCurrency(balance)).append("\n");
            stats.append("Savings Rate: ").append(String.format("%.1f%%", savingsRate)).append("\n");
            
            textStatsContent.setText(stats.toString());
            
            // TODO: Implement category breakdown and display in RecyclerView
        });
    }
    
    private String formatCurrency(double amount) {
        return String.format("₱%.2f", amount);
    }
} 