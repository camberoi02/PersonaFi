package com.example.personifi;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Adapter for the RecyclerView to display transactions.
 */
public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {

    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public TransactionAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK = new DiffUtil.ItemCallback<Transaction>() {
        @Override
        public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            // Check if any fields have changed
            return oldItem.getAmount() == newItem.getAmount() &&
                    oldItem.getCategory().equals(newItem.getCategory()) &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getDate().equals(newItem.getDate()) &&
                    oldItem.getType() == newItem.getType();
        }
    };

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction current = getItem(position);
        holder.bind(current);
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView amountTextView;
        private final TextView categoryTextView;
        private final TextView descriptionTextView;
        private final TextView dateTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            amountTextView = itemView.findViewById(R.id.text_amount);
            categoryTextView = itemView.findViewById(R.id.text_category);
            descriptionTextView = itemView.findViewById(R.id.text_description);
            dateTextView = itemView.findViewById(R.id.text_date);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        public void bind(Transaction transaction) {
            String amountPrefix = transaction.getType() == Transaction.TransactionType.INCOME ? "+ $" : "- $";
            String amountStr = amountPrefix + String.format("%.2f", transaction.getAmount());
            
            amountTextView.setText(amountStr);
            // Set text color based on transaction type
            amountTextView.setTextColor(transaction.getType() == Transaction.TransactionType.INCOME ? 
                    Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
                    
            categoryTextView.setText(transaction.getCategory());
            descriptionTextView.setText(transaction.getDescription());
            dateTextView.setText(dateFormat.format(transaction.getDate()));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}