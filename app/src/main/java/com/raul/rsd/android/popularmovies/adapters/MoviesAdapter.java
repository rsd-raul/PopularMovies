package com.raul.rsd.android.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.raul.rsd.android.popularmovies.domain.MovieLight;
import com.raul.rsd.android.popularmovies.R;
import com.raul.rsd.android.popularmovies.utils.NetworkUtils;
import com.raul.rsd.android.popularmovies.view.MovieActivity;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    // ------------------------- ATTRIBUTES --------------------------

    private MovieLight[] mMovies;

    // ------------------------- CONSTRUCTOR -------------------------

    @Inject
    public MoviesAdapter() { }

    // -------------------------- OVERRIDE ---------------------------

    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.movie_list_item, viewGroup, false);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MoviesAdapterViewHolder viewHolder, int position) {
        ImageView moviePoster = viewHolder.mMoviePoster;

        String poster_path = mMovies[position].getPoster_path();

        if(poster_path != null) {
            Uri posterUri = NetworkUtils.buildMoviePosterUri(poster_path);
            Picasso.with(moviePoster.getContext())
                    .load(posterUri)
                    .placeholder(R.drawable.placeholder_poster)
                    .into(moviePoster);
        } else
            moviePoster.setImageBitmap(mMovies[position].getPoster());
    }

    @Override
    public int getItemCount() {
        return mMovies != null ? mMovies.length : 0;
    }

    // -------------------------- USE CASES --------------------------

    public void setMoviesData(MovieLight[] movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    public MovieLight[] getMoviesData() {
        return mMovies;
    }

    // ------------------------- VIEW HOLDER -------------------------

    class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView mMoviePoster;

        MoviesAdapterViewHolder(View view) {
            super(view);
            mMoviePoster = (ImageView) view.findViewById(R.id.iv_movie_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            long selectedMovieId = mMovies[adapterPosition].getId();
            Context context = v.getContext();

            Intent intentDetailsActivity = new Intent(context, MovieActivity.class);
            intentDetailsActivity.putExtra(Intent.EXTRA_UID, selectedMovieId);
            context.startActivity(intentDetailsActivity);
        }
    }
}