package com.raul.rsd.android.popularmovies.Utils;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.raul.rsd.android.popularmovies.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

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
    static Date getDateFromString(String date){
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

    /**
     * Translate a given number of minutes into hours and minutes
     *
     * @param minutes Total number of minutes
     * @param activity Context to obtain locale based units
     * @return Hours and minutes formated
     */
    @SuppressWarnings("all")
    public static String getDurationFromMinutes(int minutes, AppCompatActivity activity){
        String duration = "";

        double hoursDouble = Math.floor(minutes/60.0);
        if(hoursDouble != 0)
            duration = String.format("%.0f %s ", hoursDouble, activity.getString(R.string.time_hours));

        int minutesInt = minutes%60;
        duration += String.format("%d %s", minutesInt, activity.getString(R.string.time_minutes));

        return duration;
    }
}
