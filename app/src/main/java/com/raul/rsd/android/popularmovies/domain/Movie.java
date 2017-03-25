package com.raul.rsd.android.popularmovies.domain;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import com.raul.rsd.android.popularmovies.utils.DateUtils;
import java.util.Date;

public class Movie implements Parcelable{

    // ------------------------- ATTRIBUTES --------------------------

    private long id;
    private String title;
    private String poster_path;
    private Bitmap poster;
    private String backdrop_path;
    private Bitmap backdrop;
    private Genre[] genres;             // Popular/TopRated doesn't provide genres name
    private String release_date;
    private double vote_average;
    private long vote_count;
    private int runtime;               // Popular/TopRated doesn't provide duration
    private String overview;
    private VideosList videos;
    private ReviewsList reviews;
    private ActorList credits;
    private int dominantBackdropColor;

    // ------------------------- CONSTRUCTOR -------------------------

    public Movie(long id) {
        this.id = id;
    }

    // ---------------------- GETTERS & SETTERS ----------------------

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public Bitmap getPoster() {
        return poster;
    }
    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public Bitmap getBackdrop() {
        return backdrop;
    }
    public void setBackdrop(Bitmap backdrop) {
        this.backdrop = backdrop;
    }

    public Genre[] getGenres() {
        return genres;
    }
    public void setGenres(Genre[] genres) {
        this.genres = genres;
    }

    public Date getRelease_date() {
        if(release_date == null || release_date.length() != 10)
            return null;
        return DateUtils.getDateFromTMDBSString(release_date);
    }
    public void setRelease_date(Date release_date) {
        if(release_date == null)
            return;
        this.release_date = DateUtils.getTMDBStringFromDate(release_date);
    }

    public double getVote_avg() {
        return vote_average;
    }
    public void setVote_avg(double vote_avg) {
        this.vote_average = vote_avg;
    }

    public long getVote_count() {
        return vote_count;
    }
    public void setVote_count(long vote_count) {
        this.vote_count = vote_count;
    }

    public String getSynopsis() {
        return overview;
    }
    public void setSynopsis(String synopsis) {
        this.overview = synopsis;
    }

    public int getDuration() {
        return runtime;
    }
    public void setDuration(int duration) {
        this.runtime = duration;
    }

    public int getDominantBackdropColor() {
        return dominantBackdropColor;
    }
    public void setDominantBackdropColor(int dominantBackdropColor) {
        this.dominantBackdropColor = dominantBackdropColor;
    }

    public Video[] getVideos() {
        return videos != null ? videos.getResults() : null;
    }
    public Review[] getReviews() {
        return reviews != null ? reviews.getResults() : null;
    }
    public Actor[] getCast() {
        return credits != null ? credits.getCast() : null;
    }

    // ------------------------- PARCELABLE --------------------------

    // FIXME add images to parcelable

    static final Creator<Movie> CREATOR = new Creator<Movie> () {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private Movie(Parcel in) {
        id = in.readLong();
        title = in.readString();
        poster_path = in.readString();
        backdrop_path = in.readString();

        genres = new Genre[in.readInt()];
        in.readTypedArray(genres, Genre.CREATOR);

        release_date = in.readString();
        vote_average = in.readDouble();
        vote_count = in.readLong();
        runtime = in.readInt();
        overview = in.readString();
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(title);
        out.writeString(poster_path);
        out.writeString(backdrop_path);

        out.writeInt(genres.length);
        out.writeTypedArray(genres, flags);

        out.writeString(release_date);
        out.writeDouble(vote_average);
        out.writeLong(vote_count);
        out.writeInt(runtime);
        out.writeString(overview);
    }
}