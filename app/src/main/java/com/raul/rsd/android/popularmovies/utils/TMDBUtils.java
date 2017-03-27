package com.raul.rsd.android.popularmovies.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.raul.rsd.android.popularmovies.data.MoviesContract.*;
import com.raul.rsd.android.popularmovies.domain.Actor;
import com.raul.rsd.android.popularmovies.domain.Genre;
import com.raul.rsd.android.popularmovies.domain.Movie;
import com.raul.rsd.android.popularmovies.R;
import com.raul.rsd.android.popularmovies.domain.MovieLight;
import com.raul.rsd.android.popularmovies.view.MovieActivity;
import com.raul.rsd.android.popularmovies.view.MainActivity;

import java.util.Date;

public abstract class TMDBUtils {

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

        Date releaseDate = movie.getRelease_date();
        if(releaseDate != null)
            builder.append(activity.getString(R.string.release_date_to_string))
                    .append(' ')
                    .append(DateUtils.getStringFromDate(releaseDate))
                    .append('\n');

        builder.append(activity.getString(R.string.duration_to_string))
                .append(' ')
                .append(String.format("%d %s", movie.getDuration(), activity.getString(R.string.time_minutes)));
        builder.append("\n\n\t\t#")
                .append(activity.getString(R.string.app_name));

