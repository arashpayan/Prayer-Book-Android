package com.arashpayan.prayerbook;

import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class SearchAdapter extends RecyclerView.Adapter<PrayerSummaryViewHolder> {

    private Cursor mCursor = null;
    private OnPrayerSelectedListener mListener;

    SearchAdapter() {
        setHasStableIds(true);
    }

    @Override
    @NonNull
    public PrayerSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_summary, parent, false);
        final PrayerSummaryViewHolder holder = new PrayerSummaryViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener == null) {
                    return;
                }

                mListener.onPrayerSelected(getItemId(holder.getAdapterPosition()));
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PrayerSummaryViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        int wordsColIdx = mCursor.getColumnIndexOrThrow(DB.OPENINGWORDS_COLUMN);
        holder.openingWords.setText(mCursor.getString(wordsColIdx));

        int ctgryColIdx = mCursor.getColumnIndexOrThrow(DB.CATEGORY_COLUMN);
        holder.detail.setText(mCursor.getString(ctgryColIdx));

        int wrdCntColIdx = mCursor.getColumnIndexOrThrow(DB.WORDCOUNT_COLUMN);
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
        int idColIdx = mCursor.getColumnIndexOrThrow(DB.ID_COLUMN);
        return mCursor.getLong(idColIdx);
    }

    void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    void setListener(OnPrayerSelectedListener l) {
        mListener = l;
    }
}

