package com.raul.rsd.android.popularmovies.Domain;

import android.os.Parcel;
import android.os.Parcelable;

public class Genre implements Parcelable{

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

    // ------------------------- PARCELABLE --------------------------

    public static final Creator CREATOR = new Creator() {
        @Override
        public Genre createFromParcel(Parcel in) {
            return new Genre(in);
        }

        @Override
        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };

    private Genre(Parcel in) {
        id = in.readLong();
        name = in.readString();
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(name);
    }
}
