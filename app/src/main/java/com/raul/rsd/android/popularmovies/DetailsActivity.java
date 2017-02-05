package com.raul.rsd.android.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import com.raul.rsd.android.popularmovies.Utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.Utils.TMDBUtils;

import java.net.URL;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "DetailsActivity";
    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Set ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configure FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> shareMovie());

        // Retrieve the ID sent and fetch the movie from TMDB
        Long movieId = getIntent().getLongExtra(Intent.EXTRA_UID, -1);
        new FetchMovieTask().execute(movieId);
    }

    private void displayMovie(){
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        // FIXME Title to big and it we have Ellipsis <- Bad
        collapsingToolbar.setTitle(mMovie.getTitle());
    }

    private void shareMovie(){
        // If the movie has not been fetched yet, don't try to share
        if(mMovie == null)
            return;

        // Create the Intent and put the info to share in a custom dialog, not using defaults
        Intent shareIntent = ShareCompat.IntentBuilder
                .from(this)
                .setType("text/plain")
                .setText(TMDBUtils.toStringMovie(mMovie, this))
                .setChooserTitle(R.string.share_movie_chooser)
                .createChooserIntent();

        // Avoid ActivityNotFoundException
        if(shareIntent.resolveActivity(getPackageManager()) != null)
            startActivity(shareIntent);
    }

    // ------------------------- ASYNC TASK --------------------------

    public class FetchMovieTask extends AsyncTask<Long, Void, Movie> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Movie doInBackground(Long... params) {
            if (params == null || params.length == 0)
                return null;

            Long id = params[0];
            URL movieRequestUrl = NetworkUtils.buildMovieURL(id);

            try {
                String jsonResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                return TMDBUtils.extractSingleMovieFromJson(jsonResponse);
            } catch (Exception ex) {
                Log.e(TAG, "doInBackground: Exception parsing JSON", ex);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Movie movie) {
            if (movie == null)
                showErrorMessage();
            else{
                mMovie = movie;
                displayMovie();
            }
        }

        private void showErrorMessage(){
        }
    }
}
