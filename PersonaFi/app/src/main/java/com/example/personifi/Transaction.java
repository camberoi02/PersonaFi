package com.example.personifi;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.personifi.database.DateConverter;

import java.util.Date;

/**
 * Entity representing a financial transaction in the PersoniFi app.
 */
@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private double amount;
    private String category;
    private String description;
    
    @TypeConverters(DateConverter.class)
    private Date date;
    
    private TransactionType type; // INCOME or EXPENSE

    // Constructor for creating a new transaction
    public Transaction(double amount, String category, String description, Date date, TransactionType type) {
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.type = type;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
    
    // Enum for transaction type
    public enum TransactionType {
        INCOME,
        EXPENSE
    }
}