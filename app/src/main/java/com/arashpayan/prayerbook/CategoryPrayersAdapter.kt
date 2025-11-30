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

internal class CategoryPrayersAdapter(
    private val language: Language,
    private val listener: OnPrayerSelectedListener
) : RecyclerView.Adapter<PrayerSummaryViewHolder?>() {
    private var prayerIds = ArrayList<Long?>()

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int {
        return prayerIds.size
    }

    override fun getItemId(position: Int): Long {
        return prayerIds.get(position)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerSummaryViewHolder {
        val itemView =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_summary, parent, false)
        itemView.setLayoutDirection(if (language.rightToLeft) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR)
        val holder = PrayerSummaryViewHolder(itemView)
        itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val pos = holder.getAbsoluteAdapterPosition()
                listener.onPrayerSelected(getItemId(pos))
            }
        })

        return holder
    }

    override fun onBindViewHolder(holder: PrayerSummaryViewHolder, position: Int) {
        holder.detail?.setText(null)
        holder.openingWords?.setText(null)
        holder.wordCount?.setText(null)

        val id = prayerIds.get(position)!!
        runInBackground(object : WorkerRunnable {
            override fun run() {
                val summary = get().getPrayerSummary(id)
                if (summary == null) {
                    return
                }
                runOnUiThread(object : UiRunnable {
                    override fun run() {
                        holder.openingWords?.setText(summary.openingWords)
                        holder.detail?.setText(summary.author)
                        val wordCount =
                            holder.openingWords?.getContext()?.getResources()
                                ?.getQuantityString(R.plurals.number_of_words, summary.wordCount, summary.wordCount)
                        holder.wordCount?.setText(wordCount)
                    }
                })
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    @UiThread
    fun setPrayerIds(prayerIds: ArrayList<Long?>) {
        this.prayerIds = prayerIds
        //notifyItemRangeInserted() doesn't work - no idea why
        notifyDataSetChanged()
    }
}
