package com.arashpayan.prayerbook;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * PrayerBook
 * Created by Arash Payan on 7/4/15.
 */
public class CategoryViewHolder extends RecyclerView.ViewHolder {

    protected final TextView category;
    protected final TextView prayerCount;
    private final View mCategoryView;
    private Language mLanguage;

    public CategoryViewHolder(View itemView) {
        super(itemView);

        mCategoryView = itemView;
        category = (TextView) itemView.findViewById(R.id.category_title);
        prayerCount = (TextView) itemView.findViewById(R.id.category_prayers_count);
    }

    public Language getLanguage() {
        return mLanguage;
    }

    public void setLanguage(Language l) {
        mLanguage = l;
        if (mLanguage != null && Build.VERSION.SDK_INT >= 17) {
            mCategoryView.setLayoutDirection(mLanguage.rightToLeft ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
    }
}
