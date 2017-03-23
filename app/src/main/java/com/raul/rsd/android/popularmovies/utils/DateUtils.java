package com.raul.rsd.android.popularmovies.utils;

import android.util.Log;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateUtils {

    private final static String TAG = "DateUtils";

    /**
     * Get locale based String representation of a date
     *
     * @param date Source date
     * @return Date with Locale format
     */
    public static String getStringFromDate(Date date){
        return DateFormat.getDateInstance().format(date);
    }

    /**
     * Parse from the string representation of a date to the Date type following the format
     * stablished by TMDB API.
     *
     * @param date String to convert into a Date
     * @return Date obtained from String
     */
    @SuppressWarnings("all")
    public static Date getDateFromTMDBSString(String date){
        Date result = null;
        try{
            result = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        }catch (ParseException ex){
            Log.e(TAG, "getDateFromString - ParseException with format yyyy-MM-dd: " + date, ex);
        }
        return result;
    }

    /**
     * Parse a Date type to the string representation based on the format established by TMDB API
     *
     * @param date Source date
     * @return String representation with TMDB format
     */
    @SuppressWarnings("all")
    public static String getTMDBStringFromDate(Date date){
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    @SuppressWarnings("deprecation")
    public static int calculateYearsBetweenDates(Date startDate, Date endDate) {
        int startDay = startDate.getDate(), endDay = endDate.getDate();
        int startMonth = startDate.getMonth(), endMonth = endDate.getMonth();
        int startYear = startDate.getYear(), endYear = endDate.getYear();

        int result = endYear - startYear;
        if (startMonth > endMonth)
            result--;
        else if (startMonth == endMonth && startDay > endDay)
            result--;
        return result;
    }
}
