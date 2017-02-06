package com.raul.rsd.android.popularmovies;

import java.util.Date;

public class Movie {

    // ------------------------- ATTRIBUTES --------------------------

    private long id;
    private String title;
    private String poster_path;     // Null TODO Both poster and backdrop can be null, handle that...
    private String backdrop_path;   // Null
    private Date release_date;
    private double vote_avg;
    private long vote_count;
    private int duration;
    private String synopsis;

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

    public Date getRelease_date() {
        return release_date;
    }
    public void setRelease_date(Date release_date) {
        this.release_date = release_date;
    }

    public double getVote_avg() {
        return vote_avg;
    }
    public void setVote_avg(double vote_avg) {
        this.vote_avg = vote_avg;
    }

    public long getVote_count() {
        return vote_count;
    }
    public void setVote_count(long vote_count) {
        this.vote_count = vote_count;
    }

    public String getSynopsis() {
        return synopsis;
    }
    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
}