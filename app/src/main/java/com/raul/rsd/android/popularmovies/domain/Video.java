package com.raul.rsd.android.popularmovies.domain;

import android.os.Parcel;
import android.os.Parcelable;

public class Video implements Parcelable{

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

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }

    // ------------------------- PARCELABLE --------------------------

    static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    private Video(Parcel in) {
        key = in.readString();
        name = in.readString();
        site = in.readString();
        size = in.readInt();
        type = in.readString();
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(key);
        out.writeString(name);
        out.writeString(site);
        out.writeInt(size);
        out.writeString(type);
    }
}
