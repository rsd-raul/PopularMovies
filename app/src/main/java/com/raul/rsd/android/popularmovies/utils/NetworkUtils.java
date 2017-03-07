/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.raul.rsd.android.popularmovies.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import com.raul.rsd.android.popularmovies.BuildConfig;
import com.raul.rsd.android.popularmovies.domain.Movie;
import com.raul.rsd.android.popularmovies.domain.MoviesList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * These utilities will be used to communicate with the weather servers.
 */
public final class NetworkUtils {

    // --------------------------- VALUES ----------------------------

    public static final String POPULAR = "popular";
    public static final String TOP_RATED = "top_rated";

    private static final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/";
    private static final String BASE_MOVIE_URL = "https://api.themoviedb.org/3/movie/";

    private static final String API_PARAM = "api_key";

    private static String POSTER_SIZE;
    private static String BACKDROP_SIZE;

    // ------------------------ URI - IMAGES -------------------------

    public static Uri buildMovieBackdropURI(String backdropPath){
        return buildMovieImageURI(backdropPath, true);
    }

    public static Uri buildMoviePosterURI(String posterPath){
        return buildMovieImageURI(posterPath, false);
    }

    /**
     * Build all Image related Uris for TMDB based device DPI.
     *
     * @param imagePath Unique path for the desired image
     * @param backdrop true if it's a backdrop, false if poster
     * @return Customised Uri pointing to the desired image
     */
    private static Uri buildMovieImageURI(String imagePath, boolean backdrop){
        // Select size based on the image <- POSTER by default as it's much more frequent
        String size = POSTER_SIZE;
        if(backdrop)
            size = BACKDROP_SIZE;

        return Uri.parse(BASE_IMAGE_URL).buildUpon()
                .appendPath(size)
                .appendPath(imagePath.substring(1))
                .build();
    }

    /**
     * Set the approximate size of both poster and backdrop to match the device DPI
     *
     * @param deviceDPI Current device DPI
     */
    // "w92", "w154", "w185", "w342", "w500", "w780", "w1000", "w1920" or "original"
    public static void setImagesSizeWithDpi(int deviceDPI){
        switch (deviceDPI){
            case DisplayMetrics.DENSITY_XHIGH:
                POSTER_SIZE = "w342";
                BACKDROP_SIZE = "w780";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                POSTER_SIZE = "w500";
                BACKDROP_SIZE = "w1000";
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                POSTER_SIZE = "w780";
                BACKDROP_SIZE = "w1920";
                break;
            default:
                POSTER_SIZE = "w185";
                BACKDROP_SIZE = "w500";
                break;
        }
    }

    // -------------------------- RETROFIT 2 -------------------------

    interface TMDBService {

        //  https://api.themoviedb.org/3/movie/popular?api_key=abc
        @GET("{filter}")
        Call<MoviesList> getMoviesByFilter(@Path("filter") String filter, @Query(API_PARAM) String api_key);

        //  https://api.themoviedb.org/3/movie/328111?api_key=abc
        @GET("{id}")
        Call<Movie> getMovieById(@Path("id") Long id, @Query(API_PARAM) String api_key);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_MOVIE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // --------------------------- NETWORK ---------------------------

    public static void getMoviesByFilter(String filter, Callback<MoviesList> callback){
        NetworkUtils.TMDBService gitHubService =  NetworkUtils.TMDBService.retrofit.create(NetworkUtils.TMDBService.class);
        Call<MoviesList> call = gitHubService.getMoviesByFilter(filter, BuildConfig.TMDB_API_KEY_V3);
        call.enqueue(callback);
    }

    public static void getMovieById(Long id, Callback<Movie> callback){
        NetworkUtils.TMDBService gitHubService =  NetworkUtils.TMDBService.retrofit.create(NetworkUtils.TMDBService.class);
        Call<Movie> call = gitHubService.getMovieById(id, BuildConfig.TMDB_API_KEY_V3);
        call.enqueue(callback);
    }

    /**
     * Check whether the device is connected to the internet or not
     *
     * @param activity The activity you wish to check from
     * @return true if the device is connected, false otherwise
     */
    public static boolean isNetworkAvailable(AppCompatActivity activity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}