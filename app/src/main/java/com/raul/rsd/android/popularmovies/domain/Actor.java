package com.raul.rsd.android.popularmovies.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.raul.rsd.android.popularmovies.utils.DateUtils;

import java.util.Date;

public class Actor implements Parcelable{

    private long id;
    private String name;
    private String biography;
    private String birthday;
    private String place_of_birth;
    private String deathday;
    private MoviesList movie_credits;
    private String profile_path;
    private String character;

    public Actor(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public Date getBirthday() {
        if(birthday == null || birthday.length() != 10)
            return null;
        return DateUtils.getDateFromTMDBSString(birthday);
    }

    public String getDeathday() {
        return deathday;
    }

    public String getPlace_of_birth() {
        return place_of_birth;
    }

    public MovieLight[] getMovies() {
        if(movie_credits != null)
            return movie_credits.getCast();
        return null;
    }

    public String getProfile_path() {
        return profile_path;
    }

    public String getCharacter() {
        return character;
    }

    // ------------------------- PARCELABLE --------------------------

    static final Creator<Actor> CREATOR = new Creator<Actor> () {
        @Override
        public Actor createFromParcel(Parcel in) {
            return new Actor(in);
        }

        @Override
        public Actor[] newArray(int size) {
            return new Actor[size];
        }
    };

    private Actor(Parcel in) {
        id = in.readLong();
        name = in.readString();
        biography = in.readString();
        birthday = in.readString();
        place_of_birth = in.readString();
        deathday = in.readString();

        movie_credits = in.readParcelable(MoviesList.class.getClassLoader());

        profile_path = in.readString();
        character = in.readString();
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(name);
        out.writeString(biography);
        out.writeString(birthday);
        out.writeString(place_of_birth);
        out.writeString(deathday);

        out.writeParcelable(movie_credits, flags);

        out.writeString(profile_path);
        out.writeString(character);
    }
}
