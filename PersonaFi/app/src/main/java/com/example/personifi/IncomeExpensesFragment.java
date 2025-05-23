package com.example.personifi;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class IncomeExpensesFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddTransaction;
    private TransactionViewModel transactionViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_income_expenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction);

        // Initialize ViewModel
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        setupViewPager();
        setupFab();
    }

    private void setupViewPager() {
        // Create adapter with Income and Expenses fragments
        TransactionPagerAdapter pagerAdapter = new TransactionPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> tab.setText(position == 0 ? "Income" : "Expenses")
        ).attach();
    }

    private void setupFab() {
        fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AddTransactionActivity.class);
            startActivity(intent);
        });
    }

    // ViewPager Adapter
    private static class TransactionPagerAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<TransactionPagerAdapter.PageViewHolder> {
        private final Fragment fragment;
        private final List<Transaction.TransactionType> types;

        public TransactionPagerAdapter(Fragment fragment) {
            this.fragment = fragment;
            this.types = new ArrayList<>();
            types.add(Transaction.TransactionType.INCOME);
            types.add(Transaction.TransactionType.EXPENSE);
        }

        @NonNull
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_transaction_list, parent, false);
            return new PageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
            Transaction.TransactionType type = types.get(position);
            holder.setupRecyclerView(type);
        }

        @Override
        public int getItemCount() {
            return types.size();
        }

        private class PageViewHolder extends RecyclerView.ViewHolder {
            private final RecyclerView recyclerView;
            private final TransactionAdapter adapter;

            public PageViewHolder(@NonNull View itemView) {
                super(itemView);
                recyclerView = itemView.findViewById(R.id.recyclerview_transactions);
                adapter = new TransactionAdapter();
                recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                recyclerView.setAdapter(adapter);
            }

            public void setupRecyclerView(Transaction.TransactionType type) {
                TransactionViewModel viewModel = new ViewModelProvider(fragment.requireActivity())
                        .get(TransactionViewModel.class);

                viewModel.getTransactionsByType(type).observe(fragment.getViewLifecycleOwner(), transactions -> {
                    adapter.submitList(transactions);
                });
            }
        }
    }
} 