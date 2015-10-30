package com.arashpayan.prayerbook;

import android.content.res.Resources;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SearchAdapter extends RecyclerView.Adapter<PrayerSummaryViewHolder> {

    private Cursor mCursor = null;

    public SearchAdapter() {
        setHasStableIds(true);
    }

    @Override
    public PrayerSummaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_summary, parent, false);

        return new PrayerSummaryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PrayerSummaryViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        int wordsColIdx = mCursor.getColumnIndexOrThrow(Database.OPENINGWORDS_COLUMN);
        holder.openingWords.setText(mCursor.getString(wordsColIdx));

        int ctgryColIdx = mCursor.getColumnIndexOrThrow(Database.CATEGORY_COLUMN);
        holder.detail.setText(mCursor.getString(ctgryColIdx));

        int wrdCntColIdx = mCursor.getColumnIndexOrThrow(Database.WORDCOUNT_COLUMN);
        int numWords = mCursor.getInt(wrdCntColIdx);
        final Resources resources = holder.detail.getContext().getResources();
        String wordCount = resources.getQuantityString(R.plurals.number_of_words, numWords, numWords);
        holder.wordCount.setText(wordCount);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }

        return mCursor.getCount();
    }

    public long getItemId(int position) {
        if (mCursor == null) {
            return RecyclerView.NO_ID;
        }
        mCursor.moveToPosition(position);
        int idColIdx = mCursor.getColumnIndexOrThrow(Database.ID_COLUMN);
        return mCursor.getLong(idColIdx);
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }
}

