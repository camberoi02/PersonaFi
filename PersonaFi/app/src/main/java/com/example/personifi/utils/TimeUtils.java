package com.example.personifi.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for handling time-related calculations consistently across the app.
 */
public class TimeUtils {
    // Constants
    public static final double DAYS_IN_MONTH = 30.436875; // Astronomical calculation
    public static final double DAYS_IN_WEEK = 7.0;
    
    /**
     * Calculate the exact number of days between two dates
     */
    public static double calculateDaysBetween(Date startDate, Date endDate) {
        Calendar startCal = getStartOfDay(startDate);
        Calendar endCal = getStartOfDay(endDate);
        
        long diffInMillis = endCal.getTimeInMillis() - startCal.getTimeInMillis();
        return diffInMillis / (1000.0 * 60 * 60 * 24);
    }
    
    /**
     * Calculate the exact number of weeks between two dates
     */
    public static double calculateWeeksBetween(Date startDate, Date endDate) {
        return calculateDaysBetween(startDate, endDate) / DAYS_IN_WEEK;
    }
    
    /**
     * Calculate the exact number of months between two dates
     */
    public static double calculateMonthsBetween(Date startDate, Date endDate) {
        Calendar startCal = getStartOfDay(startDate);
        Calendar endCal = getStartOfDay(endDate);
        
        int years = endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR);
        int months = endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH) + (years * 12);
        
        // Add fractional month based on days
        int startDay = startCal.get(Calendar.DAY_OF_MONTH);
        int endDay = endCal.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = startCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        double fractionalMonth = (endDay - startDay) / (double) daysInMonth;
        return months + fractionalMonth;
    }
    
    /**
     * Calculate the amount needed per period to reach a target
     */
    public static double calculateAmountPerPeriod(double remainingAmount, double totalPeriods, boolean isOverdue) {
        if (isOverdue) {
            switch ((int) totalPeriods) {
                case 0: // Daily
                    return remainingAmount / 30.0; // Spread over 30 days
                case 1: // Weekly
                    return remainingAmount / 4.0;  // Spread over 4 weeks
                case 2: // Monthly
                    return remainingAmount / 1.0;  // Complete within a month
                default:
                    return remainingAmount;
            }
        }
        
        // For regular periods, simply divide remaining by total periods
        // Ensure we don't divide by zero and handle very small periods
        double effectivePeriods = Math.max(1.0, totalPeriods);
        return remainingAmount / effectivePeriods;
    }
    
    /**
     * Get calendar set to start of day to avoid time zone issues
     */
    private static Calendar getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
    
    /**
     * Check if a date is in the past
     */
    public static boolean isDateInPast(Date date) {
        return getStartOfDay(date).before(getStartOfDay(new Date()));
    }
    
    /**
     * Get number of periods between dates based on period type
     */
    public static double getPeriodsRemaining(Date startDate, Date endDate, int periodType) {
        switch (periodType) {
            case 0: // Daily
                return Math.max(1, calculateDaysBetween(startDate, endDate));
            case 1: // Weekly
                return Math.max(0.143, calculateWeeksBetween(startDate, endDate)); // Min ~1 day
            case 2: // Monthly
                return Math.max(0.0333, calculateMonthsBetween(startDate, endDate)); // Min ~1 day
            default:
                return 1;
        }
    }
} 