package com.raul.rsd.android.popularmovies.Adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.raul.rsd.android.popularmovies.Domain.MovieLight;
import com.raul.rsd.android.popularmovies.R;
import com.raul.rsd.android.popularmovies.Utils.NetworkUtils;
import com.squareup.picasso.Picasso;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    // ------------------------- ATTRIBUTES --------------------------

    private MovieLight[] mMovies;
    private final MoviesAdapterOnClickHandler mClickHandler;

    // ------------------------- CONSTRUCTOR -------------------------

    public MoviesAdapter(MoviesAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

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
        Uri posterUri = NetworkUtils.buildMoviePosterURI(poster_path);

        Picasso.with(moviePoster.getContext())
                .load(posterUri)
                .placeholder(R.drawable.placeholder_poster)
                .into(moviePoster);
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


    // -------------------------- INTERFACE --------------------------

    public interface MoviesAdapterOnClickHandler {
        void onClick(long selectedMovieId);
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
            mClickHandler.onClick(selectedMovieId);
        }
    }
}