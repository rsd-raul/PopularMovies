package com.raul.rsd.android.popularmovies.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import com.raul.rsd.android.popularmovies.data.MoviesContract.*;
import com.raul.rsd.android.popularmovies.domain.Genre;
import com.raul.rsd.android.popularmovies.domain.Movie;
import com.raul.rsd.android.popularmovies.R;
import com.raul.rsd.android.popularmovies.view.DetailsActivity;
import java.util.Date;

public final class TMDBUtils {

    // -------------------------- USE CASES --------------------------

    /**
     * Auxiliary method to obtain a String representation of a given Movie based
     * on the user's locale.
     *
     * @param movie Movie we wish to convert to String
     * @param activity Activity to obtain Locale based headers
     * @return String representation of the Movie
     */
    @SuppressWarnings("all")
    public static String toStringMovie(Movie movie, AppCompatActivity activity){
        // Using builder to facilitate handling of different types of data
        StringBuilder builder = new StringBuilder();

        builder.append(activity.getString(R.string.title_to_string))
                .append(' ')
                .append(movie.getTitle())
                .append('\n');
        builder.append(activity.getString(R.string.synopsis_to_string))
                .append(' ')
                .append(movie.getSynopsis())
                .append('\n');
        builder.append(activity.getString(R.string.vote_avg_to_string))
                .append(' ')
                .append(movie.getVote_avg())
                .append('\n');
        builder.append(activity.getString(R.string.release_date_to_string))
                .append(' ')
                .append(DateUtils.getStringFromDate(movie.getRelease_date()))
                .append('\n');
        builder.append(activity.getString(R.string.duration_to_string))
                .append(' ')
                .append(String.format("%d %s", movie.getDuration(), activity.getString(R.string.time_minutes)));
        builder.append("\n\n\t\t#")
                .append(activity.getString(R.string.app_name));

        return builder.toString();
    }

    public static Movie extractMovieFromCursor(Cursor data){
        data.moveToFirst();

        Movie movie = new Movie();

        // Extract Movie details
        movie.setId(data.getInt(DetailsActivity.INDEX_ID));
        movie.setTitle(data.getString(DetailsActivity.INDEX_TITLE));
        movie.setVote_avg(data.getDouble(DetailsActivity.INDEX_VOTE_AVERAGE));
        movie.setVote_count(data.getLong(DetailsActivity.INDEX_VOTE_COUNT));
        movie.setSynopsis(data.getString(DetailsActivity.INDEX_OVERVIEW));
        movie.setDuration(data.getInt(DetailsActivity.INDEX_RUNTIME));

        Bitmap poster = BitmapUtils.geBbitmapFromBytes(data.getBlob(DetailsActivity.INDEX_POSTER));
        movie.setPoster(poster);
        poster.recycle();

        Bitmap backdrop = BitmapUtils.geBbitmapFromBytes(data.getBlob(DetailsActivity.INDEX_BACKDROP));
        movie.setBackdrop(backdrop);
        backdrop.recycle();

        Genre[] genres = getGenresFromString(data.getString(DetailsActivity.INDEX_GENRES));
        movie.setGenres(genres);

        Date release_date = DateUtils.getDateFromTMDBSString(data.getString(DetailsActivity.INDEX_RELEASE_DATE));
        movie.setRelease_date(release_date);

        return movie;
    }

    public static ContentValues getContentValuesFromMovie(Movie movie){
        ContentValues values = new ContentValues();

        values.put(MoviesEntry.COLUMN_TITLE, movie.getTitle());
        String genresStr = TMDBUtils.getStringFromGenres(movie.getGenres());
        values.put(MoviesEntry.COLUMN_GENRES, genresStr);
        String dateStr = DateUtils.getStringFromDate(movie.getRelease_date());
        values.put(MoviesEntry.COLUMN_RELEASE_DATE, dateStr);
        values.put(MoviesEntry.COLUMN_VOTE_AVERAGE, movie.getVote_avg());
        values.put(MoviesEntry.COLUMN_VOTE_COUNT, movie.getVote_count());
        values.put(MoviesEntry.COLUMN_RUNTIME, movie.getDuration());
        values.put(MoviesEntry.COLUMN_OVERVIEW, movie.getSynopsis());

        return values;
    }

    public static String getStringFromGenres(Genre[] genres){
        String genresStr = "";

        for(int i = 0; i < genres.length; i++){
            if(i > 0)
                genresStr += " - ";
            genresStr += genres[i].getTitle();
        }

        return genresStr;
    }

    private static Genre[] getGenresFromString(String genresStr){
        String[] genreArrayStr = genresStr.split(" - ");
        int genreSize = genreArrayStr.length;

        Genre[] genres = new Genre[genreSize];
        for (int i = 0; i < genreSize; i++)
            genres[i] = new Genre(genreArrayStr[i]);

        return genres;
    }
}