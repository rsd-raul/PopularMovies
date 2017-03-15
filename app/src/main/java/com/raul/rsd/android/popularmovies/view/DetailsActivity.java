package com.raul.rsd.android.popularmovies.view;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Toast;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.raul.rsd.android.popularmovies.App;
import com.raul.rsd.android.popularmovies.R;
import com.raul.rsd.android.popularmovies.adapters.ReviewItem;
import com.raul.rsd.android.popularmovies.adapters.VideoItem;
import com.raul.rsd.android.popularmovies.data.InsertMovieTask;
import com.raul.rsd.android.popularmovies.data.MoviesAsyncHandler;
import com.raul.rsd.android.popularmovies.data.MoviesContract;
import com.raul.rsd.android.popularmovies.domain.Movie;
import com.raul.rsd.android.popularmovies.domain.Review;
import com.raul.rsd.android.popularmovies.domain.Video;
import com.raul.rsd.android.popularmovies.utils.DateUtils;
import com.raul.rsd.android.popularmovies.utils.DialogsUtils;
import com.raul.rsd.android.popularmovies.utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.utils.TMDBUtils;
import com.raul.rsd.android.popularmovies.utils.UIUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import javax.inject.Inject;
import javax.inject.Provider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;
import static com.raul.rsd.android.popularmovies.data.MoviesContract.*;

