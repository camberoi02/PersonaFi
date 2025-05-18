package com.example.personifi;

import androidx.room.TypeConverter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Date converter for Room database and debug utilities
 */
public class DateConverter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    /**
     * Converts a timestamp to a Date object.
     * 
     * @param value The timestamp in milliseconds
     * @return The Date object
     */
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    /**
     * Converts a Date object to a timestamp.
     * 
     * @param date The Date object
     * @return The timestamp in milliseconds
     */
    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
    
    /**
     * Debug utility to format a date readably
     */
    public static String formatDebug(Date date) {
        if (date == null) return "null";
        return DATE_FORMAT.format(date);
    }
}