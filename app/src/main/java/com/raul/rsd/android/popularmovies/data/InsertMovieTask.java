package com.raul.rsd.android.popularmovies.data;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import com.raul.rsd.android.popularmovies.domain.Movie;
import com.raul.rsd.android.popularmovies.utils.BitmapUtils;
import com.raul.rsd.android.popularmovies.utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.utils.TMDBUtils;
import javax.inject.Inject;

public class InsertMovieTask extends AsyncTask<Movie, Void, ContentValues> {

    private MoviesAsyncHandler.MoviesAsyncQueryHandler moviesHandler;

    @Inject
    InsertMovieTask(MoviesAsyncHandler.MoviesAsyncQueryHandler moviesHandler) {
        this.moviesHandler = moviesHandler;
    }

    @Override
    protected ContentValues doInBackground(Movie... movies) {
        Movie movie = movies[0];
        ContentValues values = TMDBUtils.getContentValuesFromMovie(movie);

        Bitmap backdrop = movie.getBackdrop();
        String backdropPath = movie.getBackdrop_path();
        if(backdrop == null && backdropPath != null)
            backdrop = NetworkUtils.getBackdropFromUri(backdropPath);

        byte[] backdropBytes = backdrop != null ? BitmapUtils.getBytesFromBitmap(backdrop) : new byte[0];
        values.put(MoviesContract.MoviesEntry.COLUMN_BACKDROP, backdropBytes);

        byte[] posterBytes = BitmapUtils.getBytesFromBitmap(movie.getPoster());
        values.put(MoviesContract.MoviesEntry.COLUMN_POSTER, posterBytes);
        return values;
    }

    @Override
    protected void onPostExecute(ContentValues movie) {
        moviesHandler.startInsert(MoviesAsyncHandler.INSERT_TOKEN, null,
                MoviesContract.CONTENT_URI, movie);
        super.onPostExecute(movie);
    }
}