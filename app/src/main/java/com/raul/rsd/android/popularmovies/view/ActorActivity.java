package com.raul.rsd.android.popularmovies.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.raul.rsd.android.popularmovies.App;
import com.raul.rsd.android.popularmovies.R;
import com.raul.rsd.android.popularmovies.adapters.MovieItem;
import com.raul.rsd.android.popularmovies.domain.Actor;
import com.raul.rsd.android.popularmovies.domain.MovieLight;
import com.raul.rsd.android.popularmovies.utils.DateUtils;
import com.raul.rsd.android.popularmovies.utils.DialogsUtils;
import com.raul.rsd.android.popularmovies.utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.utils.TMDBUtils;
import com.raul.rsd.android.popularmovies.utils.UIUtils;
import com.squareup.picasso.Picasso;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Provider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class ActorActivity extends BaseActivity {

    // --------------------------- VALUES ----------------------------

    private static final String TAG = "ActorActivity";

    // ------------------------- ATTRIBUTES --------------------------

    @BindView(R.id.tv_name) TextView nameMain;
    @BindView(R.id.tv_birth_death_date) TextView birthDeathMain;
    @BindView(R.id.tv_biography_main) TextView biographyMain;
    @BindView(R.id.tv_place_birth_main) TextView placeBirthMain;
    @BindView(R.id.rv_movies) RecyclerView mMoviesRV;
    @BindView(R.id.iv_actor_profile) ImageView mProfileImageView;
    @BindView(R.id.poster_space) Space mSpace;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Inject Provider<FastItemAdapter<IItem>> fastAdapterProvider;
    @Inject Provider<MovieItem> movieItemProvider;
    private Actor mActor;

    // ------------------------- CONSTRUCTOR -------------------------

    @Override
    protected void inject(App.AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actor);
        ButterKnife.bind(this);

        // Disable the swipe to refresh
        mSwipeRefreshLayout.setEnabled(false);

        // Set ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        biographyMain.setOnClickListener(view -> {
            if(biographyMain.getText().length() > 0)
                dialogBiography();
            });

        // Retrieve the ID sent and fetch the actor from TMDB
        startNetworkRequest();
    }

    private void startNetworkRequest(){
        Log.e(TAG, "startNetworkRequest: ");

        // Notify the user if there is no internet, offer to retry or to close the app
        if(!NetworkUtils.isNetworkAvailable(ActorActivity.this))
            DialogsUtils.showErrorDialog(ActorActivity.this, (dialog, which) -> startNetworkRequest());

        mSwipeRefreshLayout.setRefreshing(true);

        long id = getIntent().getLongExtra(Intent.EXTRA_UID, -1);

        NetworkUtils.getFullActorById(id, new retrofit2.Callback<Actor>() {
            @Override
            public void onResponse(Call<Actor> call, Response<Actor> response) {
                mActor = response.body();

                if (mActor != null)
                    displayActor();
                else
                    showErrorMessage();
            }

            @Override
            public void onFailure(Call<Actor> call, Throwable t) {
                Log.e(TAG, "loadData/onFailure: Failed to fetch movie from Server", t);
                showErrorMessage();
            }
        });
    }

    private void showErrorMessage(){
        DialogsUtils.showErrorDialog(this, (dialog, which) -> startNetworkRequest());
    }

    private void displayActor(){

        mSwipeRefreshLayout.setRefreshing(false);

        Log.e(TAG, "displayActor: " + mActor.getDeathday());

        // Setup poster
        if(mActor.getProfile_path() != null)
            Picasso.with(this)
                    .load(NetworkUtils.buildActorProfileUri(mActor.getProfile_path()))
                    .placeholder(R.drawable.placeholder_poster)
                    .into(mProfileImageView);

        setupMovies(mActor.getMovies());

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            // Customize the Appbar behaviour and react to scroll
            ((AppBarLayout) findViewById(R.id.app_bar))
                .addOnOffsetChangedListener( (appBarLayout1, verticalOffset) ->
                    UIUtils.actionBarScrollControl(verticalOffset, mProfileImageView, mSpace));

        // Fill interface with formatted movie details
        nameMain.setText(mActor.getName());

        // TODO calculate age and show
        String deathDay = mActor.getDeathday();
        boolean noDeathDay = deathDay == null;    // TODO deathDay not working
        birthDeathMain.setText(DateUtils.getStringFromDate(mActor.getBirthday()));
        biographyMain.setText(mActor.getBiography());
        placeBirthMain.setText(mActor.getPlace_of_birth());
    }

    @OnClick(R.id.tv_read_more)
    void dialogBiography(){
        DialogsUtils.showBasicDialog(this, getString(R.string.biography), mActor.getBiography());
    }

    private void setupMovies(MovieLight[] movies){
        int visibility = View.VISIBLE;
        if(movies == null || movies.length == 0)
            visibility = View.GONE;

        mMoviesRV.setVisibility(visibility);
        findViewById(R.id.tv_movies_header).setVisibility(visibility);
        if(visibility == View.GONE)
            return;

        FastItemAdapter<IItem> fAdapter = fastAdapterProvider.get();
        mMoviesRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mMoviesRV.setAdapter(fAdapter);
        for(MovieLight movie : movies)
            fAdapter.add(movieItemProvider.get().withMovie(movie.getId(), movie.getPoster_path()));

        fAdapter.withOnClickListener((v, adapter, item, position) -> {
            MovieItem mi = (MovieItem) item;
            Intent intentDetailsActivity = new Intent(this, MovieActivity.class);
            intentDetailsActivity.putExtra(Intent.EXTRA_UID, mi.id);
            startActivity(intentDetailsActivity);
            return true;
        });
    }


    // -------------------------- INTERFACE --------------------------


    // -------------------------- USE CASES --------------------------

    @OnClick(R.id.fab)
    void shareMovie(){
        // If the movie has not been fetched yet, don't try to share
        if(mActor == null)
            return;

        // Create the Intent and put the info to share in a custom dialog, not using defaults
        Intent shareIntent = ShareCompat.IntentBuilder
                .from(this)
                .setType("text/plain")
                .setText(TMDBUtils.toStringActor(mActor, this))
                .setChooserTitle(R.string.share_movie_chooser)
                .createChooserIntent();

        // Avoid ActivityNotFoundException
        if(shareIntent.resolveActivity(getPackageManager()) != null)
            startActivity(shareIntent);
    }

    // -------------------------- AUXILIARY --------------------------

    // --------------------------- DETAILS ---------------------------

    // Overriding "back" not to reload the Main Activity as setting parentActivityName would
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}