package com.raul.rsd.android.popularmovies.domain;

public class MoviesList {

    // ------------------------- ATTRIBUTES --------------------------

    private MovieLight[] results;
    private MovieLight[] cast;

    // ---------------------- GETTERS & SETTERS ----------------------

    public MovieLight[] getResults() {
        return results;
    }
    public MovieLight[] getCast() {
        return cast;
    }
}