package com.raul.rsd.android.popularmovies.data;


import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

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

    // ---------------------------- CLASS ----------------------------

    public static class MoviesAsyncQueryHandler extends AsyncQueryHandler {

        @Inject
        MoviesAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }
    }
}