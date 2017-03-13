package com.raul.rsd.android.popularmovies.view;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchFilter;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;
import com.raul.rsd.android.popularmovies.App;
import com.raul.rsd.android.popularmovies.R;
import com.raul.rsd.android.popularmovies.adapters.MoviesAdapter;
import com.raul.rsd.android.popularmovies.data.MoviesContract;
import com.raul.rsd.android.popularmovies.domain.MovieLight;
import com.raul.rsd.android.popularmovies.domain.MoviesList;
import com.raul.rsd.android.popularmovies.utils.DialogsUtils;
import com.raul.rsd.android.popularmovies.utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.utils.TMDBUtils;
import com.raul.rsd.android.popularmovies.utils.UIUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    // --------------------------- VALUES ----------------------------

    private static final String TAG = "MainActivity";
    private static final String MOVIES_KEY = "movies_parcelable";

    // ------------------------- ATTRIBUTES --------------------------

    @BindView(R.id.rv_movies) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.bottom_navigation) AHBottomNavigation mBottomNavigation;
    @BindView(R.id.searchView) SearchView mSearchView;
    @Inject MoviesAdapter mMoviesAdapter;
    private String mActiveSort = NetworkUtils.POPULAR;   // By default to popular

    // ------------------------- CONSTRUCTOR -------------------------

    @Override
    protected void inject(App.AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

////REVIEW        ****** ONLY FOR DEVELOPMENT ******
//        // Simple BUG report, retrieves the last error and prompts to send an email
//        ErrorReporter errorReporter = ErrorReporter.getInstance();
//        errorReporter.Init(this);
//        errorReporter.CheckErrorAndSendMail(this);
////REVIEW        ****** ONLY FOR DEVELOPMENT ******

        // If we have the data already saved, restore those
        if(savedInstanceState != null && savedInstanceState.containsKey(MOVIES_KEY))
            setupActivity((MovieLight[]) savedInstanceState.getParcelableArray(MOVIES_KEY));
        else
            setupActivity(null);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        configureSearchView();
    }

    private void setupActivity(MovieLight[] movies) {
        // Notify the user if there is no internet, offer to retry or to close the app
        if(!NetworkUtils.isNetworkAvailable(this)) {
            DialogsUtils.showErrorDialog(this, (dialog, which) -> setupActivity(movies));
            return;
        }

        // Set ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

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
        mSwipeRefresh.setOnRefreshListener(() -> {
            if(mActiveSort.equals(NetworkUtils.FAVOURITES))
                loadFavourites();
            else
                loadData();
        });

        // Configure Bottom Navigation Bar
        configureBottomNavigationBar();

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

    // ---------------------- BOTTOM NAVIGATION ----------------------

    private void configureBottomNavigationBar(){

        // Create and add items
        mBottomNavigation.addItem(new AHBottomNavigationItem(
                R.string.popular, R.drawable.ic_popular_24dp, R.color.colorAccent));
        mBottomNavigation.addItem(new AHBottomNavigationItem(
                R.string.top_rated, R.drawable.ic_rate_24dp, R.color.amberDark));
        mBottomNavigation.addItem(new AHBottomNavigationItem(
                R.string.favourites, R.drawable.ic_favorite_border_24dp, R.color.redDark));

        // Display color under navigation bar (API 21+)
        mBottomNavigation.setTranslucentNavigationEnabled(true);

        // Manage titles
        mBottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);

        // Use colored navigation with circle reveal effect
        mBottomNavigation.setColored(true);

        // Set current item programmatically
        mBottomNavigation.setCurrentItem(0);

        // Set listeners
        mBottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            switch (position){
                case 0:
                    mActiveSort = NetworkUtils.POPULAR;
                    loadData();
                    break;
                case 1:
                    mActiveSort = NetworkUtils.TOP_RATED;
                    loadData();
                    break;
                case 2:
                    mActiveSort = NetworkUtils.FAVOURITES;
                    loadFavourites();
                    break;
            }
            return true;
        });
    }

    public void loadFavourites(){
        mSwipeRefresh.setRefreshing(true);

        LoaderManager loaderManager = getSupportLoaderManager();
        if(loaderManager.getLoader(ID_MOVIE_FAVOURITES_LOADER) == null)
            loaderManager.initLoader(ID_MOVIE_FAVOURITES_LOADER, null, this) ;
        else
            loaderManager.restartLoader(ID_MOVIE_FAVOURITES_LOADER, null, this);

        UIUtils.setSubtitle(this, mActiveSort);
    }

    // ------------------------- SEARCH VIEW -------------------------

