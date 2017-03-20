package com.raul.rsd.android.popularmovies.domain;

public class MoviesList {

    // ------------------------- ATTRIBUTES --------------------------

    private int total_pages;
    private MovieLight[] results;
    private MovieLight[] cast;

    // ---------------------- GETTERS & SETTERS ----------------------

    public int getTotal_pages() {
        return total_pages;
    }

    public MovieLight[] getResults() {
        return results;
    }
    public MovieLight[] getCast() {
        return cast;
    }
}