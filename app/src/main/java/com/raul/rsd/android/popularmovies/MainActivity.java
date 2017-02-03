package com.raul.rsd.android.popularmovies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.raul.rsd.android.popularmovies.Adapters.MoviesAdapter;
import com.raul.rsd.android.popularmovies.Utils.Constants;
import com.raul.rsd.android.popularmovies.Utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.Utils.TMDBUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    private static final String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;
    private MoviesAdapter mMoviesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        // Configure RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManager);

        mMoviesAdapter = new MoviesAdapter(this);
        mRecyclerView.setAdapter(mMoviesAdapter);

        mRecyclerView.setHasFixedSize(true);

        // Load data in the RecyclerView
        new FetchMoviesTask().execute(NetworkUtils.POPULAR);
    }

    @Override
    public void onClick(long selectedMovieId) {
        // Start intent

        // Send id
    }

    // ------------------------- ASYNC TASK --------------------------

    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
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
            mLoadingIndicator.setVisibility(View.INVISIBLE);

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
