package com.example.personafi;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

/**
 * Entity representing a budget category in the PersoniFi app.
 */
@Entity(tableName = "budgets")
public class Budget {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String category;
    private double amount;
    
    @TypeConverters(DateConverter.class)
    private Date startDate;
    
    @TypeConverters(DateConverter.class)
    private Date endDate;
    
    private BudgetPeriod period;

    // Constructor for creating a new budget
    public Budget(String category, double amount, Date startDate, Date endDate, BudgetPeriod period) {
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.period = period;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public BudgetPeriod getPeriod() {
        return period;
    }

    public void setPeriod(BudgetPeriod period) {
        this.period = period;
    }
    
    // Enum for budget period
    public enum BudgetPeriod {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }
} 