package com.example.personifi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Adapter for the budget recycler view.
 */
public class BudgetAdapter extends ListAdapter<BudgetWithSpending, BudgetAdapter.BudgetViewHolder> {

    private final NumberFormat currencyFormatter;
    private final SimpleDateFormat dateFormat;
    private OnItemClickListener listener;

    protected BudgetAdapter(NumberFormat currencyFormatter) {
        super(DIFF_CALLBACK);
        this.currencyFormatter = currencyFormatter;
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    }

    private static final DiffUtil.ItemCallback<BudgetWithSpending> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<BudgetWithSpending>() {
                @Override
                public boolean areItemsTheSame(@NonNull BudgetWithSpending oldItem, @NonNull BudgetWithSpending newItem) {
                    return oldItem.getBudget().getId() == newItem.getBudget().getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull BudgetWithSpending oldItem, @NonNull BudgetWithSpending newItem) {
                    return oldItem.getSpent() == newItem.getSpent() &&
                            oldItem.getBudget().getAmount() == newItem.getBudget().getAmount() &&
                            oldItem.getBudget().getCategory().equals(newItem.getBudget().getCategory());
                }
            };

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetWithSpending current = getItem(position);
        Budget budget = current.getBudget();
        
        holder.textBudgetCategory.setText(budget.getCategory());
        holder.textBudgetAmount.setText(currencyFormatter.format(budget.getAmount()));
        holder.textSpent.setText(String.format(Locale.getDefault(), 
                "Spent: %s", currencyFormatter.format(current.getSpent())));
        holder.textRemaining.setText(String.format(Locale.getDefault(), 
                "Remaining: %s", currencyFormatter.format(current.getRemaining())));
        
        // Format date range
        String dateRange = String.format(Locale.getDefault(), "%s - %s",
                dateFormat.format(budget.getStartDate()),
                dateFormat.format(budget.getEndDate()));
        holder.textDateRange.setText(dateRange);
        
        // Set progress
        int progress = (int) current.getPercentSpent();
        holder.progressBudget.setProgress(Math.min(progress, 100));
        
        // Set color based on spending
        if (progress >= 100) {
            holder.progressBudget.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark)));
        } else if (progress >= 75) {
            holder.progressBudget.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark)));
        } else {
            holder.progressBudget.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark)));
        }
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private final TextView textBudgetCategory;
        private final TextView textBudgetAmount;
        private final TextView textSpent;
        private final TextView textRemaining;
        private final TextView textDateRange;
        private final ProgressBar progressBudget;

        private BudgetViewHolder(View itemView) {
            super(itemView);
            textBudgetCategory = itemView.findViewById(R.id.text_budget_category);
            textBudgetAmount = itemView.findViewById(R.id.text_budget_amount);
            textSpent = itemView.findViewById(R.id.text_budget_spent);
            textRemaining = itemView.findViewById(R.id.text_budget_remaining);
            textDateRange = itemView.findViewById(R.id.text_budget_date_range);
            progressBudget = itemView.findViewById(R.id.progress_budget);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(BudgetWithSpending budget);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
} 