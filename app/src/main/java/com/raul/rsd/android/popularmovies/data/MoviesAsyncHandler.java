package com.raul.rsd.android.popularmovies.data;


import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.raul.rsd.android.popularmovies.view.DetailsActivity;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class MoviesAsyncHandler {

    // --------------------------- VALUES ----------------------------

    public static final int UPDATE_TOKEN = 1, INSERT_TOKEN = 2, DELETE_TOKEN = 3;

    // ------------------------- CONSTRUCTOR -------------------------

    @Inject
    public MoviesAsyncHandler() { }

    // -------------------------- USE CASES --------------------------

    public MoviesAsyncQueryHandler getHandler(ContentResolver cr) {
        return new MoviesAsyncQueryHandler(cr);
    }

    // -------------------------- INTERFACE --------------------------

    public interface AsyncQueryListener {
        void onQueryComplete(int token, Object cookie, Cursor cursor);
    }

    // ---------------------------- CLASS ----------------------------

    public static class MoviesAsyncQueryHandler extends AsyncQueryHandler {


        // ------------------------- ATTRIBUTES --------------------------

        private WeakReference<AsyncQueryListener> mListener;

        // ------------------------- CONSTRUCTOR -------------------------

        @Inject
        MoviesAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        // FIXME Pasar a limpio y borrar
        public void setAsyncQueryListener(AsyncQueryListener listener) {
            mListener = new WeakReference<>(listener);
        }

        // -------------------------- AUXILIARY --------------------------

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Log.e("AAAAA", "onQueryComplete: ");
            final AsyncQueryListener listener = mListener.get();
            if (listener != null)
                listener.onQueryComplete(token, cookie, cursor);
            else if (cursor != null)
                cursor.close();
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            Log.e("AAAAA", "onDeleteComplete: ");
            super.onDeleteComplete(token, cookie, result);
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            Log.e("AAAAA", "onInsertComplete: ");
            super.onInsertComplete(token, cookie, uri);
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            Log.e("AAAAA", "onUpdateComplete: ");
            super.onUpdateComplete(token, cookie, result);
        }
    }
}