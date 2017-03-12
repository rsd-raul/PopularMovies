package com.raul.rsd.android.popularmovies.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.raul.rsd.android.popularmovies.R;

import java.util.List;

import javax.inject.Inject;

public class ReviewItem extends AbstractItem<ReviewItem, ReviewItem.ViewHolder> {
    public String author;
    public String content;

    @Inject
    public ReviewItem() {
    }

    public ReviewItem withReview(String author, String content){
        this.author = author;
        this.content = content;
        return this;
    }

    @Override
    public int getType() { return R.id.tv_review_content; }

    @Override
    public int getLayoutRes() { return R.layout.movie_review_item; }

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.author.setText(author);
        viewHolder.content.setText(content);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView author;
        private TextView content;

        public ViewHolder(View view) {
            super(view);
            this.author = (TextView) view.findViewById(R.id.tv_review_name);
            this.content = (TextView) view.findViewById(R.id.tv_review_content);
        }
    }
}