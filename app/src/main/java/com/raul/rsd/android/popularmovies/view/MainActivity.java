package com.raul.rsd.android.popularmovies.view;

import android.content.Intent;
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
import com.raul.rsd.android.popularmovies.domain.Actor;
import com.raul.rsd.android.popularmovies.domain.ActorList;
import com.raul.rsd.android.popularmovies.domain.MovieLight;
import com.raul.rsd.android.popularmovies.domain.MoviesList;
import com.raul.rsd.android.popularmovies.utils.DialogsUtils;
import com.raul.rsd.android.popularmovies.utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.utils.TMDBUtils;
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
        mSwipeRefresh.setOnRefreshListener(this::loadData);

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

        // If Favourites, start the CursorLoader
        if(mActiveSort.equals(NetworkUtils.FAVOURITES)){
            LoaderManager loaderManager = getSupportLoaderManager();
            if(loaderManager.getLoader(ID_MOVIE_FAVOURITES_LOADER) == null)
                loaderManager.initLoader(ID_MOVIE_FAVOURITES_LOADER, null, this) ;
            else
                loaderManager.restartLoader(ID_MOVIE_FAVOURITES_LOADER, null, this);

        // If Top rated or Popular, start network request
        }else
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
    }

    private void showErrorMessage(){
        DialogsUtils.showFetchingDataDialog(this, (dialog, which) -> loadData());
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
//        Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show();
//    }

    // -------------------------- AUXILIARY --------------------------

    boolean mNoScroll = false;

    @Override
    protected void onResume() {
        super.onResume();

        // If coming back to Favourites most likely from Details reload but do not scroll to top
        if(mActiveSort.equals(NetworkUtils.FAVOURITES)) {
            loadData();
            mNoScroll = true;
        }
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
                    break;
                case 1:
                    mActiveSort = NetworkUtils.TOP_RATED;
                    break;
                case 2:
                    mActiveSort = NetworkUtils.FAVOURITES;
                    break;
            }
            loadData();
            return true;
        });
    }

    // ------------------------- SEARCH VIEW -------------------------

    private SearchAdapter mSearchAdapter;
    private MovieLight[] mMoviesFound;
    private Actor[] mActorsFound;
