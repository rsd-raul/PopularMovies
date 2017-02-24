package com.raul.rsd.android.popularmovies.Domain;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieLight implements Parcelable{

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

    // ------------------------- PARCELABLE --------------------------

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
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
        poster_path = in.readString();
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(poster_path);
    }
}