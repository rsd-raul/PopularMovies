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
package com.raul.rsd.android.popularmovies.Utils;

import android.net.Uri;
import android.util.Log;

import com.raul.rsd.android.popularmovies.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the weather servers.
 */
public final class NetworkUtils {

    // --------------------------- VALUES ----------------------------

    private static final String TAG = "NetworkUtils";

    public static final String POPULAR = "popular";
    public static final String TOP_RATED = "top_rated";

    private static final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/";
    private static final String BASE_MOVIE_URL = "https://api.themoviedb.org/3/movie/";

    private static final String API_PARAM = "api_key";

    // "w92", "w154", "w185", "w342", "w500", "w780", or "original"
    private static final String IMAGE_SIZE = "w500";


    // -------------------------- USE CASES --------------------------

    public static URL buildMovieURL(long id){
        Uri builtUri = Uri.parse(BASE_MOVIE_URL).buildUpon()
                .appendPath(Long.toString(id))
                .appendQueryParameter(API_PARAM, BuildConfig.TMDB_API_KEY_V3)
                .build();
        return getUrl(builtUri);
    }

    public static URL buildSortMoviesURL(String sortBy){
        Uri builtUri = Uri.parse(BASE_MOVIE_URL).buildUpon()
                .appendPath(sortBy)
                .appendQueryParameter(API_PARAM, BuildConfig.TMDB_API_KEY_V3)
                .build();
        return getUrl(builtUri);
    }

    public static Uri buildMovieImageURL(String imagePath){
        return Uri.parse(BASE_IMAGE_URL).buildUpon()
                .appendPath(IMAGE_SIZE)
                .appendPath(imagePath.substring(1))
                .build();
    }

    private static URL getUrl(Uri uri) {
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, "Malformed URL", e);
        }
        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}