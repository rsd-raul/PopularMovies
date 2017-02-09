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

    /**
     * Generic constructor for a 2 options dialog following material guidelines.
     *
     * @param activity The activity you wish to build the dialog from
     * @param retryCallback The action to perform if the user decides to try again
     * @param titRes The title of the dialog
     * @param desRes The description of the dialog
     */
    private static void showSimpleDialog(AppCompatActivity activity,
                                        MaterialDialog.SingleButtonCallback retryCallback,
                                        int titRes, int desRes) {
        new MaterialDialog.Builder(activity)
                .title(titRes)
                .content(desRes)
                .positiveText(R.string.try_again)
                .negativeText(R.string.close)
                .onPositive(retryCallback)
                .onNegative((dialog, which) -> activity.finish())
                .cancelListener(dialogInterface -> activity.finish())
                .show();
    }
}

