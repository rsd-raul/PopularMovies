package com.raul.rsd.android.popularmovies.domain;


import java.util.ArrayList;

public class MoviesList {

    private int total_pages;
    private ArrayList<MovieLight> results;

    public int getTotal_pages() {
        return total_pages;
    }

    public ArrayList<MovieLight> getResults() {
        return results;
    }
}