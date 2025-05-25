package com.example.personafi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class IncomeExpensesFragment extends Fragment {
    private TransactionViewModel transactionViewModel;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private LinearLayout emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income_expenses, container, false);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerview_transactions);
        emptyView = view.findViewById(R.id.empty_view);

        // Setup RecyclerView
        adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup ViewModel
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Observe transactions
        transactionViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            adapter.submitList(transactions);
            
            // Show empty state if no transactions
            if (transactions == null || transactions.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });

        // Setup item click listener
        adapter.setOnItemClickListener(transaction -> {
            // TODO: Implement edit transaction functionality
        });

        return view;
    }
} 