package com.raul.rsd.android.popularmovies.view;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import com.github.clans.fab.FloatingActionMenu;
import com.raul.rsd.android.popularmovies.App;
import com.raul.rsd.android.popularmovies.ErrorReporter;
import com.raul.rsd.android.popularmovies.R;
import com.raul.rsd.android.popularmovies.adapters.MoviesAdapter;
import com.raul.rsd.android.popularmovies.domain.MovieLight;
import com.raul.rsd.android.popularmovies.domain.MoviesList;
import com.raul.rsd.android.popularmovies.utils.DialogsUtils;
import com.raul.rsd.android.popularmovies.utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.utils.UIUtils;
import javax.inject.Inject;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    // --------------------------- VALUES ----------------------------

    private static final String TAG = "MainActivity";
    private static final String MOVIES_KEY = "movies_parcelable";

    // ------------------------- ATTRIBUTES --------------------------

    @BindView(R.id.rv_movies) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.menuFAB) FloatingActionMenu mFAM;

    @Inject MoviesAdapter mMoviesAdapter;
    private String mActiveSort = NetworkUtils.POPULAR;   // By default to popular

    // ------------------------- CONSTRUCTOR -------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

////REVIEW        ****** ONLY FOR DEVELOPMENT ******
//        // Simple BUG report, retrieves the last error and prompts to send an email
//        ErrorReporter errorReporter = ErrorReporter.getInstance();
//        errorReporter.Init(this);
//        errorReporter.CheckErrorAndSendMail(this);
////REVIEW        ****** ONLY FOR DEVELOPMENT ******


        // Start ButterKnife and Dagger 2
        ButterKnife.bind(this);

        // If we have the data already saved, restore those
        if(savedInstanceState != null && savedInstanceState.containsKey(MOVIES_KEY))
            setupActivity((MovieLight[]) savedInstanceState.getParcelableArray(MOVIES_KEY));
        else
            setupActivity(null);
    }

    @Override
    protected void inject(App.AppComponent component) {
        component.inject(this);
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
        NetworkUtils.setImagesSizeWithDpi(getResources().getDisplayMetrics().densityDpi);

        // Configure RecyclerView
        boolean isPortrait = !getResources().getBoolean(R.bool.is_landscape);
        int columnNumber = isPortrait ? 2 : 3;
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
                MovieLight[] movies = response.body().getResults();

                if (movies != null) {
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
