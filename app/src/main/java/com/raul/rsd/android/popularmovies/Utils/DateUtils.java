package com.raul.rsd.android.popularmovies.Utils;

import android.util.Log;

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




}
