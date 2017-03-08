package com.raul.rsd.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Inject;

import static com.raul.rsd.android.popularmovies.data.MoviesContract.*;

public class MoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    @Inject
    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " +
                MoviesEntry.TABLE_NAME + " (" +
                MoviesEntry._ID 				+ " INTEGER PRIMARY KEY UNIQUE, " +
                MoviesEntry.COLUMN_TITLE        + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_POSTER       + " BLOB, " +   // Store image for offline use
                MoviesEntry.COLUMN_BACKDROP	    + " BLOB, " +
                MoviesEntry.COLUMN_GENRES	    + " TEXT, " +   // No offline search -> No need to save genres id
                MoviesEntry.COLUMN_RELEASE_DATE + " TEXT, " +   // TODO Double check release date type
                MoviesEntry.COLUMN_VOTE_AVERAGE + " REAL, " +
                MoviesEntry.COLUMN_VOTE_COUNT   + " REAL, " +
                MoviesEntry.COLUMN_RUNTIME      + " INTEGER, " +
                MoviesEntry.COLUMN_OVERVIEW     + " TEXT" +
        ");";
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}