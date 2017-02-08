package com.raul.rsd.android.popularmovies.Utils;

import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.raul.rsd.android.popularmovies.R;

public abstract class DialogsUtils{

    public static void showFetchingDataDialog(AppCompatActivity activity,
                                              MaterialDialog.SingleButtonCallback callback){
        showSimpleDialog(activity, callback, R.string.no_fetch_title, R.string.no_fetch_content);
    }

    public static void showErrorDialog(AppCompatActivity activity,
                                       MaterialDialog.SingleButtonCallback callback) {
        showSimpleDialog(activity, callback, R.string.no_network_title, R.string.no_network_content);
    }

    private static void showSimpleDialog(AppCompatActivity activity,
                                        MaterialDialog.SingleButtonCallback callback,
                                        int titRes, int desRes) {
        new MaterialDialog.Builder(activity)
                .title(titRes)
                .content(desRes)
                .positiveText(R.string.try_again)
                .negativeText(R.string.close)
                .onPositive(callback)
                .onNegative((dialog, which) -> activity.finish())
                .cancelListener(dialogInterface -> activity.finish())
                .show();
    }
}

