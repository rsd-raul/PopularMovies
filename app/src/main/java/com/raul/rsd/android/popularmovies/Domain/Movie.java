package com.raul.rsd.android.popularmovies.Domain;

import java.util.Arrays;
import java.util.Date;

public class Movie {

    // ------------------------- ATTRIBUTES --------------------------

    private long id;
    private String title;
    private String poster_path;
    private String backdrop_path;
    private Genre[] genres;             // Popular/TopRated doesn't provide genres name
    private Date release_date;
    private double vote_average;
    private long vote_count;
    private int runtime;               // Popular/TopRated doesn't provide duration
    private String overview;

    // ------------------------- CONSTRUCTOR -------------------------

    public Movie() {
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
    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }
    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public Genre[] getGenres() {
        return genres;
    }
    public void setGenres(Genre[] genres) {
        this.genres = genres;
    }

    public Date getRelease_date() {
        return release_date;
    }
    public void setRelease_date(Date release_date) {
        this.release_date = release_date;
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


    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", poster_path='" + poster_path + '\'' +
                ", backdrop_path='" + backdrop_path + '\'' +
                ", genres=" + Arrays.toString(genres) +
                ", release_date=" + release_date +
                ", vote_avg=" + getVote_avg() +
                ", vote_count=" + vote_count +
                ", duration=" + getDuration() +
                ", synopsis='" + getSynopsis() + '\'' +
                '}';
    }
}