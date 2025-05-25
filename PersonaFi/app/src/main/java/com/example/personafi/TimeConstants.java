package com.example.personafi;

/**
 * Shared time constants to ensure all calculations are consistent across the app
 */
public class TimeConstants {

    /**
     * Average number of days in a month (365.25/12)
     */
    public static final double DAYS_IN_MONTH = 30.4375;
    
    /**
     * Average number of weeks in a month (365.25/12/7)
     */
    public static final double WEEKS_IN_MONTH = 4.348214;
    
    // Static utility class, no instances needed
    private TimeConstants() {
    }
} 