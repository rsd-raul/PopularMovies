package com.raul.rsd.android.popularmovies.utils;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.raul.rsd.android.popularmovies.R;

public abstract class DialogsUtils{

    public static void showFetchingDataDialog(AppCompatActivity activity,
                                              MaterialDialog.SingleButtonCallback retry){
        showComplexDialog(activity, retry, null, R.string.no_fetch_title,
                R.string.no_fetch_content, R.string.try_again, -1);
    }

    public static void showErrorDialog(AppCompatActivity activity,
                                       MaterialDialog.SingleButtonCallback retry) {
        showComplexDialog(activity, retry, null, R.string.no_network_title,
                R.string.no_network_content, R.string.try_again, -1);
    }

    public static void showNetworkDialogMainActivity(AppCompatActivity activity,
                                                     MaterialDialog.SingleButtonCallback toFav,
                                                     MaterialDialog.SingleButtonCallback retry){
        showComplexDialog(activity, toFav, retry, R.string.no_network_title,
                R.string.no_network_go_favourites, R.string.favourites, R.string.try_again);
    }

    /**
     *
     // FIXME Update
     * Generic constructor for a 2 options dialog following material guidelines.
     *
     * @param activity The activity you wish to build the dialog from
     * @param titRes The title of the dialog
     * @param desRes The description of the dialog
     */
    private static void showComplexDialog(AppCompatActivity activity,
                                          MaterialDialog.SingleButtonCallback positiveCallback,
                                          MaterialDialog.SingleButtonCallback negativeCallback,
                                          int titRes, int desRes, int posRes, int negRes) {

        MaterialDialog.Builder dialog = new MaterialDialog.Builder(activity)
                .title(titRes)
                .content(desRes)
                .positiveText(posRes)
                .onPositive(positiveCallback)
                .cancelable(false);

        if(negativeCallback == null) {
            Log.e("XXX", "showComplexDialog: AAAAA");
            dialog.negativeText(R.string.close);
            dialog.onNegative((dialog1, which) -> activity.finish());
        }else {
            Log.e("XXX", "showComplexDialog: BBBBB");
            dialog.negativeText(negRes);
            dialog.onNegative(negativeCallback);
            dialog.neutralText(R.string.close);
            dialog.onNeutral((dialog1, which) -> activity.finish());
        }

        dialog.show();
    }

    public static void showBasicDialog(AppCompatActivity activity, String title, String content) {
        new MaterialDialog.Builder(activity)
                .title(title)
                .content(content)
                .show();
    }
}

