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

public class VideoItem extends AbstractItem<VideoItem, VideoItem.ViewHolder> {

    // ------------------------- ATTRIBUTES --------------------------

    public String key;
    private String name;
    private Context context;

    // ------------------------- CONSTRUCTOR -------------------------

    @Inject
    public VideoItem(Context context) {
        this.context = context;
    }

    public VideoItem withVideo(String name, String key){
        this.name = name;
        this.key = key;
        return this;
    }

    @Override
    public int getLayoutRes() { return R.layout.movie_video_item; }

    // -------------------------- AUXILIARY --------------------------

    @Override
    public int getType() { return R.id.iv_movie_poster; }

    // -------------------------- USE CASES --------------------------

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.name.setText(name);
        Picasso.with(context)
                .load(NetworkUtils.buildYoutubeThumbnailUri(key))
                .placeholder(R.drawable.placeholder_backdrop)
                .into(viewHolder.video);
    }

    // ------------------------- VIEW HOLDER -------------------------

    static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        private ImageView video;

        public ViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.tv_video_name);
            this.video = (ImageView) view.findViewById(R.id.iv_movie_video);
        }
    }
}