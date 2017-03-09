package com.raul.rsd.android.popularmovies.view;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;
import com.raul.rsd.android.popularmovies.App;
import com.raul.rsd.android.popularmovies.R;
import com.raul.rsd.android.popularmovies.data.MoviesAsyncHandler;
import com.raul.rsd.android.popularmovies.data.MoviesContract;
import com.raul.rsd.android.popularmovies.domain.Movie;
import com.raul.rsd.android.popularmovies.utils.BitmapUtils;
import com.raul.rsd.android.popularmovies.utils.DateUtils;
import com.raul.rsd.android.popularmovies.utils.DialogsUtils;
import com.raul.rsd.android.popularmovies.utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.utils.TMDBUtils;
import com.raul.rsd.android.popularmovies.utils.UIUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.net.URL;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;
import static com.raul.rsd.android.popularmovies.data.MoviesContract.*;

public class DetailsActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    // --------------------------- VALUES ----------------------------

    private static final String TAG = "DetailsActivity";

    // ------------------------- ATTRIBUTES --------------------------

    @BindView(R.id.tv_title) TextView titleMain;
    @BindView(R.id.tv_rate_main_tmdb) TextView rateMain;
    @BindView(R.id.tv_rate_secondary_tmdb) TextView rateSecondary;
    @BindView(R.id.tv_description_main)  TextView descriptionMain;
    @BindView(R.id.tv_duration_main)  TextView durationMain;
    @BindView(R.id.tv_duration_secondary) TextView durationSecondary;
    @BindView(R.id.tv_release_date_main) TextView releaseDateMain;
    @BindView(R.id.tv_genres) TextView genresMain;
    @BindView(R.id.iv_movie_poster) ImageView mPosterImageView;
    @Nullable @BindView(R.id.iv_movie_backdrop) ImageView mBackdropImageView;
    @BindView(R.id.poster_space) Space mPosterSpace;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.fab) FloatingActionButton mFloatingActionButton;
    @Inject MoviesAsyncHandler.MoviesAsyncQueryHandler moviesHandler;
    private Movie mMovie;
    private boolean isFavourite;

    // ------------------------- CONSTRUCTOR -------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        mSwipeRefreshLayout.setEnabled(false);

        setupActivity();
    }

    @Override
    protected void inject(App.AppComponent component) {
        component.inject(this);
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

        mFloatingActionButton.setOnClickListener(view -> favouriteMovie());

        // Retrieve the ID sent and fetch the movie from TMDB
        loadData();
    }

    private void loadData(){
        long id = getIntent().getLongExtra(Intent.EXTRA_UID, -1);

        new Thread(() -> {
            Uri movieUriWithId = MoviesContract.getMovieUriWithId(id);

            String[] projectionColumns = {MoviesEntry._ID};

            Cursor cursor = this.getContentResolver().query(
                    movieUriWithId,
                    projectionColumns,      // Return the ID only
                    null, null, null);      // #NoFilter

            if (null != cursor && cursor.getCount() > 0) {
                startProviderRequest(id);
                cursor.close();
            }else
                startNetworkRequest(id);
        }).start();
    }

    private void startProviderRequest(long id){
        Log.e(TAG, "startProviderRequest: ");
        changeFavourite(true);

        Bundle queryBundle = new Bundle();
        queryBundle.putLong(Intent.EXTRA_UID, id);

        LoaderManager loaderManager = getSupportLoaderManager();
        if(loaderManager.getLoader(ID_MOVIE_DETAILS_LOADER) == null)
            loaderManager.initLoader(ID_MOVIE_DETAILS_LOADER, queryBundle, this) ;
        else
            loaderManager.restartLoader(ID_MOVIE_DETAILS_LOADER, queryBundle, this);
    }

    private void startNetworkRequest(long id){
        Log.e(TAG, "startNetworkRequest: ");

        mSwipeRefreshLayout.setRefreshing(true);

        changeFavourite(false);

        NetworkUtils.getFullMovieById(id, new retrofit2.Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                mMovie = response.body();

                if (mMovie != null)
                    displayMovie(false);
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

    private void displayMovie(boolean offline){
        boolean isLandscape = getResources().getBoolean(R.bool.is_landscape);

        //FIXME CLEAN
        if(offline){
            // If portrait, store image
            if(mBackdropImageView != null) {
                mBackdropImageView.setImageBitmap(mMovie.getBackdrop());
                adaptColorByBackdropCallback(this, titleMain).onSuccess();
            }
            mPosterImageView.setImageBitmap(mMovie.getPoster());
        }else {
            if(mBackdropImageView != null) {
                // Setup backdrop <- First, so Picasso gets a head start.
                Uri backdropUri = NetworkUtils.buildMovieBackdropUri(mMovie.getBackdrop_path());
                Picasso.with(this)
                        .load(backdropUri)
                        .placeholder(R.drawable.placeholder_backdrop)
                        .into(mBackdropImageView, adaptColorByBackdropCallback(this, titleMain));

                // Customize the Appbar behaviour and react to scroll
                AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
                appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    @Override                           // Don't use Lambda -> It's a trap!!
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        actionBarScrollControl(verticalOffset);
                    }
                });
            }

            //FIXME CLEAN
            // If landscape, display back button and stop loading icon
            if(mBackdropImageView == null) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null)
                    actionBar.setDisplayHomeAsUpEnabled(true);
            }

            // Setup poster <- Second, so Picasso gets a head start.
            Uri posterUri = NetworkUtils.buildMoviePosterUri(mMovie.getPoster_path());
            Picasso.with(this)
                    .load(posterUri)
                    .placeholder(R.drawable.placeholder_poster)
                    .into(mPosterImageView);
        }

        // Fill interface with formatted movie details
        titleMain.setText(mMovie.getTitle());
        durationMain.setText(DateUtils.getDurationFromMinutes(mMovie.getDuration(), this));
        durationSecondary.setText(getString(R.string.format_double_string, mMovie.getDuration(), getString(R.string.time_minutes)));
        rateMain.setText(getString(R.string.format_voteAvg, mMovie.getVote_avg(), getString(R.string.tmdb)));
        rateSecondary.setText(getString(R.string.format_double_string, mMovie.getVote_count(), getString(R.string.votes)));
        descriptionMain.setText(mMovie.getSynopsis());
        releaseDateMain.setText(DateUtils.getStringFromDate(mMovie.getRelease_date()));
        genresMain.setText(TMDBUtils.getStringFromGenres(mMovie.getGenres()));
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
                Bitmap backButtonSection = UIUtils.getPreciseBackground(backdrop, 16, 24);
                dominantColor = UIUtils.getDominantColor(backButtonSection, activity);

                // Check the actionbar presence
                ActionBar actionBar = getSupportActionBar();
                if(actionBar == null)
                    return;

                // If the dominant color is light turn the arrow Black
                if(!UIUtils.isColorDark(dominantColor))
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_back_black_24dp);
                actionBar.setDisplayHomeAsUpEnabled(true);

                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };
    }

    // -------------------------- USE CASES --------------------------

    private void changeFavourite(boolean value){
        isFavourite = value;

        // Only change the image if we are under Lollipop
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return;

        int imageRes = value ? R.drawable.ic_favorite_filled_24dp : R.drawable.ic_favorite_border_24dp;
        mFloatingActionButton.setImageResource(imageRes);
    }

    void favouriteMovie() {
        if (isFavourite) {
            Uri movieUriWithId = MoviesContract.getMovieUriWithId(mMovie.getId());
            moviesHandler.startDelete(MoviesAsyncHandler.DELETE_TOKEN, null, movieUriWithId,
                                                                                    null, null);
        } else {
            new Thread(() -> {
                ContentValues movie = TMDBUtils.getContentValuesFromMovie(mMovie);

                // FIXME CLEAN
                if(mMovie.getBackdrop() == null) {
                    Bitmap backdropBitmap = null;
                    if(mBackdropImageView != null)
                        backdropBitmap = ((BitmapDrawable) mBackdropImageView.getDrawable())
                                                                                    .getBitmap();
                    else {
                        try {
                            Uri uri = NetworkUtils.buildMovieBackdropUri(mMovie.getBackdrop_path());
                            URL url = new URL(uri.toString());
                            backdropBitmap = BitmapFactory.decodeStream(
                                                            url.openConnection().getInputStream());
                        }catch (Exception ex){
                            Log.e(TAG, "favouriteMovie: ", ex);
                        }
                    }
                    if(backdropBitmap != null) {
                        byte[] backdropBytes = BitmapUtils.getBytesFromBitmap(backdropBitmap);
                        movie.put(MoviesEntry.COLUMN_BACKDROP, backdropBytes);
                    }
                }
                if (mMovie.getPoster() == null) {
                    Bitmap posterBitmap= ((BitmapDrawable) mPosterImageView.getDrawable()).getBitmap();
                    byte[] posterBytes = BitmapUtils.getBytesFromBitmap(posterBitmap);
                    movie.put(MoviesEntry.COLUMN_POSTER, posterBytes);
                }

                moviesHandler.startInsert(MoviesAsyncHandler.INSERT_TOKEN, null,
                        MoviesContract.CONTENT_URI, movie);
            }).start();
        }

        changeFavourite(!isFavourite);
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

    void launchYoutube(Uri uri){
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    // --------------------------- DETAILS ---------------------------

    // Overriding "back" not to reload the Main Activity as setting parentActivityName would
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                shareMovie();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // ---------------------------- MENU -----------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }


    // ------------------------ CURSOR LOADER ------------------------

    private static final int ID_MOVIE_DETAILS_LOADER = 128;

    public static final String[] MOVIE_DETAILS_PROJECTION = {
                            MoviesEntry._ID,
                            MoviesEntry.COLUMN_TITLE,
                            MoviesEntry.COLUMN_POSTER,
                            MoviesEntry.COLUMN_BACKDROP,
                            MoviesEntry.COLUMN_GENRES,
                            MoviesEntry.COLUMN_RELEASE_DATE,
                            MoviesEntry.COLUMN_VOTE_AVERAGE,
                            MoviesEntry.COLUMN_VOTE_COUNT,
                            MoviesEntry.COLUMN_RUNTIME,
                            MoviesEntry.COLUMN_OVERVIEW};

    public static final int INDEX_ID = 0,
                            INDEX_TITLE = 1,
                            INDEX_POSTER = 2,
                            INDEX_BACKDROP = 3,
                            INDEX_GENRES = 4,
                            INDEX_RELEASE_DATE = 5,
                            INDEX_VOTE_AVERAGE = 6,
                            INDEX_VOTE_COUNT = 7,
                            INDEX_RUNTIME = 8,
                            INDEX_OVERVIEW = 9;

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        if(loaderId != ID_MOVIE_DETAILS_LOADER)
            throw new RuntimeException("Loader Not Implemented: " + loaderId);

        long id = getIntent().getLongExtra(Intent.EXTRA_UID, -1);

        Looper.prepare();

        return new CursorLoader(this,
                MoviesContract.getMovieUriWithId(id),
                MOVIE_DETAILS_PROJECTION,
                null, null,
                MoviesEntry.COLUMN_TIMESTAMP + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null || data.getCount() == 0)
            throw new SQLException("onLoadFinished: Problems retrieving favourite from DB");

        mMovie = TMDBUtils.extractMovieFromCursor(data);

        // Adapt the interface
        displayMovie(true);

        // Update DB and UI silently
        startSilentNetworkRequest(mMovie.getId());
    }

    private void startSilentNetworkRequest(long id){
        NetworkUtils.getMovieById(id, new retrofit2.Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                Log.i(TAG, "startSilentNetworkRequest: response obtained");
                Movie movie = response.body();

                if (movie == null)
                    return;

                // Update the UI
                rateMain.setText(getString(R.string.format_voteAvg,
                        movie.getVote_avg(), getString(R.string.tmdb)));
                rateSecondary.setText(getString(R.string.format_double_string,
                        movie.getVote_count(), getString(R.string.votes)));

                // Save ONLY updated data in the DB
                ContentValues values = TMDBUtils.getContentValuesFromMovie(mMovie, movie);

                // Update database only if there is something to update
                if(values.size() > 1)
                    return;

                Log.i(TAG, "startSilentNetworkRequest: updating Movie");
                Uri uri = MoviesContract.getMovieUriWithId(mMovie.getId());
                moviesHandler.startUpdate(MoviesAsyncHandler.UPDATE_TOKEN, null, uri, values,
                        null, null);
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) { }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovie = null;
    }
}
