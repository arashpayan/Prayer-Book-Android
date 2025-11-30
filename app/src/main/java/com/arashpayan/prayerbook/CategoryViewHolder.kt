package com.arashpayan.prayerbook

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

internal class CategoryViewHolder(private val categoryView: View) : RecyclerView.ViewHolder(
    categoryView
) {
    val category: TextView?
    val prayerCount: TextView?

    init {
        category = categoryView.findViewById<TextView?>(R.id.category_title)
        prayerCount = categoryView.findViewById<TextView?>(R.id.category_prayers_count)
    }

    fun setLanguage(l: Language) {
        categoryView.setLayoutDirection(if (l.rightToLeft) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR)
    }
}
