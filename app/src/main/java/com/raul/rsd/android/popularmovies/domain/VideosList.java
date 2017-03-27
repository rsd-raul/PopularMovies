package com.raul.rsd.android.popularmovies.domain;

import android.os.Parcel;
import android.os.Parcelable;

class VideosList implements Parcelable{

    // ------------------------- ATTRIBUTES --------------------------

    private Video[] results;

    // ---------------------- GETTERS & SETTERS ----------------------

    Video[] getResults() {
        return results;
    }

    // ------------------------- PARCELABLE --------------------------

    static final Parcelable.Creator<VideosList> CREATOR = new Parcelable.Creator<VideosList>() {
        @Override
        public VideosList createFromParcel(Parcel in) {
            return new VideosList(in);
        }

        @Override
        public VideosList[] newArray(int size) {
            return new VideosList[size];
        }
    };

    private VideosList(Parcel in) {
        results = new Video[in.readInt()];
        in.readTypedArray(results, Video.CREATOR);
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(results.length);
        out.writeTypedArray(results, flags);
    }
}
