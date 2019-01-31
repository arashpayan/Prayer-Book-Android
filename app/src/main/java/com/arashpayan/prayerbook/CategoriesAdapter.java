package com.arashpayan.prayerbook;

import android.content.Context;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arashpayan.prayerbook.database.PrayersDB;
import com.arashpayan.prayerbook.thread.UiRunnable;
import com.arashpayan.prayerbook.thread.WorkerRunnable;

import java.util.ArrayList;

/**
 * Created by arash on 7/5/15.
 */
public class CategoriesAdapter extends RecyclerView.Adapter {

    @NonNull private final ArrayList<CategoryItem> items = new ArrayList<>();
    @NonNull private final OnCategorySelectedListener listener;

    @WorkerThread
    CategoriesAdapter(@NonNull Language[] languages, @NonNull OnCategorySelectedListener listener) {
        this.listener = listener;

        for (Language l : languages) {
            items.add(new CategoryItem(R.layout.list_header, l, null));
            ArrayList<String> categories = PrayersDB.get().getCategories(l);
            for (String c : categories) {
                items.add(new CategoryItem(R.layout.category, l, c));
            }
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        if (viewType == R.layout.list_header) {
            return new CategoryHeaderHolder(itemView);
        } else if (viewType == R.layout.category) {
            CategoryViewHolder holder = new CategoryViewHolder(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getAdapterPosition();
                    CategoryItem item = items.get(pos);
                    listener.onCategorySelected(item.text, item.language);
                }
            });
            return holder;
        }

        throw new RuntimeException("Unknown viewtype");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CategoryItem item = items.get(position);
        Context ctx = holder.itemView.getContext();
        if (item.viewType == R.layout.list_header) {
            ((CategoryHeaderHolder) holder).language.setText(ctx.getString(item.language.humanName));
        } else if (item.viewType == R.layout.category) {
            CategoryViewHolder h = (CategoryViewHolder) holder;
            h.category.setText(item.text);
            h.setLanguage(item.language);
            h.prayerCount.setText(null);

            App.runInBackground(new WorkerRunnable() {
                @Override
                public void run() {
                    int count = PrayersDB.get().getPrayerCountForCategory(item.text, item.language.code);
                    App.runOnUiThread(new UiRunnable() {
                        @Override
                        public void run() {
                            h.prayerCount.setText(String.format(item.language.locale, "%d", count));
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public int getItemViewType (int position) {
        CategoryItem item = items.get(position);
        return item.viewType;
    }

    static class CategoryHeaderHolder extends RecyclerView.ViewHolder {
        protected TextView language;

        CategoryHeaderHolder(View itemView) {
            super(itemView);

            language = itemView.findViewById(R.id.header_language);
        }
    }

    static class CategoryItem {
        @LayoutRes int viewType;
        @NonNull Language language;
        String text;

        CategoryItem(@LayoutRes int viewType, @NonNull Language lang, @Nullable String text) {
            this.viewType = viewType;
            this.language = lang;
            this.text = text;
        }
    }

    interface OnCategorySelectedListener {
        @UiThread
        void onCategorySelected(String category, Language language);
    }
}
