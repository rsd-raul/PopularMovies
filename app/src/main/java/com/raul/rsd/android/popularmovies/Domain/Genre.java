package com.raul.rsd.android.popularmovies.Domain;

public class Genre {

    // ------------------------- ATTRIBUTES --------------------------

    private long id;
    private String name;

    // ------------------------- CONSTRUCTOR -------------------------

    public Genre() {
    }

    // ---------------------- GETTERS & SETTERS ----------------------

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return name;
    }
    public void setTitle(String title) {
        this.name = title;
    }
}
