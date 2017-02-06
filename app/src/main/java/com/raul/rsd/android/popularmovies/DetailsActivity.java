package com.raul.rsd.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.raul.rsd.android.popularmovies.Utils.DateUtils;
import com.raul.rsd.android.popularmovies.Utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.Utils.TMDBUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;

public class DetailsActivity extends AppCompatActivity{

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
        // Get references and values
        ImageView imageView = (ImageView) findViewById(R.id.iv_movie_backdrop);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        TextView rateMain = (TextView) findViewById(R.id.tv_rate_main_tmdb);
        TextView rateSecondary = (TextView) findViewById(R.id.tv_rate_secondary_tmdb);
        TextView descriptionMain = (TextView) findViewById(R.id.tv_description_main);
        TextView durationMain = (TextView) findViewById(R.id.tv_duration_main);
        TextView durationSecondary = (TextView) findViewById(R.id.tv_duration_secondary);
        TextView releaseDateMain = (TextView) findViewById(R.id.tv_release_date_main);

        // Setup backdrop <- First, so Picasso gets a head start.
        Uri backDropUri = NetworkUtils.buildMovieBackdropURI(mMovie.getBackdrop_path());
        Picasso.with(this)
                .load(backDropUri)
                .placeholder(R.drawable.placeholder_backdrop)
                .into(imageView);

        // Customize the Toolbar with the movie title
        collapsingToolbar.setTitle(mMovie.getTitle());
        // FIXME Title to big and it we have Ellipsis <- Bad

        // Customize movie details
        rateMain.setText(String.format("%.1f - %s", mMovie.getVote_avg(), getString(R.string.tmdb)));
        rateSecondary.setText(String.format("%d %s", mMovie.getVote_count(), getString(R.string.votes)));
        descriptionMain.setText(mMovie.getSynopsis());
        durationMain.setText(DateUtils.getDurationFromMinutes(mMovie.getDuration(), this));
        durationSecondary.setText(String.format("%d %s", mMovie.getDuration(), getString(R.string.time_minutes)));
        releaseDateMain.setText(DateUtils.getStringFromDate(mMovie.getRelease_date()));

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
