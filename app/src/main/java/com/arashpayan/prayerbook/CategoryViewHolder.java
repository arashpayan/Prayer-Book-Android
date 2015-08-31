package com.arashpayan.prayerbook;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.arashpayan.util.L;

/**
 * Created by arash on 7/4/15.
 */
public class CategoryViewHolder extends RecyclerView.ViewHolder {

    protected TextView category;
    protected TextView prayerCount;
    private View mCategoryView;

    public CategoryViewHolder(View itemView) {
        super(itemView);

        mCategoryView = itemView;
        category = (TextView) itemView.findViewById(R.id.category_title);
        prayerCount = (TextView) itemView.findViewById(R.id.category_prayers_count);
    }

    public void setLayoutDirection(int layoutDirection) {
        if (Build.VERSION.SDK_INT >= 17) {
            mCategoryView.setLayoutDirection(layoutDirection);
        }
    }
}
