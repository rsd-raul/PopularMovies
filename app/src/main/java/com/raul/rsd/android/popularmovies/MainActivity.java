package com.raul.rsd.android.popularmovies;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import com.github.clans.fab.FloatingActionMenu;
import com.raul.rsd.android.popularmovies.Adapters.MoviesAdapter;
import com.raul.rsd.android.popularmovies.Domain.MovieLight;
import com.raul.rsd.android.popularmovies.Domain.MoviesList;
import com.raul.rsd.android.popularmovies.Utils.DialogsUtils;
import com.raul.rsd.android.popularmovies.Utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.Utils.UIUtils;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    // --------------------------- VALUES ----------------------------

    private static final String TAG = "MainActivity";
    private static final String MOVIES_KEY = "movies_parcelable";

    // ------------------------- ATTRIBUTES --------------------------

    @BindView(R.id.rv_movies) RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefresh;
    private String mActiveSort = NetworkUtils.POPULAR;   // By default to popular
    @BindView(R.id.menuFAB) FloatingActionMenu mFAM;

    // ------------------------- CONSTRUCTOR -------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // If we have the data already saved, restore those
        if(savedInstanceState != null && savedInstanceState.containsKey(MOVIES_KEY))
            setupActivity((MovieLight[]) savedInstanceState.getParcelableArray(MOVIES_KEY));
        else
            setupActivity(null);
    }

    private void setupActivity(MovieLight[] movies) {
        // Notify the user if there is no internet, offer to retry or to close the app
        if(!NetworkUtils.isNetworkAvailable(this)) {
            DialogsUtils.showErrorDialog(this, (dialog, which) -> setupActivity(movies));
            return;
        }

        // Set ActionBar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Get references and values
        mMoviesAdapter = new MoviesAdapter(this);
        NetworkUtils.setImagesSizeWithDpi(getResources().getDisplayMetrics().densityDpi);

        // Configure RecyclerView
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        int columnNumber = isPortrait ? 2 : 3; // TODO -> Horizontal + Details = 2
        GridLayoutManager layoutManager = new GridLayoutManager(this, columnNumber);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mMoviesAdapter);
        mRecyclerView.setHasFixedSize(true);

        // Configure Swipe Refresh
        mSwipeRefresh.setOnRefreshListener(this::loadData);

        // Configure FAM
        configureMenu();

        // Load data in the RecyclerView with the default sorting
        if(movies == null)
            loadData();
        else
            mMoviesAdapter.setMoviesData(movies);
    }

    // -------------------------- USE CASES --------------------------

    public void loadData(){
        mSwipeRefresh.setRefreshing(true);

        NetworkUtils.getMoviesByFilter(mActiveSort, new Callback<MoviesList>() {
            @Override
            public void onResponse(Call<MoviesList> call, Response<MoviesList> response) {
                ArrayList<MovieLight> responseMovies = response.body().getResults();

                if (responseMovies != null) {
                    MovieLight[] movies = new MovieLight[responseMovies.size()];
                    movies = responseMovies.toArray(movies);

                    mMoviesAdapter.setMoviesData(movies);
                    mRecyclerView.smoothScrollToPosition(0);
                } else
                    showErrorMessage();

                mSwipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<MoviesList> call, Throwable t) {
                Log.e(TAG, "loadData/onFailure: Failed to fetch movies from Server", t);
                showErrorMessage();
            }
        });
        UIUtils.setSubtitle(this, mActiveSort);
    }

    private void showErrorMessage(){
        DialogsUtils.showFetchingDataDialog(this, (dialog, which) -> loadData());
    }

    @Override
    public void onClick(long selectedMovieId) {
        // Build the intent and store the ID
        Intent intentDetailsActivity = new Intent(this, DetailsActivity.class);
        intentDetailsActivity.putExtra(Intent.EXTRA_UID, selectedMovieId);
        startActivity(intentDetailsActivity);
    }

    // ------------------------ FAM and FABs -------------------------

    private void configureMenu(){

        // Change the background depending on the fabMenu Status
        mFAM.setIconAnimated(false);
        mFAM.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            int fromColor = ContextCompat.getColor(getApplicationContext(), R.color.transparent);
            int toColor = ContextCompat.getColor(getApplicationContext(), R.color.background);

            // Creation of animator to transition between the transparent color and the other one
            final ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(mFAM,
                    "backgroundColor", new ArgbEvaluator(), fromColor, toColor).setDuration(100);

            @Override
            public void onMenuToggle(boolean opened) {
                int famIcon;
                if (opened) {
                    backgroundColorAnimator.start();
                    famIcon = R.drawable.ic_close_24dp;
                }else {
                    backgroundColorAnimator.reverse();
                    famIcon = R.drawable.ic_filter_24dp;
                }
                mFAM.getMenuIconView().setImageResource(famIcon);
            }
        });

        // On click outside close the menu
        mFAM.setClosedOnTouchOutside(true);
    }

    @OnClick(R.id.topRatedFAB)
    void changeFilterToTopRated(){
        mFAM.close(false);
        mActiveSort = NetworkUtils.TOP_RATED;
        loadData();
    }

    @OnClick(R.id.popularFAB)
    void changeFilterToPopular(){
        mFAM.close(false);
        mActiveSort = NetworkUtils.POPULAR;
        loadData();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(MOVIES_KEY, mMoviesAdapter.getMoviesData());
    }

//    @Override // TODO handle restore
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//    }
}
