package com.arashpayan.prayerbook;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class PrayerSummaryViewHolder extends RecyclerView.ViewHolder {

    protected TextView openingWords;
    protected TextView detail;
    protected TextView wordCount;

    public PrayerSummaryViewHolder(View v) {
        super(v);

        openingWords = (TextView) v.findViewById(R.id.prayer_summary);
        detail = (TextView) v.findViewById(R.id.prayer_author);
        wordCount = (TextView) v.findViewById(R.id.prayer_word_count);
    }
}
