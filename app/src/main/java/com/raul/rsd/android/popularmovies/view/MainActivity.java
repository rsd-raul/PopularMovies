package com.raul.rsd.android.popularmovies.view;

import android.content.Intent;
import android.database.Cursor;
import android.speech.RecognizerIntent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchFilter;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.raul.rsd.android.popularmovies.App;
import com.raul.rsd.android.popularmovies.R;
import com.raul.rsd.android.popularmovies.adapters.MovieItem;
import com.raul.rsd.android.popularmovies.data.MoviesContract;
import com.raul.rsd.android.popularmovies.domain.Actor;
import com.raul.rsd.android.popularmovies.domain.ActorList;
import com.raul.rsd.android.popularmovies.domain.MovieLight;
import com.raul.rsd.android.popularmovies.domain.MoviesList;
import com.raul.rsd.android.popularmovies.utils.DialogsUtils;
import com.raul.rsd.android.popularmovies.utils.extras.EndlessRecyclerViewScrollListener;
import com.raul.rsd.android.popularmovies.utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.utils.TMDBUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.raul.rsd.android.popularmovies.data.MoviesContract.*;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    // --------------------------- VALUES ----------------------------

    private static final String TAG = "MainActivity";
    private static final String MOVIES_KEY = "movies_key";
    private static final String ACTIVE_SORT_KEY = "active_sort_key";
    private static final String NEXT_PAGE_KEY = "next_page_key";

    // ------------------------- ATTRIBUTES --------------------------

    @BindView(R.id.rv_movies) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.bottom_navigation) AHBottomNavigation mBottomNavigation;
    @BindView(R.id.searchView) SearchView mSearchView;
    @Inject FastItemAdapter<MovieItem> mFastAdapter;
    @Inject Provider<MovieItem> mMovieItemProvider;
    private String mActiveSort = NetworkUtils.POPULAR;   // By default to popular
    private EndlessRecyclerViewScrollListener mScrollListener;
    private boolean mNoScroll = false;
    private int mNextPage = 1;
    private ArrayList<MovieLight> mMoviesShown = new ArrayList<>();

    // ------------------------- CONSTRUCTOR -------------------------

    @Override
    protected void inject(App.AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_main);

        // Initialize if necessary.
        NetworkUtils.setImagesSizeWithDpi(getResources().getDisplayMetrics().densityDpi);
        ButterKnife.bind(this);

        // Set ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setTitle(R.string.none);

        // Retrieve data if saved previously
        if(savedState != null) {
            if (savedState.containsKey(ACTIVE_SORT_KEY))
                mActiveSort = savedState.getString(ACTIVE_SORT_KEY);
            if (savedState.containsKey(NEXT_PAGE_KEY))
                mNextPage = savedState.getInt(NEXT_PAGE_KEY);
        }

        // Prepare RecyclerView options
        int columnNumber = getResources().getBoolean(R.bool.is_landscape) ? 3 : 2;
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(columnNumber,
                                                            StaggeredGridLayoutManager.VERTICAL);
        mScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if(!mActiveSort.equals(NetworkUtils.FAVOURITES)) {
                    mSwipeRefresh.setRefreshing(true);
                    queryTMDbServer();
                }
            }
        };
        mFastAdapter.withOnClickListener((v, adapter, movieItem, position) -> {
            Intent intentDetailsActivity = new Intent(this, MovieActivity.class);
            intentDetailsActivity.putExtra(Intent.EXTRA_UID, movieItem.id);
            this.startActivity(intentDetailsActivity);
            return true;
        });

        // Configure RecyclerView
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnScrollListener(mScrollListener);
        mRecyclerView.setAdapter(mFastAdapter);
        mRecyclerView.setHasFixedSize(true);

        // Configure BottomNavigationBar, SearchView and SwipeRefresh
        configureBottomNavigationBar();
        configureSearchView();
        mSwipeRefresh.setOnRefreshListener(() -> {
            resetStatuses();
            loadData();
        });

        // Finally, if we have the data already saved, restore those
        if(savedState != null && savedState.containsKey(MOVIES_KEY) && !mActiveSort.equals(NetworkUtils.FAVOURITES)) {
            ArrayList<MovieLight> storedMovies = savedState.getParcelableArrayList(MOVIES_KEY);
            if(storedMovies != null && storedMovies.size() != 0)
                addMoviesToRV(storedMovies.toArray(), false);
            mFastAdapter.withSavedInstanceState(savedState);
        } else
            loadData();
    }

    private void addMoviesToRV(Object[] moviesToAdd, boolean clearFirst){
        if(clearFirst)
            mFastAdapter.clear();

        if(moviesToAdd == null)
            return;

        for (Object object : moviesToAdd) {
            MovieLight movie = (MovieLight) object;
            if (movie.getPoster_path() != null || movie.getPoster() != null)
                mMoviesShown.add(movie);
                mFastAdapter.add(mMovieItemProvider.get().withMovie(movie.getId(),
                                                                    movie.getPoster_path(),
                                                                    movie.getPoster()));
        }
        mFastAdapter.notifyDataSetChanged();
    }

    // -------------------------- USE CASES --------------------------

    public void loadData(){

        // Notify the user if there is no internet, offer to retry or to close the app
        if(!mActiveSort.equals(NetworkUtils.FAVOURITES) && !NetworkUtils.isNetworkAvailable(this)) {
            addMoviesToRV(null, true);
            DialogsUtils.showNetworkDialogMainActivity(this,
                    // Go favourites
                    (dialog, which) -> mBottomNavigation.setCurrentItem(3),
                    // Retry
                    (dialog, which) -> loadData());
            return;
        }

        mSwipeRefresh.setRefreshing(true);

        // If Favourites, start the CursorLoader, else, start network request for the first page
        if (mActiveSort.equals(NetworkUtils.FAVOURITES))
            queryFavouritesDB();
        else
            queryTMDbServer();
    }

    private void queryTMDbServer(){
        NetworkUtils.getMoviesByFilter(mActiveSort, mNextPage, new Callback<MoviesList>() {
            @Override
            public void onResponse(Call<MoviesList> call, Response<MoviesList> response) {

                MovieLight[] movies = response.body().getResults();
                if (movies != null) {
                    addMoviesToRV(movies, mNextPage == 1);
                    if(mNextPage == 1)
                        mRecyclerView.smoothScrollToPosition(0);
                } else
                    onFailure(null, null);

                mSwipeRefresh.setRefreshing(false);

                mNextPage++;
            }

            @Override
            public void onFailure(Call<MoviesList> call, Throwable t) {
                Log.e(TAG, "loadData/onFailure: Failed to fetch movies from Server", t);
                DialogsUtils.showFetchingDataDialog(MainActivity.this, (dialog, which) -> queryTMDbServer());
            }
        });
    }

    // --------------------------- STATES ----------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ACTIVE_SORT_KEY, mActiveSort);
        outState.putInt(NEXT_PAGE_KEY, mNextPage);
        outState.putParcelableArrayList(MOVIES_KEY, mMoviesShown);
        outState = mFastAdapter.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    // -------------------------- AUXILIARY --------------------------

    private void resetStatuses(){
        mNextPage = 1;
        mScrollListener.resetState();
        mMoviesShown = new ArrayList<>();
    }

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
                R.string.upcoming, R.drawable.ic_schedule_24dp, R.color.indigoDark));
        mBottomNavigation.addItem(new AHBottomNavigationItem(
                R.string.favourites, R.drawable.ic_favorite_border_24dp, R.color.redDark));

        // Display color under navigation bar (API 21+)
        mBottomNavigation.setTranslucentNavigationEnabled(true);

        // Manage titles
        mBottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);

        // Use colored navigation with circle reveal effect
        mBottomNavigation.setColored(true);

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
                    mActiveSort = NetworkUtils.UPCOMING;
                    break;
                case 3:
                    mActiveSort = NetworkUtils.FAVOURITES;
                    break;
            }
            resetStatuses();
            loadData();
            return true;
        });
    }

    // ------------------------- SEARCH VIEW -------------------------

    private SearchAdapter mSearchAdapter;
    private MovieLight[] mMoviesFound;
    private Actor[] mActorsFound;
    private Toast wrongFilter = null;

    private void configureSearchView() {
        if (mSearchView == null)
            return;
        configureSearchViewBehaviour();
        configureSearchViewFilters();

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
                if(filtersLength < 1 || filtersLength > 5) {
                    if (wrongFilter == null)
                        wrongFilter = Toast.makeText(MainActivity.this, R.string.select_filter, Toast.LENGTH_SHORT);
                    wrongFilter.show();
                }
                return false;
            }
        });

        mSearchView.setVoiceText(getString(R.string.say_movie_or_actor));
        mSearchView.setOnVoiceClickListener(() -> {
            mSearchView.open(false);
            mSearchView.hideKeyboard();
        });

        mSearchAdapter.addOnItemClickListener((view, position) -> {
            Class itemClass;
            long itemId;

            if(mMoviesFound != null){
                itemClass = MovieActivity.class;
                itemId = mMoviesFound[position].getId();

            } else if(mActorsFound != null){
                itemClass = ActorActivity.class;
                itemId = mActorsFound[position].getId();

            } else {
                Toast.makeText(this, R.string.select_filter, Toast.LENGTH_SHORT).show();
                return;
            }

            mSearchView.close(false);
            mMoviesFound = null; mActorsFound = null;

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

        mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
            @Override
            public boolean onOpen() {
                mBottomNavigation.hideBottomNavigation(false);
                mSearchView.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
                if(!NetworkUtils.isNetworkAvailable(MainActivity.this))
                    Toast.makeText(MainActivity.this, R.string.no_network_title, Toast.LENGTH_SHORT).show();
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

    private void configureSearchViewFilters(){
        List<SearchFilter> filters = new ArrayList<>();
        filters.add(new SearchFilter(getString(R.string.movie), true));
        filters.add(new SearchFilter(getString(R.string.actor), false));
        mSearchView.setFilters(filters);

        // Custom filter behaviour
        LinearLayout mFiltersContainer = (LinearLayout) findViewById(R.id.filters_container);
        AppCompatCheckBox movie = (AppCompatCheckBox) mFiltersContainer.getChildAt(0);
        AppCompatCheckBox actor = (AppCompatCheckBox) mFiltersContainer.getChildAt(1);

        movie.setOnClickListener(view -> actor.setChecked(!actor.isChecked()));
        actor.setOnClickListener(view -> movie.setChecked(!movie.isChecked()));
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

        mMoviesFound = null; mActorsFound = null;

        mSearchView.showProgress();

        switch (getActiveFilters()){
            case "movie":
                NetworkUtils.findMovieByName(query, 1, new Callback<MoviesList>() {
                    @Override
                    public void onResponse(Call<MoviesList> call, Response<MoviesList> response) {
                        if(response == null || response.body() == null)
                            return;

                        // Set the actors in the suggestion list
                        mMoviesFound = response.body().getResults();
                        List<SearchItem> suggestionsList = new ArrayList<>();
                        for(MovieLight movie : mMoviesFound)
                            suggestionsList.add(new SearchItem(movie.getTitle()));
                        mSearchAdapter.setData(suggestionsList);

                        mSearchView.hideProgress();
                    }

                    @Override
                    public void onFailure(Call<MoviesList> call, Throwable t) {
                        Log.e(TAG, "findData: onFailure: Problems retrieving the movies from TMDb ", t);
                        mSearchView.hideProgress();
                    }
                });
                break;

            case "actor":
                NetworkUtils.findActorByName(query, 1, new Callback<ActorList>() {
                    @Override
                    public void onResponse(Call<ActorList> call, Response<ActorList> response) {
                        if(response == null || response.body() == null)
                            return;

                        // Set the actors in the suggestion list
                        mActorsFound = response.body().getResults();
                        List<SearchItem> suggestionsList = new ArrayList<>();
                        for(Actor actor : mActorsFound)
                            suggestionsList.add(new SearchItem(actor.getName()));
                        mSearchAdapter.setData(suggestionsList);

                        mSearchView.hideProgress();
                    }

                    @Override
                    public void onFailure(Call<ActorList> call, Throwable t) {
                        Log.e(TAG, "findData: onFailure: Problems retrieving the actors from TMDb ", t);
                        mSearchView.hideProgress();
                    }
                });
                break;

            default:
                mSearchView.hideProgress();
                Toast.makeText(this, R.string.select_filter, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SearchView.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && results.size() > 0) {
                String searchWrd = results.get(0);
                if (!TextUtils.isEmpty(searchWrd) && mSearchView != null)
                    mSearchView.setQuery(searchWrd, true);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // ------------------------ CURSOR LOADER ------------------------

    private static final int ID_MOVIE_FAVOURITES_LOADER = 14;

    public static final String[] BASE_PROJECTION = { MoviesEntry._ID, MoviesEntry.COLUMN_POSTER};
    public static final int INDEX_ID = 0, INDEX_POSTER = 1;

    private void queryFavouritesDB(){
        LoaderManager loaderManager = getSupportLoaderManager();
        if(loaderManager.getLoader(ID_MOVIE_FAVOURITES_LOADER) == null)
            loaderManager.initLoader(ID_MOVIE_FAVOURITES_LOADER, null, this) ;
        else
            loaderManager.restartLoader(ID_MOVIE_FAVOURITES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        if(loaderId != ID_MOVIE_FAVOURITES_LOADER)
            throw new RuntimeException("Loader Not Implemented: " + loaderId);

        return new CursorLoader(this, MoviesContract.CONTENT_URI, BASE_PROJECTION,
                                null, null, MoviesContract.MoviesEntry.COLUMN_TIMESTAMP + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null) {
            Log.e(TAG, "onLoadFinished: Problems retrieving favourite from DB");
            return;
        }

        data.moveToFirst();
        MovieLight[] movies = TMDBUtils.extractLightMoviesFromCursor(data);
        data.close();

        // Adapt the interface
        addMoviesToRV(movies, true);
        mSwipeRefresh.setRefreshing(false);

        if(mNoScroll)
            mNoScroll = false;
        else
            mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        addMoviesToRV(null, true);
    }
}