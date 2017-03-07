package com.raul.rsd.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

public class MoviesProvider extends ContentProvider {

    // --------------------------- VALUES ----------------------------

    public static final int MOVIE = 100;
    public static final int MOVIE_WITH_ID = 101;

    // ------------------------- ATTRIBUTES --------------------------

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    @Inject
    MoviesDbHelper mDBHelper;

    // ------------------------- CONSTRUCTOR -------------------------

    @Override
    public boolean onCreate() {
        return true;
    }

    // -------------------------- AUXILIARY --------------------------

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Tasks Directory
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIE, MOVIE);
        // Tasks with id
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);

        return uriMatcher;
    }

    // ------------------------ CRUD METHODS -------------------------

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
