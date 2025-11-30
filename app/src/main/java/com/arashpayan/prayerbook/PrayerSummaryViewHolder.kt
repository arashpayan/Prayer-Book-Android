package com.arashpayan.prayerbook

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

internal class PrayerSummaryViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val openingWords: TextView?
    val detail: TextView?
    val wordCount: TextView?

    init {
        openingWords = v.findViewById<TextView?>(R.id.prayer_summary)
        detail = v.findViewById<TextView?>(R.id.prayer_author)
        wordCount = v.findViewById<TextView?>(R.id.prayer_word_count)
    }
}
