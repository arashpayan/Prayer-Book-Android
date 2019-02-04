package com.arashpayan.prayerbook;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * PrayerBook
 * Created by Arash Payan on 7/4/15.
 */
class CategoryViewHolder extends RecyclerView.ViewHolder {

    final TextView category;
    final TextView prayerCount;
    private final View categoryView;

    CategoryViewHolder(View itemView) {
        super(itemView);

        categoryView = itemView;
        category = itemView.findViewById(R.id.category_title);
        prayerCount = itemView.findViewById(R.id.category_prayers_count);
    }

    public void setLanguage(Language l) {
        categoryView.setLayoutDirection(l.rightToLeft ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    }
}
