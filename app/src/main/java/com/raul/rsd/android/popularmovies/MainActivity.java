package com.raul.rsd.android.popularmovies;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.raul.rsd.android.popularmovies.Adapters.MoviesAdapter;
import com.raul.rsd.android.popularmovies.Utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.Utils.TMDBUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    private static final String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    private SwipeRefreshLayout mSwipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mMoviesAdapter = new MoviesAdapter(this);

        // Configure RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mMoviesAdapter);
        mRecyclerView.setHasFixedSize(true);

        // Configure Swipe Refresh
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        // Load data in the RecyclerView
        loadData();
    }

    private void loadData(){
        new FetchMoviesTask().execute(NetworkUtils.POPULAR);
    }

    @Override
    public void onClick(long selectedMovieId) {
        // Start intent

        // Send id
        Toast.makeText(this, "Movie selected with id: " + selectedMovieId, Toast.LENGTH_SHORT).show();
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
            URL movieRequestUrl = NetworkUtils.buildSortMoviesURL(sortMode);

            try {
                String jsonResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                return TMDBUtils.extractMoviesFromJson(MainActivity.this, jsonResponse);
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
