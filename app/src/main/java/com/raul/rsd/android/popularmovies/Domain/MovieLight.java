package com.raul.rsd.android.popularmovies.Domain;

public class MovieLight {

    // ------------------------- ATTRIBUTES --------------------------

    private long id;
    private String poster_path;

    // ------------------------- CONSTRUCTOR -------------------------

    public MovieLight() {
    }

    // ---------------------- GETTERS & SETTERS ----------------------

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getPoster_path() {
        return poster_path;
    }
    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }
}