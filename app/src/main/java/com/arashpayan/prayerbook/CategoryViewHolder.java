package com.arashpayan.prayerbook;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * PrayerBook
 * Created by Arash Payan on 7/4/15.
 */
public class CategoryViewHolder extends RecyclerView.ViewHolder {

    protected final TextView category;
    final TextView prayerCount;
    private final View mCategoryView;
    private Language mLanguage;

    CategoryViewHolder(View itemView) {
        super(itemView);

        mCategoryView = itemView;
        category = itemView.findViewById(R.id.category_title);
        prayerCount = itemView.findViewById(R.id.category_prayers_count);
    }

    public Language getLanguage() {
        return mLanguage;
    }

    public void setLanguage(Language l) {
        mLanguage = l;
        mCategoryView.setLayoutDirection(mLanguage.rightToLeft ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    }
}
