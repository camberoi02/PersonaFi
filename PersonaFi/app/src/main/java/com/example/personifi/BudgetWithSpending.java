package com.example.personifi;

import androidx.room.Embedded;

import java.util.Date;

/**
 * Plain Old Java Object (POJO) to hold a budget with spending information.
 */
public class BudgetWithSpending {

    @Embedded
    private Budget budget;
    
    private double spent;
    
    // Calculate remaining budget
    public double getRemaining() {
        return budget.getAmount() - spent;
    }
    
    // Calculate percentage spent
    public double getPercentSpent() {
        if (budget.getAmount() == 0) {
            return 0;
        }
        return (spent / budget.getAmount()) * 100;
    }
    
    // Getters and Setters
    
    public Budget getBudget() {
        return budget;
    }
    
    public void setBudget(Budget budget) {
        this.budget = budget;
    }
    
    public double getSpent() {
        return spent;
    }
    
    public void setSpent(double spent) {
        this.spent = spent;
    }
} 