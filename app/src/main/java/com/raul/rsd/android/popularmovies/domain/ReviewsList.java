package com.raul.rsd.android.popularmovies.domain;

import android.os.Parcel;
import android.os.Parcelable;

public class ReviewsList implements Parcelable{

    // ------------------------- ATTRIBUTES --------------------------

    private Review[] results;

    // ---------------------- GETTERS & SETTERS ----------------------

    public Review[] getResults() {
        return results;
    }

    // ------------------------- PARCELABLE --------------------------

    static final Creator<ReviewsList> CREATOR = new Creator<ReviewsList> () {
        @Override
        public ReviewsList createFromParcel(Parcel in) {
            return new ReviewsList(in);
        }

        @Override
        public ReviewsList[] newArray(int size) {
            return new ReviewsList[size];
        }
    };

    private ReviewsList(Parcel in) {
        results = new Review[in.readInt()];
        in.readTypedArray(results, Review.CREATOR);
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(results.length);
        out.writeTypedArray(results, flags);
    }
}
