package com.raul.rsd.android.popularmovies.domain;

import com.raul.rsd.android.popularmovies.utils.DateUtils;

import java.util.Date;

public class Actor {

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
    public void setBiography(String biography) {
        this.biography = biography;
    }

    public Date getBirthday() {
        if(birthday == null || birthday.length() != 10)
            return null;
        return DateUtils.getDateFromTMDBSString(birthday);
    }
    public void setBirthday(Date birthday) {
        if(birthday == null)
            return;
        this.birthday = DateUtils.getTMDBStringFromDate(birthday);
    }

    public String getDeathday() {
        return deathday;
    }
    public void setDeathday(String deathday) {
        this.deathday = deathday;
    }

    public String getPlace_of_birth() {
        return place_of_birth;
    }
    public void setPlace_of_birth(String place_of_birth) {
        this.place_of_birth = place_of_birth;
    }

    public MovieLight[] getMovies() {
        if(movie_credits != null)
            return movie_credits.getCast();
        return null;
    }

    public String getProfile_path() {
        return profile_path;
    }
    public void setProfile_path(String profile_path) {
        this.profile_path = profile_path;
    }

    public String getCharacter() {
        return character;
    }
    public void setCharacter(String character) {
        this.character = character;
    }
}
