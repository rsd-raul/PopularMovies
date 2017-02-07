package com.raul.rsd.android.popularmovies.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.view.Window;
import android.view.WindowManager;

import com.raul.rsd.android.popularmovies.R;


public class UIUtils {


    public static void setSubtitle(AppCompatActivity activity, String activeFilter){
        // Format the filter
        String filter = activeFilter.replace('_',' ');
        filter = filter.substring(0, 1).toUpperCase() + filter.substring(1);

        // Set the subtitle
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar != null)
            actionBar.setSubtitle("Filter: " + filter);
    }

    public static int getDominantColor(Bitmap bitmap, Context context) {
        Palette palette = Palette.from(bitmap).generate();
        return palette.getDominantColor(ContextCompat.getColor(context, R.color.colorPrimary));
    }

    public static boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        return darkness >= 0.5;
    }

    // less than 1.0f to darken -> 0.7 seems ideal

    /**
     * Modify a color programmatically in order to obtain different shades of the same color.
     *
     * @param color color to modify
     * @param factor more than 1 makes the color lighter, less than 1 makes it darker
     * @return modified color
     */
    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        // Math.min handles that the value does not exceeds 255
        return Color.argb(a, Math.min(r,255), Math.min(g,255), Math.min(b,255));
    }

    // TODO - Review for API < LOLLIPOP
    // http://stackoverflow.com/questions/22192291/how-to-change-the-status-bar-color-in-android
    public static void adaptAppBarAndStatusBarColors(AppCompatActivity activity, int color){
        int colorPrimary = ContextCompat.getColor(activity, color);

        // Get the ActionBar and customize color
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(colorPrimary));

        // If the Android version allows, get the StatusBar and customize it
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;

        int colorPrimaryDark = manipulateColor(colorPrimary, 0.7f);
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(colorPrimaryDark);
    }
}
