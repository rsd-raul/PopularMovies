package com.raul.rsd.android.popularmovies.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
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
import java.util.Random;

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
    @BindView(R.id.tv_read_more) TextView readMoreButton;
    @BindView(R.id.tv_place_birth_main) TextView placeBirthMain;
    @BindView(R.id.tv_years_main) TextView yearsMain;
    @BindView(R.id.tv_movies_count_main) TextView movieCountMain;
    @BindView(R.id.rv_movies) RecyclerView mMoviesRV;
    @BindView(R.id.iv_actor_profile) ImageView mProfileImageView;
    @Nullable @BindView(R.id.iv_actor_backdrop) ImageView mBackdropImageView;
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
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        biographyMain.setOnClickListener(view -> {
            if(biographyMain.getText().length() > 0)
                DialogsUtils.showBasicDialog(this, getString(R.string.biography), mActor.getBiography());
            });

        // Retrieve the ID sent and fetch the actor from TMDB
        startNetworkRequest();
    }

    private void startNetworkRequest(){
        // Notify the user if there is no internet, offer to retry or to close the app
        if(!NetworkUtils.isNetworkAvailable(ActorActivity.this)) {
            DialogsUtils.showErrorDialog(ActorActivity.this, (dialog, which) -> startNetworkRequest());
            return;
        }

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

        // Setup poster if available
        String profile = mActor.getProfile_path();
        if (profile != null && profile.length() > 5) {
            Picasso.with(this)
                    .load(NetworkUtils.buildActorProfileUri(mActor.getProfile_path()))
                    .placeholder(R.drawable.placeholder_poster)
                    .into(mProfileImageView);

            // If is portrait, hide/show the profile picture on scroll
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                // Customize the Appbar behaviour and react to scroll
                ((AppBarLayout) findViewById(R.id.app_bar))
                    .addOnOffsetChangedListener((appBarLayout1, verticalOffset) ->
                        UIUtils.actionBarScrollControl(verticalOffset, mProfileImageView, mSpace));

        // If not, adapt the interface not to show it
        }else {
            mProfileImageView.setVisibility(View.GONE);
            mSpace.setVisibility(View.GONE);
        }

        if (mBackdropImageView != null) {
            // Setup backdrop
            int moviesLength = mActor.getMovies().length;
            if (moviesLength > 0) {

                // Make sure the movie has a valid poster before loading it
                String path;
                do{
                    int i = new Random().nextInt(moviesLength);
                    path = mActor.getMovies()[i].getPoster_path();
                } while (path == null || path.length() < 5);

                Picasso.with(this)
                        .load(NetworkUtils.buildMovieBackdropUri(path))
                        .placeholder(R.drawable.placeholder_poster)
                        .into(mBackdropImageView);
            } else
                mBackdropImageView.setImageResource(R.drawable.header_background_default);
        }






        String notAvailable = getString(R.string.not_available);

        // Name
        nameMain.setText(mActor.getName());

        // Biography
        String biography = mActor.getBiography();
        if(biography == null || biography.length() == 0) {
            biography = notAvailable;
            readMoreButton.setVisibility(View.GONE);
        } else
            readMoreButton.setOnClickListener(view -> DialogsUtils.showBasicDialog(this,
                    getString(R.string.biography), mActor.getBiography()));
        biographyMain.setText(biography);

        // Place of birth
        String place = mActor.getPlace_of_birth();
        placeBirthMain.setText(place == null || place.length() == 0 ? notAvailable : place);

        // Birthday
        Date birthDate = mActor.getBirthday();
        String birthDateStr;
        if(birthDate != null)
            birthDateStr = DateUtils.getStringFromDate(mActor.getBirthday());
        else
            birthDateStr = notAvailable;
        birthDeathMain.setText(getString(R.string.format_string_string,
                                                    getString(R.string.birthday), birthDateStr));
        // Death day
        Date endDate = new Date();
        String deathDay = mActor.getDeathday();
        if(deathDay != null && deathDay.length()==10) {
            endDate = DateUtils.getDateFromTMDBSString(deathDay);
            birthDeathMain.append("\n" + getString(R.string.format_string_string,
                    getString(R.string.death_day),
                    DateUtils.getStringFromDate(endDate)));
        }

        // Age
        String ageStr;
        if(birthDate != null)
            ageStr = String.valueOf(DateUtils.calculateYearsBetweenDates(birthDate, endDate));
        else
            ageStr = getString(R.string.not_available_short);
        yearsMain.setText(ageStr);

        // Movies
        MovieLight[] moviesList = mActor.getMovies();
        int movies = moviesList != null ? moviesList.length : 0;
        movieCountMain.setText(String.valueOf(movies));

        setupMovies(moviesList);
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
            if(movie.getPoster_path() != null && movie.getPoster_path().length() > 5)
                fAdapter.add(movieItemProvider.get().withMovie(movie.getId(), movie.getPoster_path(), movie.getCharacter()));

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

    @OnClick(R.id.share_actor)
    void shareActor(){
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