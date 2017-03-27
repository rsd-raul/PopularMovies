package com.raul.rsd.android.popularmovies.domain;

import android.os.Parcel;
import android.os.Parcelable;

public class MoviesList implements Parcelable{

    // ------------------------- ATTRIBUTES --------------------------

    private MovieLight[] results;
    private MovieLight[] cast;

    // ---------------------- GETTERS & SETTERS ----------------------

    public MovieLight[] getResults() {
        return results;
    }
    public MovieLight[] getCast() {
        return cast;
    }

    // ------------------------- PARCELABLE --------------------------

    static final Parcelable.Creator<MoviesList> CREATOR = new Parcelable.Creator<MoviesList>() {
        @Override
        public MoviesList createFromParcel(Parcel in) {
            return new MoviesList(in);
        }

        @Override
        public MoviesList[] newArray(int size) {
            return new MoviesList[size];
        }
    };

    private MoviesList(Parcel in) {
        results = new MovieLight[in.readInt()];
        in.readTypedArray(results, MovieLight.CREATOR);

        cast = new MovieLight[in.readInt()];
        in.readTypedArray(cast, MovieLight.CREATOR);
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        if(results == null)
            results = new MovieLight[0];
        out.writeInt(results.length);
        out.writeTypedArray(results, flags);

        if(cast == null)
            cast = new MovieLight[0];
        out.writeInt(cast.length);
        out.writeTypedArray(cast, flags);
    }
}