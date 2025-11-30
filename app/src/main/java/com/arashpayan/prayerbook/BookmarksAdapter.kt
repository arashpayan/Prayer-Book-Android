package com.arashpayan.prayerbook

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.recyclerview.widget.RecyclerView
import com.arashpayan.prayerbook.App.Companion.runInBackground
import com.arashpayan.prayerbook.App.Companion.runOnUiThread
import com.arashpayan.prayerbook.database.PrayersDB.Companion.get
import com.arashpayan.prayerbook.thread.UiRunnable
import com.arashpayan.prayerbook.thread.WorkerRunnable

internal class BookmarksAdapter(private val listener: OnPrayerSelectedListener) :
    RecyclerView.Adapter<PrayerSummaryViewHolder?>() {
    private var bookmarks = ArrayList<Long?>()

    init {
        setHasStableIds(true)
    }

    @UiThread
    fun bookmarkAdded(prayerId: Long) {
        bookmarks.add(prayerId)
        notifyItemInserted(bookmarks.size - 1)
    }

    @UiThread
    fun bookmarkDeleted(prayerId: Long) {
        val idx = bookmarks.indexOf(prayerId)
        if (idx != -1) {
            bookmarks.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    override fun getItemCount(): Int {
        return bookmarks.size
    }

    override fun getItemId(position: Int): Long {
        return bookmarks.get(position)!!
    }

    override fun onBindViewHolder(holder: PrayerSummaryViewHolder, position: Int) {
        val id = bookmarks.get(position)!!
        holder.detail?.setText(null)
        holder.openingWords?.setText(null)
        holder.wordCount?.setText(null)

        runInBackground(object : WorkerRunnable {
            override fun run() {
                val summary = get().getPrayerSummary(id)
                if (summary == null) {
                    return
                }
                runOnUiThread(object : UiRunnable {
                    override fun run() {
                        holder.openingWords?.setText(summary.openingWords)
                        holder.detail?.setText(summary.category)
                        val wordCount =
                            holder.openingWords
                                ?.getContext()
                                ?.getResources()
                                ?.getQuantityString(R.plurals.number_of_words, summary.wordCount, summary.wordCount)
                        holder.wordCount?.setText(wordCount)
                    }
                })
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerSummaryViewHolder {
        val itemView =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_summary, parent, false)
        val holder = PrayerSummaryViewHolder(itemView)
        itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val pos = holder.getAbsoluteAdapterPosition()
                listener.onPrayerSelected(bookmarks.get(pos)!!)
            }
        })
        return holder
    }

    @SuppressLint("NotifyDataSetChanged")
    @UiThread
    fun setBookmarks(bookmarks: ArrayList<Long?>) {
        this.bookmarks = bookmarks

        notifyDataSetChanged()
    }
}
