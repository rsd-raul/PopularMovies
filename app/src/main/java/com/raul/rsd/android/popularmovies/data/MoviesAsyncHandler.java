package com.raul.rsd.android.popularmovies.data;


import android.content.AsyncQueryHandler;
import android.content.ContentResolver;

import javax.inject.Inject;

public class MoviesAsyncHandler {

    // --------------------------- VALUES ----------------------------

    public static final int UPDATE_TOKEN = 1, DELETE_TOKEN = 3;
    static final int INSERT_TOKEN = 2;

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