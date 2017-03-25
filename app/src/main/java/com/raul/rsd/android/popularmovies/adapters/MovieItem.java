package com.raul.rsd.android.popularmovies.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.raul.rsd.android.popularmovies.R;
import com.raul.rsd.android.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.inject.Inject;

public class MovieItem extends AbstractItem<MovieItem, MovieItem.ViewHolder> implements Parcelable {

    // ------------------------- ATTRIBUTES --------------------------

    public long id;
    private String poster_path;
    private Bitmap poster;
    private Context context;

    // ------------------------- CONSTRUCTOR -------------------------

    @Inject
    MovieItem(Context context) {
        this.context = context;
    }

    public MovieItem withMovie(long id, String poster_path, Bitmap poster){
        this.poster_path = poster_path;
        this.poster = poster;
        this.id = id;
        return this;
    }

    @Override
    public int getLayoutRes() { return R.layout.movie_list_item; }

    // -------------------------- AUXILIARY --------------------------

    @Override
    public int getType() { return R.id.iv_movie_poster; }

    // -------------------------- USE CASES --------------------------

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        if(poster_path != null)
            Picasso.with(context)
                    .load(NetworkUtils.buildMoviePosterUri(poster_path))
                    .placeholder(R.drawable.placeholder_poster)
                    .into(viewHolder.poster);
        else
            viewHolder.poster.setImageBitmap(poster);
    }

    // ------------------------- VIEW HOLDER -------------------------

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView poster;

        public ViewHolder(View view) {
            super(view);
            this.poster = (ImageView) view.findViewById(R.id.iv_movie_poster);
        }
    }

    // ------------------------- PARCELABLE --------------------------

    static final Creator<MovieItem> CREATOR = new Creator<MovieItem> () {
        @Override
        public MovieItem createFromParcel(Parcel in) { return new MovieItem(); }

        @Override
        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };

    private MovieItem() { }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) { }
}