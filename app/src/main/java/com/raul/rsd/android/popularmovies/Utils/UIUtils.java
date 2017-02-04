package com.raul.rsd.android.popularmovies.Utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;


public class UIUtils {


    // Prueba Palette, de Google
    // http://stackoverflow.com/questions/8471236/finding-the-dominant-color-of-an-image-in-an-android-drawable
    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
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
