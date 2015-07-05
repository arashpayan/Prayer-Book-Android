package com.arashpayan.prayerbook;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by arash on 7/4/15.
 */
public class CategoryViewHolder extends RecyclerView.ViewHolder {

    protected TextView category;
    protected TextView prayerCount;

    public CategoryViewHolder(View itemView) {
        super(itemView);

        category = (TextView) itemView.findViewById(R.id.category_title);
        prayerCount = (TextView) itemView.findViewById(R.id.category_prayers_count);
    }
}
