package com.raul.rsd.android.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.raul.rsd.android.popularmovies.R;
import com.raul.rsd.android.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;
import java.util.List;

import javax.inject.Inject;

public class ActorMovieItem extends AbstractItem<ActorMovieItem, ActorMovieItem.ViewHolder> {

    // ------------------------- ATTRIBUTES --------------------------

    public long id;
    private String poster_path, character;
    private Context context;

    // ------------------------- CONSTRUCTOR -------------------------

    @Inject
    ActorMovieItem(Context context) {
        this.context = context;
    }

    public ActorMovieItem withMovie(long id, String poster_path, String character){
        this.poster_path = poster_path;
        this.character = character;
        this.id = id;
        return this;
    }

    @Override
    public int getLayoutRes() { return R.layout.actor_movie_item; }

    // -------------------------- AUXILIARY --------------------------

    @Override
    public int getType() { return R.id.iv_movie_poster; }

    // -------------------------- USE CASES --------------------------

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.character.setText(character);
        Picasso.with(context)
                .load(NetworkUtils.buildMoviePosterUri(poster_path))
                .placeholder(R.drawable.placeholder_poster)
                .into(viewHolder.poster);
    }

    // ------------------------- VIEW HOLDER -------------------------

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView poster;
        private TextView character;

        public ViewHolder(View view) {
            super(view);
            this.poster = (ImageView) view.findViewById(R.id.iv_movie_poster);
            this.character = (TextView) view.findViewById(R.id.tv__movie_character);
        }
    }
}