//    mHistoryDatabase.clearDatabase();     // TODO allow option on Settings

    private void configureSearchView() {
        if (mSearchView == null)
            return;
        configureSearchViewBehaviour();

        // Find data
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                findData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                int filtersLength = getActiveFilters().length();
                if(filtersLength < 1 || filtersLength > 5)
                    Toast.makeText(MainActivity.this, R.string.select_filter, Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mSearchView.setVoiceText("Set permission on Android 6.0+ !");
        mSearchView.setOnVoiceClickListener(() -> {
            Toast.makeText(MainActivity.this, "onVoiceClick", Toast.LENGTH_SHORT).show();
            // permission
        });


        mSearchAdapter.addOnItemClickListener((view, position) -> {
            // Get the query and add it to the DB
//            TextView movieTitle = (TextView) view.findViewById(R.id.textView_item_text);
//            String query = movieTitle.getText().toString();

            Class itemClass;
            long itemId;
            if(mMoviesFound != null){
                Log.e(TAG, "configureSearchView: CLICKING MOVIE");

                MovieLight movie = mMoviesFound[position];
                mMoviesFound = null;
                mSearchView.close(false);

                itemClass = MovieActivity.class;
                itemId = movie.getId();

            } else if(mActorsFound != null){
                Log.e(TAG, "configureSearchView: CLICKING ACTOR");

                Actor actor = mActorsFound[position];
                mActorsFound = null;
                mSearchView.close(false);

                itemClass = ActorActivity.class;
                itemId = actor.getId();

            } else {
                Log.e(TAG, "configureSearchView: BAD FILTER");

                Toast.makeText(this, R.string.select_filter, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intentDetailsActivity = new Intent(this, itemClass);
            intentDetailsActivity.putExtra(Intent.EXTRA_UID, itemId);
            startActivity(intentDetailsActivity);
        });
        mSearchView.setAdapter(mSearchAdapter);
    }




    private void configureSearchViewBehaviour() {
        mSearchAdapter = new SearchAdapter(this);
        mSearchView.setHint(R.string.search);
        mSearchView.setShouldClearOnClose(true);
        mSearchView.setNavigationIcon(R.drawable.ic_search_black_24dp);
        mSearchView.setOnMenuClickListener(() -> mSearchView.open(true));
        // Define filters
        List<SearchFilter> filters = new ArrayList<>();
        filters.add(new SearchFilter(getString(R.string.movie), true));
        filters.add(new SearchFilter(getString(R.string.actors), false));
        mSearchView.setFilters(filters);

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
                mMoviesFound = null;
                mActorsFound = null;
                return true;
            }
        });
    }

    /**
     * Process the enabled filters on the SearchView, concatenate the active filters.
     *
     * @return A string of concatenated filters
     */
    private String getActiveFilters(){
        List<Boolean> filters = mSearchView.getFiltersStates();
        String active = "";
        if(filters.get(0)) active += "movie";
        if(filters.get(1)) active += "actor";
        return active;
    }

    private void findData(String query){
        if(query == null || query.equals(""))
            return;

        mMoviesFound = null;
        mActorsFound = null;

        mSearchView.showProgress();

        switch (getActiveFilters()){
            case "movie":
                NetworkUtils.findMovieByName(query, 1, new Callback<MoviesList>() {
                    @Override
                    public void onResponse(Call<MoviesList> call, Response<MoviesList> response) {
                        if(response == null || response.body() == null)
                            return;

                        mMoviesFound = response.body().getResults();

                        List<SearchItem> suggestionsList = new ArrayList<>();
                        for(MovieLight movie : mMoviesFound)
                            suggestionsList.add(new SearchItem(movie.getTitle()));
//                        mSearchAdapter.setSuggestionsList(suggestionsList);
                        mSearchAdapter.setData(suggestionsList);

                        mSearchView.hideProgress();
                    }

                    @Override
                    public void onFailure(Call<MoviesList> call, Throwable t) { }
                });
                break;

            case "actor":
                NetworkUtils.findActorByName(query, 1, new Callback<ActorList>() {
                    @Override
                    public void onResponse(Call<ActorList> call, Response<ActorList> response) {
                        if(response == null || response.body() == null)
                            return;

                        mActorsFound = response.body().getResults();

                        List<SearchItem> suggestionsList = new ArrayList<>();
                        for(Actor actor : mActorsFound)
                            suggestionsList.add(new SearchItem(actor.getName()));
                        mSearchAdapter.setData(suggestionsList);

                        mSearchView.hideProgress();
                    }

                    @Override
                    public void onFailure(Call<ActorList> call, Throwable t) { }
                });
                break;

            default:
                mSearchView.hideProgress();
                Toast.makeText(this, R.string.select_filter, Toast.LENGTH_SHORT).show();
                break;
        }
        // TODO cancel request if not finished and there is a new one
    }

    // ------------------------ CURSOR LOADER ------------------------

    private static final int ID_MOVIE_FAVOURITES_LOADER = 14;

    public static final String[] MOVIE_DETAILS_PROJECTION = {
                            MoviesContract.MoviesEntry._ID,
                            MoviesContract.MoviesEntry.COLUMN_POSTER};

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
        if(data == null) {
            Log.e(TAG, "onLoadFinished: Problems retrieving favourite from DB");
            return;
        }

        data.moveToFirst();
        MovieLight[] movies = TMDBUtils.extractLightMoviesFromCursor(data);
        data.close();

        // Adapt the interface
        mMoviesAdapter.setMoviesData(movies);
        mSwipeRefresh.setRefreshing(false);

        if(mNoScroll) {
            mNoScroll = false;
            return;
        }

        mNoScroll = false;
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.setMoviesData(null);
    }
}
