package com.arashpayan.prayerbook;

import android.annotation.SuppressLint;
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

class RecentsAdapter extends RecyclerView.Adapter<PrayerSummaryViewHolder> {

    @NonNull private final OnPrayerSelectedListener listener;
    @NonNull private ArrayList<Long> recentIds = new ArrayList<>();

    RecentsAdapter(@NonNull OnPrayerSelectedListener l) {
        this.listener = l;
        setHasStableIds(true);
    }

    void clearAll() {
        int size = recentIds.size();
        recentIds.clear();
        notifyItemRangeRemoved(0, size);
    }

    @Override
    public int getItemCount() {
        return recentIds.size();
    }

    @Override
    public long getItemId(int position) {
        return recentIds.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull PrayerSummaryViewHolder holder, int position) {
        long id = recentIds.get(position);
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
                int pos = holder.getAbsoluteAdapterPosition();
                listener.onPrayerSelected(recentIds.get(pos));
            }
        });
        return holder;
    }

    void onPrayerAccessed(long prayerId) {
        // check if we already have this id
        int idx = recentIds.indexOf(prayerId);
        if (idx == -1) {
            recentIds.add(0, prayerId);
            notifyItemInserted(0);
            return;
        }

        recentIds.remove(idx);
        notifyItemRemoved(idx);
        recentIds.add(0, prayerId);
        notifyItemInserted(0);
    }

    @SuppressLint("NotifyDataSetChanged")
    @UiThread
    void setRecentIds(@NonNull ArrayList<Long> recentIds) {
        this.recentIds = recentIds;

        notifyDataSetChanged();
    }

}
