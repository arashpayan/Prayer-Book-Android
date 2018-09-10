package com.arashpayan.prayerbook;

import android.content.res.Resources;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class CategoryPrayersAdapter extends RecyclerView.Adapter<PrayerSummaryViewHolder> {

    private final Cursor mCursor;
    private final Language mLanguage;
    private OnPrayerSelectedListener mListener;

    CategoryPrayersAdapter(String category, Language language) {
        this.mCursor = DB.get().getPrayers(category, language);
        this.mLanguage = language;
        setHasStableIds(true);
    }

    @Override
    @NonNull
    public PrayerSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_summary, parent, false);
        itemView.setLayoutDirection(mLanguage.rightToLeft ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        final PrayerSummaryViewHolder holder = new PrayerSummaryViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener == null) {
                    return;
                }

                int pos = holder.getAdapterPosition();
                mListener.onPrayerSelected(getItemId(pos));
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PrayerSummaryViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        int wordsColIdx = mCursor.getColumnIndexOrThrow(DB.OPENINGWORDS_COLUMN);
        holder.openingWords.setText(mCursor.getString(wordsColIdx));

        int authorColIdx = mCursor.getColumnIndexOrThrow(DB.AUTHOR_COLUMN);
        String author = mCursor.getString(authorColIdx);
        holder.detail.setText(author);
        if (author == null || author.isEmpty()) {
            holder.detail.setVisibility(View.GONE);
        } else {
            holder.detail.setVisibility(View.VISIBLE);
        }

        int wordCountColIdx = mCursor.getColumnIndexOrThrow(DB.WORDCOUNT_COLUMN);
        int numWords = mCursor.getInt(wordCountColIdx);
        final Resources resources = holder.wordCount.getResources();
        String wordCount = resources.getQuantityString(R.plurals.number_of_words, numWords, numWords);
        holder.wordCount.setText(wordCount);
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
        int idColIdx = mCursor.getColumnIndexOrThrow(DB.ID_COLUMN);
        return mCursor.getLong(idColIdx);
    }

    void setListener(OnPrayerSelectedListener l) {
        mListener = l;
    }
}
