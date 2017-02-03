package com.raul.rsd.android.popularmovies.Utils;


import android.graphics.Bitmap;

public class UIUtils {


    // Prueba Palette, de Google
    // http://stackoverflow.com/questions/8471236/finding-the-dominant-color-of-an-image-in-an-android-drawable
    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }
}
