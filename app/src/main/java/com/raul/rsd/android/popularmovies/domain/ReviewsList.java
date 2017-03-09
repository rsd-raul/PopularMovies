package com.raul.rsd.android.popularmovies.domain;

public class ReviewsList {

    // ------------------------- ATTRIBUTES --------------------------

    private Review[] results;
    private int page;
    private int total_pages;

    // ---------------------- GETTERS & SETTERS ----------------------

    public Review[] getResults() {
        return results;
    }

    public int getPage() {
        return page;
    }

    public int getTotal_pages() {
        return total_pages;
    }
}
