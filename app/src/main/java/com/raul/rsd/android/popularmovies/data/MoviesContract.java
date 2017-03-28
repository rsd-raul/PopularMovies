package com.raul.rsd.android.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class MoviesContract {

    // ----------------------------- URI -----------------------------

    static final String AUTHORITY = "com.raul.rsd.android.popularmovies";
    static final String PATH_MOVIE = "movie";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // content://com.raul.rsd.android.popularmovies/movie
    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

    public static Uri getMovieUriWithId(long id){
        return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
    }

    // Avoid instantiation
    private MoviesContract(){}

    // -------------------------- DB TABLES --------------------------

    public static final class MoviesEntry implements BaseColumns {

        static final String TABLE_NAME = "movies";

        public static final String COLUMN_TITLE = "title",
                                   COLUMN_POSTER = "poster",
                                   COLUMN_BACKDROP = "backdrop",
                                   COLUMN_GENRES = "genres",
                                   COLUMN_RELEASE_DATE = "release_date",
                                   COLUMN_VOTE_AVERAGE = "vote_average",
                                   COLUMN_VOTE_COUNT = "vote_count",
                                   COLUMN_RUNTIME = "runtime",
                                   COLUMN_OVERVIEW = "overview",
                                   COLUMN_DOMINANT = "dominant_color",
                                   COLUMN_TIMESTAMP = "timestamp";
    }
}
