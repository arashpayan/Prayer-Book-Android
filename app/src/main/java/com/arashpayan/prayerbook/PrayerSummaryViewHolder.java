package com.arashpayan.prayerbook;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class PrayerSummaryViewHolder extends RecyclerView.ViewHolder {

    final TextView openingWords;
    final TextView detail;
    final TextView wordCount;

    PrayerSummaryViewHolder(@NonNull View v) {
        super(v);

        openingWords = v.findViewById(R.id.prayer_summary);
        detail = v.findViewById(R.id.prayer_author);
        wordCount = v.findViewById(R.id.prayer_word_count);
    }
}
