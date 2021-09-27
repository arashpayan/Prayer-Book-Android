package com.arashpayan.prayerbook;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;

import com.arashpayan.prayerbook.database.PrayerSummary;
import com.arashpayan.prayerbook.database.PrayersDB;
import com.arashpayan.prayerbook.thread.UiRunnable;
import com.arashpayan.prayerbook.thread.WorkerRunnable;

import java.util.ArrayList;

class CategoryPrayersAdapter extends RecyclerView.Adapter<PrayerSummaryViewHolder> {

    @NonNull private ArrayList<Long> prayerIds = new ArrayList<>();
    @NonNull private final Language language;
    @NonNull final private OnPrayerSelectedListener listener;

    CategoryPrayersAdapter(@NonNull Language language, @NonNull OnPrayerSelectedListener listener) {
        this.language = language;
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        return prayerIds.size();
    }

    public long getItemId(int position) {
        return prayerIds.get(position);
    }

    @Override
    @NonNull
    public PrayerSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_summary, parent, false);
        itemView.setLayoutDirection(language.rightToLeft ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        final PrayerSummaryViewHolder holder = new PrayerSummaryViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                listener.onPrayerSelected(getItemId(pos));
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PrayerSummaryViewHolder holder, int position) {
        holder.detail.setText(null);
        holder.openingWords.setText(null);
        holder.wordCount.setText(null);

        long id = prayerIds.get(position);
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
                        holder.detail.setText(summary.author);
                        String wordCount = holder.openingWords.getContext().getResources().
                                getQuantityString(R.plurals.number_of_words, summary.wordCount, summary.wordCount);
                        holder.wordCount.setText(wordCount);
                    }
                });
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @UiThread
    void setPrayerIds(@NonNull ArrayList<Long> prayerIds) {
        this.prayerIds = prayerIds;
        //notifyItemRangeInserted() doesn't work - no idea why
        notifyDataSetChanged();
    }
}
