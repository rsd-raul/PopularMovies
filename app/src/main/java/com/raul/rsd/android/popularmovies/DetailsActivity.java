package com.raul.rsd.android.popularmovies;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;
import com.raul.rsd.android.popularmovies.domain.Genre;
import com.raul.rsd.android.popularmovies.domain.Movie;
import com.raul.rsd.android.popularmovies.utils.DateUtils;
import com.raul.rsd.android.popularmovies.utils.DialogsUtils;
import com.raul.rsd.android.popularmovies.utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.utils.TMDBUtils;
import com.raul.rsd.android.popularmovies.utils.UIUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity{

    // --------------------------- VALUES ----------------------------

    private static final String TAG = "DetailsActivity";

    // ------------------------- ATTRIBUTES --------------------------

    private Movie mMovie;
    @BindView(R.id.tv_title) TextView titleMain;
    @BindView(R.id.tv_rate_main_tmdb) TextView rateMain;
    @BindView(R.id.tv_rate_secondary_tmdb) TextView rateSecondary;
    @BindView(R.id.tv_description_main)  TextView descriptionMain;
    @BindView(R.id.tv_duration_main)  TextView durationMain;
    @BindView(R.id.tv_duration_secondary) TextView durationSecondary;
    @BindView(R.id.tv_release_date_main) TextView releaseDateMain;
    @BindView(R.id.tv_genres) TextView genresMain;
    @BindView(R.id.iv_movie_poster) ImageView mPosterImageView;
    @BindView(R.id.iv_movie_backdrop) ImageView mBackdropImageView;
    @BindView(R.id.poster_space) Space mPosterSpace;

    // ------------------------- CONSTRUCTOR -------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        setupActivity();
    }

    private void setupActivity(){
        // Notify the user if there is no internet, offer to retry or to close the app
        if(!NetworkUtils.isNetworkAvailable(this)){
            DialogsUtils.showErrorDialog(this, (dialog, which) -> setupActivity());
            return;
        }

        // Set ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve the ID sent and fetch the movie from TMDB
        loadData();
    }

    private void loadData(){
        Long id = getIntent().getLongExtra(Intent.EXTRA_UID, -1);
        NetworkUtils.getMovieById(id, new retrofit2.Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                mMovie = response.body();

                if(mMovie != null)
                    displayMovie();
                else
                    showErrorMessage();
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Log.e(TAG, "loadData/onFailure: Failed to fetch movie from Server", t);
                showErrorMessage();
            }
        });
    }

    private void showErrorMessage(){
        DialogsUtils.showErrorDialog(this, (dialog, which) -> loadData());
    }

    @SuppressWarnings("all")
    private void displayMovie(){
        // Setup backdrop <- First, so Picasso gets a head start.
        Uri backdropUri = NetworkUtils.buildMovieBackdropURI(mMovie.getBackdrop_path());
        Picasso.with(this)
                    .load(backdropUri)
                    .placeholder(R.drawable.placeholder_backdrop)
                    .into(mBackdropImageView, adaptColorByBackdropCallback(this, titleMain));

        // Setup poster <- Second, so Picasso gets a head start.
        Uri posterUri = NetworkUtils.buildMoviePosterURI(mMovie.getPoster_path());
        Picasso.with(this)
                    .load(posterUri)
                    .placeholder(R.drawable.placeholder_poster)
                    .into(mPosterImageView);

        // Customize the Appbar behaviour and react to scroll
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override                           // Don't use Lambda -> It's a trap!!
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                actionBarScrollControl(verticalOffset);
            }
        });

        // Fill interface with formated movie details
        titleMain.setText(mMovie.getTitle());
        durationMain.setText(DateUtils.getDurationFromMinutes(mMovie.getDuration(), this));
        durationSecondary.setText(String.format("%d %s", mMovie.getDuration(), getString(R.string.time_minutes)));
        rateMain.setText(String.format("%.1f - %s", mMovie.getVote_avg(), getString(R.string.tmdb)));
        rateSecondary.setText(String.format("%d %s", mMovie.getVote_count(), getString(R.string.votes)));
        descriptionMain.setText(mMovie.getSynopsis());
        releaseDateMain.setText(DateUtils.getStringFromDate(mMovie.getRelease_date()));
        Genre[] movieGenres = mMovie.getGenres();
        for(int i = 0; i < movieGenres.length; i++){
            if(i > 0)
                genresMain.append(" - ");
            genresMain.append(movieGenres[i].getTitle());
        }
    }

    // -------------------------- INTERFACE --------------------------

    private void actionBarScrollControl(int verticalOffset){
        int visibility = mPosterImageView.getVisibility();
        float fromXY = 0f, toXY = 0f;
        boolean react = false;

        // Estimate when to hide the movie poster so it doesn't collide with the ActionBar
        if(verticalOffset > -150 && visibility != View.VISIBLE) {
            visibility = View.VISIBLE;
            toXY = 1f;
            react = true;
        }if(verticalOffset <= -150 && visibility != View.INVISIBLE){
            visibility = View.INVISIBLE;
            fromXY = 1f;
            react = true;
        }

        // If the image state is the desired already -> Do nothing
        if(!react)
            return;

        // Hide or show the Poster
        mPosterImageView.setVisibility(visibility);

        // Animate that change
        ScaleAnimation expandAnimation = new ScaleAnimation(fromXY, toXY, fromXY, toXY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        expandAnimation.setDuration(100);
        expandAnimation.setInterpolator(new AccelerateInterpolator());
        mPosterImageView.startAnimation(expandAnimation);

        // Modify the title and genre margin based on the poster
        if(visibility == View.INVISIBLE)
            visibility = View.GONE;
        mPosterSpace.setVisibility(visibility);
    }

    private Callback adaptColorByBackdropCallback (AppCompatActivity activity, TextView titleMain){
        return new Callback() {
            @Override
            public void onSuccess() {
                // Get the image we just loaded and obtain his dominant color
                Bitmap backdrop = ((BitmapDrawable)mBackdropImageView.getDrawable()).getBitmap();
                int dominantColor = UIUtils.getDominantColor(backdrop, activity);

                // Adapt the interface to that color
                findViewById(R.id.rl_title_genre).setBackgroundColor(dominantColor);

                if(UIUtils.isColorDark(dominantColor))
                    titleMain.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryTextLight));

                // Get the subsection of the backdrop under the back arrow and its dominant color
                Bitmap backButtonSection = UIUtils.getPreciseBackBackground(backdrop, 16, 24);
                dominantColor = UIUtils.getDominantColor(backButtonSection, activity);

                // Check the actionbar presence
                ActionBar actionBar = getSupportActionBar();
                if(actionBar == null)
                    return;

                // If the dominant color is light turn the arrow Black
                if(!UIUtils.isColorDark(dominantColor))
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_back_black_24dp);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }

            @Override
            public void onError() { }
        };
    }

    // -------------------------- USE CASES --------------------------

    @OnClick(R.id.fab)
    void shareMovie(){
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

    // --------------------------- DETAILS ---------------------------

    // Overriding "back" not to reload the Main Activity as setting parentActivityName would
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
