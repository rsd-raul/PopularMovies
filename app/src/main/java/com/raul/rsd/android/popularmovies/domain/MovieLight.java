package com.raul.rsd.android.popularmovies.domain;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class MovieLight implements Parcelable{

    // ------------------------- ATTRIBUTES --------------------------

    private long id;
    private String title;
    private String character;
    private String poster_path;
    private Bitmap poster;

    // ------------------------- CONSTRUCTOR -------------------------

    public MovieLight(long id, Bitmap poster) {
        this.id = id;
        this.poster = poster;
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

    public String getCharacter() {
        return character;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public Bitmap getPoster() {
        return poster;
    }
    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }

    // ------------------------- PARCELABLE --------------------------

    public static final Creator<MovieLight> CREATOR = new Creator<MovieLight>() {
        @Override
        public MovieLight createFromParcel(Parcel in) {
            return new MovieLight(in);
        }

        @Override
        public MovieLight[] newArray(int size) {
            return new MovieLight[size];
        }
    };

    private MovieLight(Parcel in) {
        id = in.readLong();
        title = in.readString();
        poster_path = in.readString();
        character = in.readString();
        poster = null;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(title);
        out.writeString(poster_path);
        out.writeString(character);
    }
}