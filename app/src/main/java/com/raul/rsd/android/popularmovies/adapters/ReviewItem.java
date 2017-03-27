package com.raul.rsd.android.popularmovies.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.raul.rsd.android.popularmovies.R;

import java.util.List;

import javax.inject.Inject;

public class ReviewItem extends AbstractItem<ReviewItem, ReviewItem.ViewHolder> {

    // ------------------------- ATTRIBUTES --------------------------

    public String author, content;

    // ------------------------- CONSTRUCTOR -------------------------

    @Inject
    ReviewItem() { }

    public ReviewItem withReview(String author, String content){
        this.author = author;
        this.content = content;
        return this;
    }

    @Override
    public int getLayoutRes() { return R.layout.movie_review_item; }

    // -------------------------- AUXILIARY --------------------------

    @Override
    public int getType() { return R.id.tv_review_content; }

    // -------------------------- USE CASES --------------------------

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.author.setText(author);
        viewHolder.content.setText(content);
    }

    // ------------------------- VIEW HOLDER -------------------------

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView author, content;

        public ViewHolder(View view) {
            super(view);
            this.author = (TextView) view.findViewById(R.id.tv_review_name);
            this.content = (TextView) view.findViewById(R.id.tv_review_content);
        }
    }
}