package com.arashpayan.prayerbook;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by arash on 7/5/15.
 */
public class CategoryPrayersAdapter extends RecyclerView.Adapter<PrayerSummaryViewHolder> {

    private final Cursor mCursor;

    public CategoryPrayersAdapter(String category, Language language) {
        this.mCursor = Database.getInstance().getPrayers(category, language);
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

        int authorColIdx = mCursor.getColumnIndexOrThrow(Database.AUTHOR_COLUMN);
        holder.detail.setText(mCursor.getString(authorColIdx));

        int wrdCntColIdx = mCursor.getColumnIndexOrThrow(Database.WORDCOUNT_COLUMN);
        holder.wordCount.setText(mCursor.getString(wrdCntColIdx) + " " + holder.wordCount.getContext().getString(R.string.words));
    }

    @Override
    public int getItemCount() {
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
}
