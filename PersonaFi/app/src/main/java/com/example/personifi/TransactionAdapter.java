package com.example.personifi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {
    private OnItemClickListener listener;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;

    public TransactionAdapter() {
        super(new DiffUtil.ItemCallback<Transaction>() {
            @Override
            public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                return oldItem.equals(newItem);
            }
        });
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fil", "PH"));
        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        holder.bind(transaction);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textDescription;
        private final TextView textCategory;
        private final TextView textAmount;
        private final TextView textDate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescription = itemView.findViewById(R.id.text_description);
            textCategory = itemView.findViewById(R.id.text_category);
            textAmount = itemView.findViewById(R.id.text_amount);
            textDate = itemView.findViewById(R.id.text_date);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        public void bind(Transaction transaction) {
            textDescription.setText(transaction.getDescription());
            textCategory.setText(transaction.getCategory());
            textAmount.setText(currencyFormatter.format(transaction.getAmount()));
            textDate.setText(dateFormatter.format(transaction.getDate()));

            // Set text color based on transaction type
            int colorResId = transaction.getType() == Transaction.TransactionType.INCOME 
                ? R.color.income 
                : R.color.expense;
            textAmount.setTextColor(itemView.getContext().getColor(colorResId));
        }
    }
} 