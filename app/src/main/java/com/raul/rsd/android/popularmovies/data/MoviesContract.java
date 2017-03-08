package com.raul.rsd.android.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MoviesContract {

    // ------------------------- PARCELABLE --------------------------
    // ----------------------------- URI -----------------------------

    public static final String AUTHORITY = "com.raul.rsd.android.popularmovies";
    public static final String PATH_MOVIE = "movie";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // content://com.raul.rsd.android.popularmovies/movie
    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

    public static Uri getMovieUriWithId(long id){
        return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
    }

    // -------------------------- DB TABLES --------------------------

    public static final class MoviesEntry implements BaseColumns {

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_BACKDROP = "backdrop";
        public static final String COLUMN_GENRES = "genres";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}