        return builder.toString();
    }

    public static String toStringActor(Actor actor, AppCompatActivity activity){
        // Using builder to facilitate handling of different types of data
        StringBuilder builder = new StringBuilder();

        builder.append(activity.getString(R.string.name_to_string))
                .append(' ')
                .append(actor.getName())
                .append('\n');
        builder.append(activity.getString(R.string.biography_to_string))
                .append(' ')
                .append(actor.getBiography())
                .append('\n');
        Date birthday = actor.getBirthday();
        if(birthday != null)
            builder.append(activity.getString(R.string.birthday_to_string))
                    .append(' ')
                    .append(DateUtils.getStringFromDate(birthday))
                    .append('\n');

        String deathDay = actor.getDeathday();
        if(deathDay != null && deathDay.length() > 5) {
            Date deathDayDate = DateUtils.getDateFromTMDBSString(deathDay);
            builder.append(activity.getString(R.string.death_day_to_string))
                    .append(' ')
                    .append(DateUtils.getStringFromDate(deathDayDate))
                    .append('\n');
        }
        builder.append(activity.getString(R.string.place_of_birth_to_string))
                .append(' ')
                .append(actor.getPlace_of_birth())
                .append('\n');

        return builder.toString();
    }

    public static Movie extractMovieFromCursor(Cursor data){

        Movie movie = new Movie(data.getInt(MovieActivity.INDEX_ID));

        // Extract Movie details
        movie.setTitle(data.getString(MovieActivity.INDEX_TITLE));
        movie.setVote_avg(data.getDouble(MovieActivity.INDEX_VOTE_AVERAGE));
        movie.setVote_count(data.getLong(MovieActivity.INDEX_VOTE_COUNT));
        movie.setSynopsis(data.getString(MovieActivity.INDEX_OVERVIEW));
        movie.setDuration(data.getInt(MovieActivity.INDEX_RUNTIME));
        movie.setDominantBackdropColor(data.getInt(MovieActivity.INDEX_DOMINANT));

        Bitmap poster = BitmapUtils.getBitmapFromBytes(data.getBlob(MovieActivity.INDEX_POSTER));
        movie.setPoster(poster);

        Bitmap backdrop = BitmapUtils.getBitmapFromBytes(data.getBlob(MovieActivity.INDEX_BACKDROP));
        movie.setBackdrop(backdrop);

        Genre[] genres = getGenresFromString(data.getString(MovieActivity.INDEX_GENRES));
        movie.setGenres(genres);

        Date release_date = DateUtils.getDateFromTMDBSString(data.getString(MovieActivity.INDEX_RELEASE_DATE));
        movie.setRelease_date(release_date);

        return movie;
    }

    public static MovieLight[] extractLightMoviesFromCursor(Cursor data){
        MovieLight[] movies = new MovieLight[data.getCount()];

        for (int i = 0; i < movies.length; i++) {
            int id = data.getInt(MainActivity.INDEX_ID);
            Bitmap poster = BitmapUtils.getBitmapFromBytes(data.getBlob(MainActivity.INDEX_POSTER));
            movies[i] = new MovieLight(id, poster);

            data.moveToNext();
        }

        return movies;
    }

    /**
     * Populate ContentValues with the info we save into the DB, ignoring what we don't.
     *
     * @param movie The movie we will extract the data from.
     * @return A DB ready ContentValue based on the Movie
     */
    public static ContentValues getContentValuesFromMovie(Movie movie){
        ContentValues values = new ContentValues();

        values.put(MoviesEntry._ID, movie.getId());
        values.put(MoviesEntry.COLUMN_TITLE, movie.getTitle());
        String genresStr = TMDBUtils.getStringFromGenres(movie.getGenres());
        values.put(MoviesEntry.COLUMN_GENRES, genresStr);

        Date releaseDate = movie.getRelease_date();
        if(releaseDate != null)
            values.put(MoviesEntry.COLUMN_RELEASE_DATE, DateUtils.getTMDBStringFromDate(releaseDate));

        values.put(MoviesEntry.COLUMN_VOTE_AVERAGE, movie.getVote_avg());
        values.put(MoviesEntry.COLUMN_VOTE_COUNT, movie.getVote_count());
        values.put(MoviesEntry.COLUMN_RUNTIME, movie.getDuration());
        values.put(MoviesEntry.COLUMN_OVERVIEW, movie.getSynopsis());
        values.put(MoviesEntry.COLUMN_DOMINANT, movie.getDominantBackdropColor());

        Log.e("TMDBUtils", "getContentValuesFromMovie: " + values.size());
        return values;
    }

    /**
     * Compare the movies received and only save the attributes that are different between them.
     *
     * @param oldMovie The movie currently stored on the DB
     * @param newMovie The movie retrieved from TMDB
     * @return Only the values that have changed and the ID
     */
    public static ContentValues getContentValuesFromMovie(Movie oldMovie, Movie newMovie){
        ContentValues values = new ContentValues();
        values.put(MoviesEntry._ID, newMovie.getId());

        // Check values, only update if needed
        if(!oldMovie.getTitle().equals(newMovie.getTitle()))
            values.put(MoviesEntry.COLUMN_TITLE, newMovie.getTitle());
        else if( oldMovie.getVote_count() != newMovie.getVote_count())
            values.put(MoviesEntry.COLUMN_VOTE_COUNT, newMovie.getVote_count());
        else if( oldMovie.getDuration() != newMovie.getDuration())
            values.put(MoviesEntry.COLUMN_RUNTIME, newMovie.getDuration());
        else if( oldMovie.getSynopsis() != null && !oldMovie.getSynopsis().equals(newMovie.getSynopsis()))
            values.put(MoviesEntry.COLUMN_OVERVIEW, newMovie.getSynopsis());
        else if( oldMovie.getGenres() != null && oldMovie.getGenres() != newMovie.getGenres()) {
            String genresStr = TMDBUtils.getStringFromGenres(newMovie.getGenres());
            values.put(MoviesEntry.COLUMN_GENRES, genresStr);
        } else if( oldMovie.getRelease_date() != null && !oldMovie.getRelease_date().equals(newMovie.getRelease_date())) {
            String dateStr = DateUtils.getTMDBStringFromDate(newMovie.getRelease_date());
            values.put(MoviesEntry.COLUMN_RELEASE_DATE, dateStr);
        } else if( oldMovie.getSynopsis() != null && oldMovie.getVote_avg() != newMovie.getVote_avg())
            values.put(MoviesEntry.COLUMN_VOTE_AVERAGE, newMovie.getVote_avg());

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