//    SearchHistoryTable mHistoryDatabase;  // TODO Research
//    mHistoryDatabase = new SearchHistoryTable(this);
//    mHistoryDatabase.setHistorySize(10);

    private void configureSearchView() {
        if (mSearchView == null)
            return;

        configureSearchViewBehaviour();


        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(MainActivity.this, "onQueryTextSubmit", Toast.LENGTH_SHORT).show();
//                    getData(query, 0);
//                    mSearchView.close(false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Toast.makeText(MainActivity.this, "onQueryTextChange", Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        mSearchView.setVoiceText("Set permission on Android 6.0+ !");
        mSearchView.setOnVoiceClickListener(() -> {
            Toast.makeText(MainActivity.this, "onVoiceClick", Toast.LENGTH_SHORT).show();
            // permission
        });


        List<SearchItem> suggestionsList = new ArrayList<>();
        suggestionsList.add(new SearchItem("search1"));
        suggestionsList.add(new SearchItem("search2"));

        SearchAdapter searchAdapter = new SearchAdapter(this, suggestionsList);
        searchAdapter.addOnItemClickListener((view, position) -> {
            Toast.makeText(MainActivity.this, "onItemClick", Toast.LENGTH_SHORT).show();
//          TextView movieTitle = (TextView) view.findViewById(R.id.textView_item_text);
//          String query = textView.getText().toString();
//          getData(query, position);
//          mSearchView.close(false);
        });
        mSearchView.setAdapter(searchAdapter);

        /*suggestionsList.add(new SearchItem("search12"));
        suggestionsList.add(new SearchItem("search22"));
        suggestionsList.add(new SearchItem("search32"));
        searchAdapter.notifyDataSetChanged();*/
    }

    private void configureSearchViewBehaviour() {
        mSearchView.setHint(R.string.search);
        mSearchView.setNavigationIcon(R.drawable.ic_search_black_24dp);
        mSearchView.setOnMenuClickListener(() -> mSearchView.open(true));

        // Define filters
        List<SearchFilter> filters = new ArrayList<>();
        filters.add(new SearchFilter(getString(R.string.movies), true));
        filters.add(new SearchFilter(getString(R.string.actors), false));
        mSearchView.setFilters(filters);
        //use mSearchView.getFiltersStates() to consider filter when performing search

        mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
            @Override
            public boolean onOpen() {
                mBottomNavigation.hideBottomNavigation(false);
                mSearchView.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
                return true;
            }
            @Override
            public boolean onClose() {
                mBottomNavigation.restoreBottomNavigation(true);
                mSearchView.setNavigationIcon(R.drawable.ic_search_black_24dp);
                return true;
            }
        });
    }

    // --------------------------- STATES ----------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(MOVIES_KEY, mMoviesAdapter.getMoviesData());
    }

//    @Override // TODO handle restore
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//    }

    // ------------------------ CURSOR LOADER ------------------------

    private static final int ID_MOVIE_FAVOURITES_LOADER = 14;

    public static final String[] MOVIE_DETAILS_PROJECTION = {
                            MoviesContract.MoviesEntry._ID,
                            MoviesContract.MoviesEntry.COLUMN_POSTER,};

    public static final int INDEX_ID = 0,
                            INDEX_POSTER = 1;

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        if(loaderId != ID_MOVIE_FAVOURITES_LOADER)
            throw new RuntimeException("Loader Not Implemented: " + loaderId);

        return new CursorLoader(this, MoviesContract.CONTENT_URI, MOVIE_DETAILS_PROJECTION,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // REVIEW after startDelete it comes here and fails.... Why?
        if(data == null || data.getCount() == 0) {
            Log.e(TAG, "onLoadFinished: Problems retrieving favourite from DB");
            return;
        }

        data.moveToFirst();
        MovieLight[] movies = TMDBUtils.extractLightMoviesFromCursor(data);
        data.close();

        // Adapt the interface
        mMoviesAdapter.setMoviesData(movies);
        mRecyclerView.smoothScrollToPosition(0);
        mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.setMoviesData(null);
    }
}
