package com.example.personafi;

public class SavingsGoal {
    private String name;
    private double targetAmount;
    private double currentAmount;
    private double lastAddedAmount;
    private boolean achieved;

    public SavingsGoal(String name, double targetAmount) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = 0;
        this.lastAddedAmount = 0;
        this.achieved = false;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (this.currentAmount >= this.targetAmount) {
            this.achieved = true;
        }
    }

    public double getLastAddedAmount() {
        return lastAddedAmount;
    }

    public void setLastAddedAmount(double lastAddedAmount) {
        this.lastAddedAmount = lastAddedAmount;
    }

    public boolean isAchieved() {
        return achieved;
    }

    public void setAchieved(boolean achieved) {
        this.achieved = achieved;
    }

    public double getProgress() {
        if (targetAmount <= 0) {
            return 0;
        }
        return currentAmount / targetAmount;
    }
} 