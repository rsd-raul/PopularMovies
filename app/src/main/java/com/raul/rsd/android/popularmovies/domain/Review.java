package com.raul.rsd.android.popularmovies.domain;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable {

    // ------------------------- ATTRIBUTES --------------------------

    private String author;
    private String content;

    // ---------------------- GETTERS & SETTERS ----------------------

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    // ------------------------- PARCELABLE --------------------------

    static final Creator<Review> CREATOR = new Creator<Review> () {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    private Review(Parcel in) {
        author = in.readString();
        content = in.readString();
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(author);
        out.writeString(content);
    }
}
