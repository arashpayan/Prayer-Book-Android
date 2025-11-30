package com.arashpayan.prayerbook

import android.annotation.SuppressLint
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arashpayan.prayerbook.database.PrayersDB

internal class SearchAdapter : RecyclerView.Adapter<PrayerSummaryViewHolder?>() {
    private var mCursor: Cursor? = null
    private var mListener: OnPrayerSelectedListener? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerSummaryViewHolder {
        val itemView =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_summary, parent, false)
        val holder = PrayerSummaryViewHolder(itemView)
        itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mListener == null) {
                    return
                }

                mListener!!.onPrayerSelected(getItemId(holder.getAbsoluteAdapterPosition()))
            }
        })

        return holder
    }

    override fun onBindViewHolder(holder: PrayerSummaryViewHolder, position: Int) {
        mCursor!!.moveToPosition(position)

        val wordsColIdx = mCursor!!.getColumnIndexOrThrow(PrayersDB.OPENINGWORDS_COLUMN)
        holder.openingWords?.setText(mCursor!!.getString(wordsColIdx))

        val ctgryColIdx = mCursor!!.getColumnIndexOrThrow(PrayersDB.CATEGORY_COLUMN)
        holder.detail?.setText(mCursor!!.getString(ctgryColIdx))

        val wrdCntColIdx = mCursor!!.getColumnIndexOrThrow(PrayersDB.WORDCOUNT_COLUMN)
        val numWords = mCursor!!.getInt(wrdCntColIdx)
        if (holder != null) {
            val resources = holder.detail?.getContext()?.getResources()
            if (resources != null) {
                val wordCount =
                    resources.getQuantityString(R.plurals.number_of_words, numWords, numWords)
                holder.wordCount?.setText(wordCount)
            }
        }
    }

    override fun getItemCount(): Int {
        if (mCursor == null) {
            return 0
        }

        return mCursor!!.getCount()
    }

    override fun getItemId(position: Int): Long {
        if (mCursor == null) {
            return RecyclerView.NO_ID
        }
        mCursor!!.moveToPosition(position)
        val idColIdx = mCursor!!.getColumnIndexOrThrow(PrayersDB.ID_COLUMN)
        return mCursor!!.getLong(idColIdx)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCursor(cursor: Cursor?) {
        this.mCursor = cursor
        notifyDataSetChanged()
    }

    fun setListener(l: OnPrayerSelectedListener?) {
        mListener = l
    }
}

