package com.raul.rsd.android.popularmovies.domain;

public class Review {

    // ------------------------- ATTRIBUTES --------------------------

    private String author;
    private String content;

    // ---------------------- GETTERS & SETTERS ----------------------

    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
