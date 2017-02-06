package com.raul.rsd.android.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import com.raul.rsd.android.popularmovies.Adapters.MoviesAdapter;
import com.raul.rsd.android.popularmovies.Utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.Utils.TMDBUtils;
import com.raul.rsd.android.popularmovies.Utils.UIUtils;
import com.squareup.leakcanary.LeakCanary;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    private static final String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    public String sActiveSort = NetworkUtils.POPULAR;   // By default to popular

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure Leak Canary to warn about memory leaks
        if (LeakCanary.isInAnalyzerProcess(this))
            return;
        LeakCanary.install(getApplication());

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
        new FloatingActionMenuConfigurator().configure(this);

        // Load data in the RecyclerView
        loadData();
    }

    public void loadData(){
        new FetchMoviesTask().execute(sActiveSort);
        UIUtils.setSubtitle(this, sActiveSort);
    }

    @Override
    public void onClick(long selectedMovieId) {
        // Build the intent and store the ID
        Intent intentDetailsActivity = new Intent(this, DetailsActivity.class);
        intentDetailsActivity.putExtra(Intent.EXTRA_UID, selectedMovieId);
        startActivity(intentDetailsActivity);
    }

//    @Override
//    public void onClick(String weatherForDay) {
//        Context context = this;
//        Class destinationClass = DetailActivity.class;
//        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
//        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);
//        startActivity(intentToStartDetailActivity);
//    }

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
                showMainRecyclerView();
                mMoviesAdapter.setMoviesData(movies);
            }
        }

        private void showMainRecyclerView(){
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        private void showErrorMessage(){
        }
    }
}
