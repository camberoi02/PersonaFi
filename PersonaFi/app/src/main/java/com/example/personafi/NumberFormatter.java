package com.example.personafi;

public class NumberFormatter {
    public static String formatCompactNumber(long number) {
        if (number < 1000) return String.valueOf(number);
        
        // Calculate the exponent (1 for thousands, 2 for millions, etc.)
        int exp = (int) (Math.log(number) / Math.log(1000));
        
        // Get the appropriate suffix
        String suffix = "kmbt";
        if (exp > suffix.length()) {
            exp = suffix.length();
        }
        
        // Calculate the value
        double value = number / Math.pow(1000, exp);
        
        // Format the number
        if (value % 1 == 0) {
            return String.format("%.0f%c", value, suffix.charAt(exp - 1));
        } else {
            return String.format("%.1f%c", value, suffix.charAt(exp - 1));
        }
    }

    public static String formatCompactNumber(double number) {
        return formatCompactNumber((long) number);
    }
} 