public class DetailsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    // --------------------------- VALUES ----------------------------

    private static final String TAG = "DetailsActivity";

    // ------------------------- ATTRIBUTES --------------------------

    @BindView(R.id.tv_title) TextView titleMain;
    @BindView(R.id.tv_rate_main_tmdb) TextView rateMain;
    @BindView(R.id.tv_rate_secondary_tmdb) TextView rateSecondary;
    @BindView(R.id.tv_description_main)  TextView descriptionMain;
    @BindView(R.id.tv_duration_main)  TextView durationMain;
    @BindView(R.id.tv_release_date_main) TextView releaseDateMain;
    @BindView(R.id.tv_genres) TextView genresMain;
    @BindView(R.id.iv_movie_poster) ImageView mPosterImageView;
    @Nullable @BindView(R.id.iv_movie_backdrop) ImageView mBackdropImageView;
    @BindView(R.id.poster_space) Space mPosterSpace;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.fab) FloatingActionButton mFloatingActionButton;
    @BindView(R.id.rv_trailers) RecyclerView mTrailersRV;
    @BindView(R.id.rv_reviews) RecyclerView mReviewsRV;
    @Inject MoviesAsyncHandler.MoviesAsyncQueryHandler moviesHandler;
    @Inject Provider<InsertMovieTask> insertMovieTaskProvider;
    @Inject Provider<FastItemAdapter<IItem>> fastAdapterProvider;
    @Inject Provider<VideoItem> videoItemProvider;
    @Inject Provider<ReviewItem> reviewItemProvider;
    private Movie mMovie;
    private boolean isFavourite;

    // ------------------------- CONSTRUCTOR -------------------------

    @Override
    protected void inject(App.AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        mSwipeRefreshLayout.setEnabled(false);

        setupActivity();

////REVIEW        ****** ONLY FOR DEVELOPMENT ******
//        // Simple BUG report, retrieves the last error and prompts to send an email
//        ErrorReporter errorReporter = ErrorReporter.getInstance();
//        errorReporter.Init(this);
//        errorReporter.CheckErrorAndSendMail(this);
////REVIEW        ****** ONLY FOR DEVELOPMENT ******
    }



    private void setupActivity(){
        // Set ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFloatingActionButton.setOnClickListener(view -> favouriteMovie());

        // Retrieve the ID sent and fetch the movie from TMDB
        mMovie = new Movie(getIntent().getLongExtra(Intent.EXTRA_UID, -1));
        new LoadMovieTask().execute();
    }



    private void startProviderRequest(){
        Log.e(TAG, "startProviderRequest: ");
        changeFavourite(true);

        LoaderManager loaderManager = getSupportLoaderManager();
        if(loaderManager.getLoader(ID_MOVIE_DETAILS_LOADER) == null)
            loaderManager.initLoader(ID_MOVIE_DETAILS_LOADER, null, this) ;
        else
            loaderManager.restartLoader(ID_MOVIE_DETAILS_LOADER, null, this);
    }

    private void startNetworkRequest(){
        Log.e(TAG, "startNetworkRequest: ");

        // Notify the user if there is no internet, offer to retry or to close the app
        if(!NetworkUtils.isNetworkAvailable(DetailsActivity.this))
            DialogsUtils.showErrorDialog(DetailsActivity.this, (dialog, which) -> startNetworkRequest());


        mSwipeRefreshLayout.setRefreshing(true);

        changeFavourite(false);

        NetworkUtils.getFullMovieById(mMovie.getId(), new retrofit2.Callback<Movie>() {
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
        DialogsUtils.showErrorDialog(this, (dialog, which) -> new LoadMovieTask().execute());
    }

    private void displayMovie(boolean isOffline){
        boolean isPortrait = mBackdropImageView != null;

        if(isOffline){
            if(isPortrait) {
                // Load backdrop and customize toolbar
                mBackdropImageView.setImageBitmap(mMovie.getBackdrop());
                adaptColorByBackdropCallback(this).onSuccess();
            }
            mPosterImageView.setImageBitmap(mMovie.getPoster());
        }else {
            if(isPortrait) {
                // Setup backdrop <- First, so Picasso gets a head start.
                if(mMovie.getBackdrop_path() != null)
                    Picasso.with(this)
                            .load(NetworkUtils.buildMovieBackdropUri(mMovie.getBackdrop_path()))
                            .placeholder(R.drawable.placeholder_backdrop)
                            .into(mBackdropImageView, adaptColorByBackdropCallback(this));
            } else
                mSwipeRefreshLayout.setRefreshing(false);

            // Setup poster
            if(mMovie.getPoster_path() != null)
                Picasso.with(this)
                        .load(NetworkUtils.buildMoviePosterUri(mMovie.getPoster_path()))
                        .placeholder(R.drawable.placeholder_poster)
                        .into(mPosterImageView);

            setupTrailers(mMovie.getVideos());

            setupReviews(mMovie.getReviews());
        }

        if (isPortrait)
            // Customize the Appbar behaviour and react to scroll
            ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener(
                    (appBarLayout1, verticalOffset) -> actionBarScrollControl(verticalOffset));
        else
            adaptInterfaceWithBackdropColor(mMovie.getDominantBackdropColor());


        // Fill interface with formatted movie details
        titleMain.setText(mMovie.getTitle());
        genresMain.setText(TMDBUtils.getStringFromGenres(mMovie.getGenres()));
        durationMain.setText(UIUtils.getCustomDurationString(this, mMovie.getDuration()));
        rateMain.setText(String.valueOf(mMovie.getVote_avg()));
        rateSecondary.setText(String.valueOf(mMovie.getVote_count()));
        descriptionMain.setText(mMovie.getSynopsis());
        releaseDateMain.setText(DateUtils.getStringFromDate(mMovie.getRelease_date()));
    }

    private void setupTrailers(Video[] videos){
        int visibility = View.VISIBLE;
        if(videos == null || videos.length == 0)
            visibility = View.GONE;

        mTrailersRV.setVisibility(visibility);
        findViewById(R.id.tv_trailers_header).setVisibility(visibility);
        if(visibility == View.GONE)
            return;

        FastItemAdapter<IItem> fAdapter = fastAdapterProvider.get();
        mTrailersRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mTrailersRV.setAdapter(fAdapter);
        for(Video video : videos)
            fAdapter.add(videoItemProvider.get().withVideo(video.getName(), video.getKey()));

        fAdapter.withOnClickListener((v, adapter, item, position) -> {
            VideoItem vi = (VideoItem) item;
            Uri videoUri = NetworkUtils.buildYoutubeTrailerUri(vi.key);
            startActivity(new Intent(Intent.ACTION_VIEW, videoUri));
            return true;
        });
    }

    private void setupReviews(Review[] reviews){
//        int visibility = View.VISIBLE;
//        if(reviews == null || reviews.length == 0)
//            visibility = View.GONE;
//
//        mReviewsRV.setVisibility(visibility);
//        findViewById(R.id.tv_reviews_header).setVisibility(visibility);
//        if(visibility == View.GONE)
//            return;

        FastItemAdapter<IItem> fAdapter = fastAdapterProvider.get();
        mReviewsRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mReviewsRV.setAdapter(fAdapter);

        if(reviews == null || reviews.length == 0) {
            fAdapter.add(reviewItemProvider.get().withReview("", getString(R.string.no_reviews)));
            return;
        }

        for(Review review : reviews)
            fAdapter.add(reviewItemProvider.get().withReview(review.getAuthor(), review.getContent()));
        fAdapter.withOnClickListener((v, adapter, item, position) -> {
            ReviewItem ri = (ReviewItem) item;
            DialogsUtils.showReviewDialog(this, ri.author, ri.content);
            return true;
        });
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

    private Callback adaptColorByBackdropCallback (AppCompatActivity activity){
        return new Callback() {
            @Override
            public void onSuccess() {
                if(mBackdropImageView == null)
                    return;

                // Get the image we just loaded and obtain his dominant color
                Bitmap backdrop = ((BitmapDrawable)mBackdropImageView.getDrawable()).getBitmap();
                mMovie.setDominantBackdropColor(UIUtils.getDominantColor(backdrop, activity));
                adaptInterfaceWithBackdropColor(mMovie.getDominantBackdropColor());

                // Get the subsection of the backdrop under the back arrow and its dominant color
                Bitmap backButtonSection = UIUtils.getDpBasedBitmap(backdrop, 16, 16, 24, 24);
                int dominantColor = UIUtils.getDominantColor(backButtonSection, activity);

                // Check the actionbar presence
                ActionBar actionBar = getSupportActionBar();
                if(actionBar == null)
                    return;

                // If the dominant color is light turn the arrow and the share black
                if(!UIUtils.isColorDark(dominantColor)) {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_back_black_24dp);
                }
                actionBar.setDisplayHomeAsUpEnabled(true);
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };
    }

    private void adaptInterfaceWithBackdropColor(int dominantColor){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        Log.e(TAG, "adaptInterfaceWithBackdropColor: " + dominantColor);
        if(dominantColor == 0)
            return;

        // Adapt the interface to that color
        findViewById(R.id.rl_title_genre).setBackgroundColor(dominantColor);

        boolean darkColor = UIUtils.isColorDark(dominantColor);
        if(darkColor)
            titleMain.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryTextLight));

        //If landscape continue
        if(mBackdropImageView != null)
            return;

        UIUtils.adaptAppBarAndStatusBarColors(this, dominantColor);

        // If the dominant color is light turn make sure everything is visible
        if(actionBar != null && !darkColor){
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_black_24dp);
            titleMain.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryText));
        }
    }

    // -------------------------- USE CASES --------------------------

    private void startSilentNetworkRequest(long id){
        NetworkUtils.getFullMovieById(id, new retrofit2.Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                Movie movie = response.body();
                if (movie == null)
                    return;

                Log.i(TAG, "startSilentNetworkRequest: response obtained");

                // Update the UI
                rateMain.setText(String.valueOf(movie.getVote_avg()));
                rateSecondary.setText(String.valueOf(movie.getVote_count()));

                setupTrailers(movie.getVideos());
                setupReviews(movie.getReviews());

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
            public void onFailure(Call<Movie> call, Throwable t) {
                Log.e(TAG, "startSilentNetworkRequest: onFailure: ", t);
            }
        });
    }


    void favouriteMovie() {
        Log.e(TAG, "favouriteMovie: " + status);

        if(!isSafe())
            return;
        status = BUSY;

        if (isFavourite) {
            Uri movieUriWithId = MoviesContract.getMovieUriWithId(mMovie.getId());

            status = SAFE;
            moviesHandler.startDelete(MoviesAsyncHandler.DELETE_TOKEN, null, movieUriWithId,
                                                                                    null, null);
        } else {
            // Save the poster and if the layout is portrait, save the backdrop too.
            mMovie.setPoster(((BitmapDrawable) mPosterImageView.getDrawable()).getBitmap());
            if(mBackdropImageView != null)
                mMovie.setBackdrop(((BitmapDrawable) mBackdropImageView.getDrawable()).getBitmap());

            status = SAFE;
            insertMovieTaskProvider.get().execute(mMovie);
        }
        changeFavourite(!isFavourite);
    }

    private void changeFavourite(boolean value){
        isFavourite = value;
        int imageRes = value ? R.drawable.ic_favorite_filled_24dp : R.drawable.ic_favorite_border_24dp;
        mFloatingActionButton.setImageResource(imageRes);
    }

    @OnClick(R.id.share_movie)
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

    // -------------------------- AUXILIARY --------------------------

    final int SAFE = 1, BUSY = -1;
    private int status = SAFE;
    private Toast mToast;

    private boolean isSafe(){
        if(status == SAFE)
            return true;
        if(mToast == null)
            mToast = Toast.makeText(this, "Saving Movie offline", Toast.LENGTH_SHORT);
        else
            mToast.show();
        return false;
    }

    // --------------------------- DETAILS ---------------------------

    // Overriding "back" not to reload the Main Activity as setting parentActivityName would
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(isSafe())
                    finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(isSafe())
            super.onBackPressed();
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
                            MoviesEntry.COLUMN_OVERVIEW,
                            MoviesEntry.COLUMN_DOMINANT};

    public static final int INDEX_ID = 0,
                            INDEX_TITLE = 1,
                            INDEX_POSTER = 2,
                            INDEX_BACKDROP = 3,
                            INDEX_GENRES = 4,
                            INDEX_RELEASE_DATE = 5,
                            INDEX_VOTE_AVERAGE = 6,
                            INDEX_VOTE_COUNT = 7,
                            INDEX_RUNTIME = 8,
                            INDEX_OVERVIEW = 9,
                            INDEX_DOMINANT = 10;

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        if(loaderId != ID_MOVIE_DETAILS_LOADER)
            throw new RuntimeException("Loader Not Implemented: " + loaderId);

        long id = getIntent().getLongExtra(Intent.EXTRA_UID, -1);
        Uri movieWithIdUri = MoviesContract.getMovieUriWithId(id);

        return new CursorLoader(this, movieWithIdUri, MOVIE_DETAILS_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // REVIEW after startDelete it comes here and fails.... Why?
        if(data == null || data.getCount() == 0) {
            Log.e(TAG, "onLoadFinished: Problems retrieving favourite from DB");
            return;
        }

        data.moveToFirst();
        mMovie = TMDBUtils.extractMovieFromCursor(data);
        data.close();

        // Adapt the interface
        displayMovie(true);

        // Update DB and UI silently
        startSilentNetworkRequest(mMovie.getId());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovie = null;
    }

    // ------------------------- ASYNC TASK --------------------------

    private class LoadMovieTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... nothingToSeeHere) {

            Uri movieUriWithId = MoviesContract.getMovieUriWithId(mMovie.getId());
            String[] projectionColumns = {MoviesEntry._ID};

            Cursor cursor = DetailsActivity.this.getContentResolver().query(
                    movieUriWithId,
                    projectionColumns,      // Return the ID only
                    null, null, null);      // #NoFilter

            boolean isLocal = cursor != null && cursor.getCount() > 0;
            if(cursor != null)
                cursor.close();
            return isLocal;
        }

        @Override
        protected void onPostExecute(Boolean isLocal) {
            if (isLocal)
                startProviderRequest();
            else{
                startNetworkRequest();
        }

            super.onPostExecute(isLocal);
        }
    }
}