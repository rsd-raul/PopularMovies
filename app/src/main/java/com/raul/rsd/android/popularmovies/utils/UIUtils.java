package com.raul.rsd.android.popularmovies.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import com.raul.rsd.android.popularmovies.R;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

public abstract class UIUtils {

    public static CharSequence getCustomDurationString(Context context, int totalMinutes){
        int hours = (int) Math.floor(totalMinutes/60.0);
        int minutes = totalMinutes%60;
        String adjust = minutes<10 ? "0" : "";

        Resources resources = context.getResources();
        String[] items = {String.valueOf(hours), context.getString(R.string.hour_unit),
                adjust + String.valueOf(minutes), context.getString(R.string.minute_unit)};

        CharSequence finalText = "";
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            int textSize, textColor, itemLength = item.length();
            if( i==0 || i==2 ){
                textSize = resources.getDimensionPixelSize(R.dimen.hour_size);
                textColor = Color.BLACK;
            } else {
                textSize = resources.getDimensionPixelSize(R.dimen.minute_size);
                textColor = Color.GRAY;
            }

            SpannableString span = new SpannableString(item);
            span.setSpan(new AbsoluteSizeSpan(textSize), 0, itemLength, SPAN_INCLUSIVE_INCLUSIVE);
            span.setSpan(new ForegroundColorSpan(textColor), 0, itemLength, 0);

            finalText = TextUtils.concat(finalText, span);
        }
        return finalText;
    }

    /**
     * Sets the active sort as a subtitle in the ActionBar.
     *
     * @param activity The Activity that contains the Actionbar
     * @param activeFilter The filter currently active
     */
    public static void setSubtitle(AppCompatActivity activity, String activeFilter){
        // Format the filter
        String filter = activeFilter.replace('_',' ');
        filter = filter.substring(0, 1).toUpperCase() + filter.substring(1);

        // Set the subtitle
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar != null)
            actionBar.setSubtitle("Filter: " + filter);
    }

    /**
     * Extract the predominant color from an Image (Bitmap).
     *
     * @param bitmap The image we wish to obtain the color from
     * @param context Needed to access a default color to return if Palette cannot obtain a dominant
     * @return The dominant color on that picture
     */
    public static int getDominantColor(Bitmap bitmap, Context context) {
        Palette palette = Palette.from(bitmap).generate();
        return palette.getDominantColor(ContextCompat.getColor(context, R.color.colorPrimary));
    }

    /**
     * Method to discern whether a given Color is considered Dark or not
     *
     * @param color Color to analyse
     * @return true if the color is considered Dark, false otherwise
     */
    public static boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        return darkness >= 0.5;
    }

    /**
     * Method to customize the Actionbar and Statusbar with a given color and a darker version of
     * itself (following Material Guidelines -> primaryColor, primaryDark).
     *
     * @param activity The Activity containing the Actionbar
     * @param colorPrimary The color we wish to use as primary
     */
    public static void adaptAppBarAndStatusBarColors(AppCompatActivity activity, int colorPrimary){
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

    /**
     * Modify a color programmatically in order to obtain different shades of the same color.
     *
     * @param color color to modify
     * @param factor more than 1 makes the color lighter, less than 1 makes it darker (0.7f for primaryDark)
     * @return modified color
     */
    private static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        // Math.min handles that the value does not exceeds 255
        return Color.argb(a, Math.min(r,255), Math.min(g,255), Math.min(b,255));
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    private static float convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static Bitmap getDpBasedBitmap(Bitmap source, int marginXDp, int marginYDp,
                                                                int widthDp, int heightDp){
        int[] valuesDp = {marginXDp, marginYDp, widthDp, heightDp};
        int deviceWidthPx = Resources.getSystem().getDisplayMetrics().widthPixels;
        int backdropWidthPx = source.getWidth();
        double ratioPx = deviceWidthPx * 1.0 / backdropWidthPx;

        // Convert DP values to real source based Pixels
        int[] valuesPx = new int[4];
        for (int i = 0; i < valuesDp.length; i++) {
            float valueInPx = convertDpToPixel(valuesDp[i]);
            valueInPx *= ratioPx;
            valuesPx[i] = Math.round(valueInPx);
        }

        return Bitmap.createBitmap(source, valuesPx[0], valuesPx[1], valuesPx[2], valuesPx[3]);
    }
}
