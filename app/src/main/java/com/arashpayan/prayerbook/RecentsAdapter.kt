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

internal class RecentsAdapter(private val listener: OnPrayerSelectedListener) :
    RecyclerView.Adapter<PrayerSummaryViewHolder?>() {
    private var recentIds = ArrayList<Long?>()

    init {
        setHasStableIds(true)
    }

    fun clearAll() {
        val size = recentIds.size
        recentIds.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun getItemCount(): Int {
        return recentIds.size
    }

    override fun getItemId(position: Int): Long {
        return recentIds.get(position)!!
    }

    override fun onBindViewHolder(holder: PrayerSummaryViewHolder, position: Int) {
        val id = recentIds.get(position)!!
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
                            holder.openingWords?.getContext()?.getResources()
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
                listener.onPrayerSelected(recentIds.get(pos)!!)
            }
        })
        return holder
    }

    fun onPrayerAccessed(prayerId: Long) {
        // check if we already have this id
        val idx = recentIds.indexOf(prayerId)
        if (idx == -1) {
            recentIds.add(0, prayerId)
            notifyItemInserted(0)
            return
        }

        recentIds.removeAt(idx)
        notifyItemRemoved(idx)
        recentIds.add(0, prayerId)
        notifyItemInserted(0)
    }

    @SuppressLint("NotifyDataSetChanged")
    @UiThread
    fun setRecentIds(recentIds: ArrayList<Long?>) {
        this.recentIds = recentIds

        notifyDataSetChanged()
    }
}
