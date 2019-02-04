package com.arashpayan.prayerbook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arashpayan.prayerbook.database.PrayerSummary;
import com.arashpayan.prayerbook.database.PrayersDB;
import com.arashpayan.prayerbook.thread.UiRunnable;
import com.arashpayan.prayerbook.thread.WorkerRunnable;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;

class BookmarksAdapter extends RecyclerView.Adapter<PrayerSummaryViewHolder> {

    @NonNull private final OnPrayerSelectedListener listener;
    @NonNull private ArrayList<Long> bookmarks = new ArrayList<>();

    BookmarksAdapter(@NonNull OnPrayerSelectedListener l) {
        this.listener = l;
        setHasStableIds(true);
    }

    @UiThread
    void bookmarkAdded(long prayerId) {
        bookmarks.add(prayerId);
        notifyItemInserted(bookmarks.size()-1);
    }

    @UiThread
    void bookmarkDeleted(long prayerId) {
        int idx = bookmarks.indexOf(prayerId);
        if (idx != -1) {
            bookmarks.remove(idx);
            notifyItemRemoved(idx);
        }
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    @Override
    public long getItemId(int position) {
        return bookmarks.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull PrayerSummaryViewHolder holder, int position) {
        long id = bookmarks.get(position);
        holder.detail.setText(null);
        holder.openingWords.setText(null);
        holder.wordCount.setText(null);

        App.runInBackground(new WorkerRunnable() {
            @Override
            public void run() {
                PrayerSummary summary = PrayersDB.get().getPrayerSummary(id);
                if (summary == null) {
                    return;
                }
                App.runOnUiThread(new UiRunnable() {
                    @Override
                    public void run() {
                        holder.openingWords.setText(summary.openingWords);
                        holder.detail.setText(summary.category);
                        String wordCount = holder.openingWords.getContext().getResources().
                                getQuantityString(R.plurals.number_of_words, summary.wordCount, summary.wordCount);
                        holder.wordCount.setText(wordCount);
                    }
                });
            }
        });
    }

    @NonNull
    @Override
    public PrayerSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_summary, parent, false);
        final PrayerSummaryViewHolder holder = new PrayerSummaryViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                listener.onPrayerSelected(bookmarks.get(pos));
            }
        });
        return holder;
    }

    @UiThread
    void setBookmarks(@NonNull ArrayList<Long> bookmarks) {
        this.bookmarks = bookmarks;

        notifyDataSetChanged();
    }
}
