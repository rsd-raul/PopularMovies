package com.raul.rsd.android.popularmovies.Utils;

import android.content.Context;
import android.util.Log;

import com.raul.rsd.android.popularmovies.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Date;

public final class TMDBUtils {

    // --------------------------- VALUES ----------------------------

    private final static String TAG = "TMDBUtils";

    private final static String TMDB_STATUS_CODE = "status_code";

    private final static String TMDB_ID = "id";
    private final static String TMDB_POSTER = "poster_path";
    private final static String TMDB_BACKDROP = "backdrop_path";
    private final static String TMDB_RELEASE_DATE = "release_date";
    private final static String TMDB_VOTES_AVG = "vote_average";
    private final static String TMDB_SYNOPSIS = "overview";

    // -------------------------- USE CASES --------------------------

    public static Movie[] extractMoviesFromJson(String moviesJson) throws JSONException {

        // Build a JSONObject with the response, then check for errors and handle them accordingly
        JSONObject results = new JSONObject(moviesJson);

        // Check if the status is valid, if it isn't, handle the problem and exit the method
        if (results.has(TMDB_STATUS_CODE) && processStatusCode(results.getInt(TMDB_STATUS_CODE)))
            return null;

        final String TMDB_RESULTS = "results";

        // Get an array of all the movies
        JSONArray moviesArray = results.getJSONArray(TMDB_RESULTS);

        // Initialize a Movie array to that size
        Movie[] extractedMovies = new Movie[moviesArray.length()];

        // Iterate over the movies on JSON, process the data and store movies in the array
        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieJson = moviesArray.getJSONObject(i);

            Movie aux = new Movie();
            aux.setId(movieJson.getLong(TMDB_ID));
            aux.setPoster_path(movieJson.getString(TMDB_POSTER));

            extractedMovies[i] = aux;
        }

        return extractedMovies;
    }

    public static Movie extractSingleMovieFromJson(Context context, String movieJson) throws JSONException {

        // Build a JSONObject with the response, then check for errors and handle them accordingly
        JSONObject movie = new JSONObject(movieJson);

        // Check if the status is valid, if it isn't, handle the problem and exit the method
        if (movie.has(TMDB_STATUS_CODE) && processStatusCode(movie.getInt(TMDB_STATUS_CODE)))
            return null;

        // Initialize a Movie array to that size
        Movie extractedMovie = new Movie();

        extractedMovie.setId(movie.getLong(TMDB_ID));
        extractedMovie.setPoster_path(movie.getString(TMDB_POSTER));
        extractedMovie.setBackdrop_path(movie.getString(TMDB_BACKDROP));
        extractedMovie.setVote_avg(movie.getDouble(TMDB_VOTES_AVG));
        extractedMovie.setSynopsis(movie.getString(TMDB_SYNOPSIS));
        Date parsedDate = DateUtils.getDateFromString(movie.getString(TMDB_RELEASE_DATE));
        extractedMovie.setRelease_date(parsedDate);

        return extractedMovie;
    }

    // -------------------------- AUXILIARY --------------------------

    private static boolean processStatusCode(int status_code) {
        boolean problem = false;
        switch (status_code) {
            case 1:case 12:case 13:
                /*
                // SUCCESS -> Notify user
                1	200	Success.
                12	201	The item/record was updated successfully.
                13	200	The item/record was deleted successfully.
                */
                break;

            case 8:case 9:case 15:case 21:case 24:case 30:case 34:
                /*
                // WARNING -> Handle, Notify user
                8	403	Duplicate entry: The data you tried to submit already exists.
                9	503	Service offline: This service is temporarily offline, try again later.
                15	500	Failed.
                21	200	Entry not found: The item you are trying to edit cannot be found.
                24	504	Your request to the backend server timed out. Try again.
                30	401	Invalid username and/or password: You did not provide a valid login.
                34	401	The resource you requested could not be found.
                */
                problem = true;
                break;

            case 3:case 7:case 10:case 14:case 16:case 17:case 32:case 33:
                /*
                // AUTH -> Dev problem
                3	401	Authentication failed: You do not have permissions to access the service.
                7	401	Invalid API key: You must be granted a valid key.
                10	401	Suspended API key: Access to your account has been suspended, contact TMDb.
                14	401	Authentication failed.
                16	401	Device denied.
                17	401	Session denied.
                32	401	Email not verified: Your email address has not been verified.
                33	401	Invalid request token: The request token is either expired or invalid.
                */
                problem = true;
                break;

            case 2:case 4:case 5:case 6:case 18:case 19:case 20:case 22:case 23:case 25:
            case 26:case 27:case 28:case 29:
                /*
                // MALFORMED -> Check deprecated
                2	501	Invalid service: this service does not exist.
                4	405	Invalid format: This service doesn't exist in that format.
                5	422	Invalid parameters: Your request parameters are incorrect.
                6	404	Invalid id: The pre-requisite id is invalid or not found.
                18	400	Validation failed.
                19	406	Invalid accept header.
                20	422	Invalid date range: Should be a range no longer than 14 days.
                22	400	Invalid page: Pages start at 1 and max at 1000. They are expected to be an integer.
                23	400	Invalid date: Format needs to be YYYY-MM-DD.
                25	429	Your request count (#) is over the allowed limit of (40).
                26	400	You must provide a username and password.
                27	400	Too many append to response objects: The maximum number of remote calls is 20.
                28	400	Invalid timezone: Please consult the documentation for a valid timezone.
                29	400	You must confirm this action: Please provide a confirm=true parameter.
                 */
                problem = true;
                break;

            case 11:case 31:
                /*
                // TMDB -> TMDB Problem -> Check, Validate, Contact
                11	500	Internal error: Something went wrong, contact TMDb.
                31	401	Account disabled: Your account is no longer active. Contact TMDb if this is an error.
                 */
                problem = true;
                break;

            default:
                // If not controlled, log the error and try to continue
                Log.e(TAG, "processStatusCode: " + status_code + " is NOT CONTROLLED");
                break;
        }
        return problem;
    }
}