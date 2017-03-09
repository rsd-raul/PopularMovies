package com.raul.rsd.android.popularmovies.domain;

public class Video {

    // ------------------------- ATTRIBUTES --------------------------

    private String key;
    private String name;
    private String site;
    private int size;
    private String type;

    // ---------------------- GETTERS & SETTERS ----------------------

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }
    public void setSite(String site) {
        this.site = site;
    }

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
