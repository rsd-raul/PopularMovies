package com.raul.rsd.android.popularmovies;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.raul.rsd.android.popularmovies.Adapters.MoviesAdapter;
import com.raul.rsd.android.popularmovies.Domain.Movie;
import com.raul.rsd.android.popularmovies.Utils.DialogsUtils;
import com.raul.rsd.android.popularmovies.Utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.Utils.TMDBUtils;
import com.raul.rsd.android.popularmovies.Utils.UIUtils;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    // --------------------------- VALUES ----------------------------

    private static final String TAG = "MainActivity";

    // ------------------------- ATTRIBUTES --------------------------

    private RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private String mActiveSort = NetworkUtils.POPULAR;   // By default to popular
    private FloatingActionMenu mFAM;

    // ------------------------- CONSTRUCTOR -------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupActivity();
    }

    private void setupActivity() {
        // Notify the user if there is no internet, offer to retry or to close the app
        if(!NetworkUtils.isNetworkAvailable(this)) {
            DialogsUtils.showErrorDialog(this, (dialog, which) -> setupActivity());
            return;
        }

        // Set ActionBar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Get references and values
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mMoviesAdapter = new MoviesAdapter(this);
        NetworkUtils.setImagesSizeWithDpi(getResources().getDisplayMetrics().densityDpi);

        // Configure RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mMoviesAdapter);
        mRecyclerView.setHasFixedSize(true);

        // Configure Swipe Refresh
        mSwipeRefresh.setOnRefreshListener(this::loadData);

        // Configure FAM and FABs
        configureMenu();
        configureChildren();

        // Load data in the RecyclerView with the default sorting
        loadData();
    }

    // -------------------------- USE CASES --------------------------

    public void loadData(){
        new FetchMoviesTask().execute(mActiveSort);
        UIUtils.setSubtitle(this, mActiveSort);
    }

    @Override
    public void onClick(long selectedMovieId) {
        // Build the intent and store the ID
        Intent intentDetailsActivity = new Intent(this, DetailsActivity.class);
        intentDetailsActivity.putExtra(Intent.EXTRA_UID, selectedMovieId);
        startActivity(intentDetailsActivity);
    }

    // ------------------------- ASYNC TASK --------------------------

    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeRefresh.setRefreshing(true);
        }

        @Override
        protected Movie[] doInBackground(String... params) {
            if (params == null || params.length == 0)
                return null;

            String sortMode = params[0];
            URL moviesRequestUrl = NetworkUtils.buildSortMoviesURL(sortMode);

            try {
                String jsonResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                return TMDBUtils.extractMoviesFromJson(jsonResponse);
            } catch (Exception ex) {
                Log.e(TAG, "doInBackground: Exception parsing JSON", ex);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            mSwipeRefresh.setRefreshing(false);

            if (movies == null)
                showErrorMessage();
            else{
                mMoviesAdapter.setMoviesData(movies);
                mRecyclerView.smoothScrollToPosition(0);
            }
        }
    }

    private void showErrorMessage(){
        DialogsUtils.showFetchingDataDialog(this, (dialog, which) -> loadData());
    }

    // ------------------------ FAM and FABs -------------------------

    private void configureMenu(){
        mFAM = (FloatingActionMenu) findViewById(R.id.menuFAB);

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

    /**
     * Configure al FAM children (FAB) behaviour (onClick)
     */
    private void configureChildren(){
        FloatingActionButton popularFAB = (FloatingActionButton) findViewById(R.id.popularFAB);
        if(popularFAB != null)
            popularFAB.setOnClickListener(view -> {
                mFAM.close(false);
                mActiveSort = NetworkUtils.POPULAR;
                loadData();
            });

        FloatingActionButton topRatedFAB = (FloatingActionButton) findViewById(R.id.topRatedFAB);
        if(topRatedFAB != null)
            topRatedFAB.setOnClickListener(view -> {
                mFAM.close(false);
                mActiveSort = NetworkUtils.TOP_RATED;
                loadData();
            });
    }
}
