package com.example.personifi;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import com.example.personifi.TimeConstants;
import com.example.personifi.utils.TimeUtils;

/**
 * Entity representing a savings goal in the PersoniFi app.
 * Helps students set and track savings goals.
 */
@Entity(tableName = "savings_goals")
public class SavingsGoal {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String description;
    private double targetAmount;
    private double currentAmount;
    private boolean isCompleted;
    
    @TypeConverters(DateConverter.class)
    private Date createdDate;
    
    @TypeConverters(DateConverter.class)
    private Date targetDate;

    // Priority level for the savings goal
    private Priority priority;

    // Constructor for creating a new savings goal
    public SavingsGoal(String name, String description, double targetAmount, 
                       double currentAmount, Date createdDate, Date targetDate, 
                       Priority priority) {
        this.name = capitalizeWords(name);
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.isCompleted = false;
        this.createdDate = createdDate;
        this.targetDate = targetDate;
        this.priority = priority;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = capitalizeWords(name);
    }

    // Utility method to capitalize the first letter of each word
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : text.toCharArray()) {
            if (Character.isSpaceChar(c) || c == '-' || c == '_' || c == '.' || c == ',') {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
        
        // Update completion status
        this.isCompleted = currentAmount >= targetAmount;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(Date targetDate) {
        this.targetDate = targetDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    // Calculate progress percentage
    public double getProgressPercentage() {
        if (targetAmount == 0) {
            return 0;
        }
        return (currentAmount / targetAmount) * 100;
    }
    
    // Calculate remaining amount to save
    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }
    
    // Add @Ignore annotation for fields that shouldn't be persisted
    @androidx.room.Ignore
    private transient double totalDays;
    @androidx.room.Ignore
    private transient double totalWeeks;
    @androidx.room.Ignore
    private transient double totalMonths;
    @androidx.room.Ignore
    private transient double dailyAmount;
    @androidx.room.Ignore
    private transient double weeklyAmount;
    @androidx.room.Ignore
    private transient double monthlyAmount;

    // Getters for the calculated fields
    public double getTotalDays() {
        getMonthlyGoal(); // Ensure values are calculated
        return totalDays;
    }

    public double getTotalWeeks() {
        getMonthlyGoal(); // Ensure values are calculated
        return totalWeeks;
    }

    public double getTotalMonths() {
        getMonthlyGoal(); // Ensure values are calculated
        return totalMonths;
    }

    public double getDailyAmount() {
        getMonthlyGoal(); // Ensure values are calculated
        return dailyAmount;
    }

    public double getWeeklyAmount() {
        getMonthlyGoal(); // Ensure values are calculated
        return weeklyAmount;
    }

    public double getMonthlyAmount() {
        getMonthlyGoal(); // Ensure values are calculated
        return monthlyAmount;
    }

    // Calculate monthly saving goal to reach target by target date
    public double getMonthlyGoal() {
        if (targetDate == null) {
            return 0;
        }
        
        // If goal is completed, return 0
        if (isCompleted()) {
            this.totalDays = 0;
            this.totalWeeks = 0;
            this.totalMonths = 0;
            this.dailyAmount = 0;
            this.weeklyAmount = 0;
            this.monthlyAmount = 0;
            return 0;
        }
        
        // Get current date
        Date currentDate = new Date();
        
        // Check if goal is overdue
        boolean isOverdue = TimeUtils.isDateInPast(targetDate);
        
        // Calculate periods remaining
        this.totalDays = TimeUtils.getPeriodsRemaining(currentDate, targetDate, 0);
        this.totalWeeks = TimeUtils.getPeriodsRemaining(currentDate, targetDate, 1);
        this.totalMonths = TimeUtils.getPeriodsRemaining(currentDate, targetDate, 2);
        
        // Calculate required amounts
        double remainingAmount = getRemainingAmount();
        
        // Calculate amounts per period
        this.dailyAmount = TimeUtils.calculateAmountPerPeriod(remainingAmount, totalDays, isOverdue);
        this.weeklyAmount = TimeUtils.calculateAmountPerPeriod(remainingAmount, totalWeeks, isOverdue);
        this.monthlyAmount = TimeUtils.calculateAmountPerPeriod(remainingAmount, totalMonths, isOverdue);
        
        // Log calculation details for debugging
        android.util.Log.d("SavingsGoal", String.format(
            "Goal Calculation for %s:\n" +
            "Target Date: %s\n" +
            "Current Date: %s\n" +
            "Total Days: %.2f\n" +
            "Total Weeks: %.2f\n" +
            "Total Months: %.2f\n" +
            "Target Amount: %.2f\n" +
            "Remaining Amount: %.2f\n" +
            "Daily Required: %.2f\n" +
            "Weekly Required: %.2f\n" +
            "Monthly Required: %.2f\n" +
            "Is Overdue: %b",
            this.name,
            new java.text.SimpleDateFormat("yyyy-MM-dd").format(targetDate),
            new java.text.SimpleDateFormat("yyyy-MM-dd").format(currentDate),
            this.totalDays,
            this.totalWeeks,
            this.totalMonths,
            targetAmount,
            remainingAmount,
            this.dailyAmount,
            this.weeklyAmount,
            this.monthlyAmount,
            isOverdue
        ));
        
        return this.monthlyAmount;
    }
    
    // Enum for priority level
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
    
    /**
     * Create a deep copy of this SavingsGoal
     */
    public SavingsGoal copy() {
        SavingsGoal copy = new SavingsGoal(
            this.name,
            this.description,
            this.targetAmount,
            this.currentAmount,
            this.createdDate != null ? new Date(this.createdDate.getTime()) : null,
            this.targetDate != null ? new Date(this.targetDate.getTime()) : null,
            this.priority
        );
        copy.setId(this.id);
        copy.setCompleted(this.isCompleted);
        return copy;
    }
} 