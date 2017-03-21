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

public class ActorItem extends AbstractItem<ActorItem, ActorItem.ViewHolder> {
    public long id;
    private String profile_path, name, character;
    private Context context;

    @Inject
    public ActorItem(Context context) {
        this.context = context;
    }

    public ActorItem withActor(long id, String profile_path, String name, String character){
        this.id = id;
        this.profile_path = profile_path;
        this.name = name;
        this.character = character;
        return this;
    }

    @Override
    public int getType() { return R.id.iv_actor_profile; }

    @Override
    public int getLayoutRes() { return R.layout.movie_actor_item; }

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.name.setText(name);
        viewHolder.character.setText(character);
        Picasso.with(context)
                .load(NetworkUtils.buildActorProfileUri(profile_path))
                .placeholder(R.drawable.placeholder_backdrop)
                .into(viewHolder.profile);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView profile;
        private TextView name;
        private TextView character;

        public ViewHolder(View view) {
            super(view);
            this.profile = (ImageView) view.findViewById(R.id.iv_actor_profile);
            this.name = (TextView) view.findViewById(R.id.tv_actor_name);
            this.character = (TextView) view.findViewById(R.id.tv_actor_character);
        }
    }
}