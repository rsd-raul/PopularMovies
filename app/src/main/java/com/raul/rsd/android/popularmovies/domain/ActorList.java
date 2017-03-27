package com.raul.rsd.android.popularmovies.domain;

import android.os.Parcel;
import android.os.Parcelable;

public class ActorList implements Parcelable {

    // ------------------------- ATTRIBUTES --------------------------

    private Actor[] results;
    private Actor[] cast;

    // ---------------------- GETTERS & SETTERS ----------------------

    public Actor[] getResults() {
        return results;
    }
    Actor[] getCast() {
        return cast;
    }

    // ------------------------- PARCELABLE --------------------------

    static final Parcelable.Creator<ActorList> CREATOR = new Parcelable.Creator<ActorList>() {
        @Override
        public ActorList createFromParcel(Parcel in) {
            return new ActorList(in);
        }

        @Override
        public ActorList[] newArray(int size) {
            return new ActorList[size];
        }
    };

    private ActorList(Parcel in) {
        results = new Actor[in.readInt()];
        in.readTypedArray(results, Actor.CREATOR);

        cast = new Actor[in.readInt()];
        in.readTypedArray(cast, Actor.CREATOR);
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        if(results == null)
            results = new Actor[0];
        out.writeInt(results.length);
        out.writeTypedArray(results, flags);

        if(cast == null)
            cast = new Actor[0];
        out.writeInt(cast.length);
        out.writeTypedArray(cast, flags);
    }
}
