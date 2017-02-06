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

    public static String getStringFromDate(Date date){
        return DateFormat.getDateInstance().format(date);
    }

    public static Date getDateFromString(String date){
        Date result = null;
        try{
            result = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        }catch (ParseException ex){
            Log.e(TAG, "getDateFromString - ParseException with format yyyy-MM-dd: " + date, ex);
        }
        return result;
    }

    public static String getTMDBStringFromDate(Date date){

        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

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
