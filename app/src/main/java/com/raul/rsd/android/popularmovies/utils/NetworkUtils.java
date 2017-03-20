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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import com.raul.rsd.android.popularmovies.BuildConfig;
import com.raul.rsd.android.popularmovies.domain.Actor;
import com.raul.rsd.android.popularmovies.domain.ActorList;
import com.raul.rsd.android.popularmovies.domain.Movie;
import com.raul.rsd.android.popularmovies.domain.MoviesList;
import java.net.URL;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public abstract class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    // --------------------------- VALUES ----------------------------

    public static final String POPULAR = "popular";
    public static final String TOP_RATED = "top_rated";
    public static final String FAVOURITES = "favourites";
    private static final String VIDEOS = "videos";
    private static final String REVIEWS = "reviews";
    private static final String MOVIE_CREDITS = "movie_credits";
    private static final String IMAGES = "images";

    private static final String BASE_TMDB_URL = "https://api.themoviedb.org/3/";
    private static final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/";
    private static final String BASE_YOUTUBE_URL = "https://www.youtube.com/watch";
    private static final String BASE_THUMBNAIL_URL = "https://img.youtube.com/vi/";

    private static final String API_PARAM = "api_key";
    private static final String VIDEO_PARAM = "v";
    private static final String APPEND_PARAM = "append_to_response";
    private static final String QUERY_PARAM = "query";
    private static final String PAGE_PARAM = "page";

    private static String POSTER_SIZE;
    private static String BACKDROP_SIZE;
    private static String THUMBNAIL_SIZE;


    // ------------------------ URI BUILDERS -------------------------

    // https://www.youtube.com/watch?v=Zk3yLI0q794
    public static Uri buildYoutubeTrailerUri(String videoPath){
        return Uri.parse(BASE_YOUTUBE_URL).buildUpon()
                .appendQueryParameter(VIDEO_PARAM, videoPath)
                .build();
    }

    // https://img.youtube.com/vi/Zk3yLI0q794/default.jpg
    public static Uri buildYoutubeThumbnailUri(String videoPath){
        return Uri.parse(BASE_THUMBNAIL_URL).buildUpon()
                .appendPath(videoPath)
                .appendPath(THUMBNAIL_SIZE)
                .build();
    }

    // https://image.tmdb.org/t/p/w500/fW37Gbk5PJZuXvyZwtcr0cMwPKY.jpg
    public static Uri buildActorProfileUri(String profilePath){
        return Uri.parse(BASE_IMAGE_URL).buildUpon()
                .appendPath(POSTER_SIZE)
                .appendPath(profilePath.substring(1))
                .build();
    }

    public static Uri buildMovieBackdropUri(String backdropPath){
        return buildMovieImageURI(backdropPath, true);
    }

    public static Uri buildMoviePosterUri(String posterPath){
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

    public static Bitmap getBackdropFromUri(String backdropPath){
        Uri uri = NetworkUtils.buildMovieBackdropUri(backdropPath);
        try {
            URL url = new URL(uri.toString());
            return BitmapFactory.decodeStream(url.openConnection().getInputStream());
        }catch (Exception ex){
            Log.e(TAG, "getBackdropFromUri: Unable to get bitmap from uri: " + uri, ex);
            return null;
        }
    }

    /**
     * Set the approximate size of both poster and backdrop to match the device DPI
     *
     * @param deviceDPI Current device DPI
     */
    // "w92", "w154", "w185", "w342", "w500", "w780", "w1000", "w1920" or "original"
    // default.jpg 120x90, mqdefault.jpg 320x180, hqdefault.jpg 480x360
    // sddefault.jpg 640x480, maxresdefault.jpg 1920x1080 might not exist <- Avoid
    public static void setImagesSizeWithDpi(int deviceDPI){
        switch (deviceDPI){
            case DisplayMetrics.DENSITY_XHIGH:
                POSTER_SIZE = "w342";
                BACKDROP_SIZE = "w780";
                THUMBNAIL_SIZE = "mqdefault.jpg";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                POSTER_SIZE = "w500";
                BACKDROP_SIZE = "w1000";
                THUMBNAIL_SIZE = "mqdefault.jpg";
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                POSTER_SIZE = "w780";
                BACKDROP_SIZE = "w1920";
                THUMBNAIL_SIZE = "hqdefault.jpg";
                break;
            default:
                POSTER_SIZE = "w185";
                BACKDROP_SIZE = "w500";
                THUMBNAIL_SIZE = "default.jpg";
                break;
        }
    }

    // -------------------------- RETROFIT 2 -------------------------

    interface TMDBService {

        //  https://api.themoviedb.org/3/movie/popular?api_key=abc
        @GET("movie/{filter}")
        Call<MoviesList> getMoviesByFilter(@Path("filter") String filter,
                                           @Query(API_PARAM) String api_key);

        //http://api.themoviedb.org/3/movie/131634?api_key=<<api_key>>&append_to_response=videos,reviews
        @GET("movie/{id}")
        Call<Movie> getFullMovieById(@Path("id") Long id,
                                     @Query(API_PARAM) String api_key,
                                     @Query(APPEND_PARAM) String videosAndReviews);

        //https://api.themoviedb.org/3/search/movie?api_key=<<api_key>>&query=passengers&page=1
        @GET("search/movie")
        Call<MoviesList> findMovieByName(@Query(API_PARAM) String api_key,
                                         @Query(QUERY_PARAM) String query,
                                         @Query(PAGE_PARAM) int page);


        //https://api.themoviedb.org/3/search/person?api_key=<<api_key>>&query=nicolas%20cage&page=1
        @GET("search/person")
        Call<ActorList> findActorByName(@Query(API_PARAM) String api_key,
                                         @Query(QUERY_PARAM) String query,
                                         @Query(PAGE_PARAM) int page);

        //http://api.themoviedb.org/3/person/2963?api_key=<<api_key>>&append_to_response=movie_credits
        @GET("person/{id}")
        Call<Actor> getFullActorById(@Path("id") Long id,
                                     @Query(API_PARAM) String api_key,
                                     @Query(APPEND_PARAM) String movieCredits);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_TMDB_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static NetworkUtils.TMDBService getTMDBService(){
        return NetworkUtils.TMDBService.retrofit.create(NetworkUtils.TMDBService.class);
    }

    // --------------------------- NETWORK ---------------------------

    public static void findActorByName(String query, int page, Callback<ActorList> callback){
        getTMDBService().findActorByName(BuildConfig.TMDB_API_KEY_V3, query, page)
                                                                                .enqueue(callback);
    }

    public static void findMovieByName(String query, int page, Callback<MoviesList> callback){
        getTMDBService().findMovieByName(BuildConfig.TMDB_API_KEY_V3, query, page)
                                                                                .enqueue(callback);
    }

    public static void getMoviesByFilter(String filter, Callback<MoviesList> callback){
        getTMDBService().getMoviesByFilter(filter, BuildConfig.TMDB_API_KEY_V3).enqueue(callback);
    }

    public static void getFullMovieById(Long id, Callback<Movie> callback){
        getTMDBService().getFullMovieById(id, BuildConfig.TMDB_API_KEY_V3, VIDEOS+","+REVIEWS)
                                                                                .enqueue(callback);
    }

    public static void getFullActorById(Long id, Callback<Actor> callback){
        getTMDBService().getFullActorById(id, BuildConfig.TMDB_API_KEY_V3, MOVIE_CREDITS+","+IMAGES)
                                                                                .enqueue(callback);
    }

    // -------------------------- AUXILIARY --------------------------

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