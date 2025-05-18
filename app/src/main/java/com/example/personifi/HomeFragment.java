package com.example.personifi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment {
    
    private TransactionViewModel transactionViewModel;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private LinearLayout emptyView;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Adjust layout parameters for the root view when used in transactions_container
        if (container != null && container.getId() == R.id.transactions_container) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params != null) {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(params);
            }
            
            // Hide the title since it's already in the main activity
            View titleView = view.findViewById(R.id.text_title);
            if (titleView != null) {
                titleView.setVisibility(View.GONE);
            }
        }
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerview_transactions);
        emptyView = view.findViewById(R.id.empty_view);
        
        // Setup RecyclerView
        adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Initialize ViewModel
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        
        // Observe transactions
        transactionViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            // Update RecyclerView
            adapter.submitList(transactions);
            
            // Show empty state if no transactions
            if (transactions == null || transactions.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                
                // Limit the number of transactions shown in transactions_container
                if (getView() != null && getView().getParent() instanceof View) {
                    View parent = (View) getView().getParent();
                    if (parent.getId() == R.id.transactions_container) {
                        // Only show the most recent 5 transactions
                        int limit = Math.min(5, transactions.size());
                        adapter.submitList(transactions.subList(0, limit));
                    }
                }
            }
        });
        
        // Setup item click handler for editing transactions
        adapter.setOnItemClickListener(transaction -> {
            // TODO: Implement edit transaction functionality
            Toast.makeText(requireContext(), "Edit transaction: " + transaction.getDescription(), Toast.LENGTH_SHORT).show();
        });
    }
} 