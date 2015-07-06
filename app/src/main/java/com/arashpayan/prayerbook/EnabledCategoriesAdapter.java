package com.arashpayan.prayerbook;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arashpayan.util.L;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by arash on 7/5/15.
 */
public class EnabledCategoriesAdapter extends RecyclerView.Adapter {

    private ArrayList<CategoriesAdapter> mAdapters;
    private Language[] languages;
    private Context mContext;

    public EnabledCategoriesAdapter(Context context, Language[] languages) {
        this.mContext = context;
        this.languages = languages;
        this.mAdapters = new ArrayList<>(this.languages.length);

        for (Language l : this.languages) {
            CategoriesAdapter ca = new CategoriesAdapter(l);
            this.mAdapters.add(ca);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == R.layout.list_header) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header, parent, false);
            return new CategoryHeaderHolder(itemView);
        } else {
            return mAdapters.get(0).onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int previousSum = 0;
        int sum = 0;
        for (CategoriesAdapter ca : this.mAdapters) {
            if (position == sum) {
                ((CategoryHeaderHolder) holder).language.setText(mContext.getText(ca.getLanguage().humanName));
                return;
            }
            sum += ca.getItemCount() + 1;
            previousSum += 1;
            if (position < sum) {
                ca.onBindViewHolder((CategoryViewHolder) holder, position - previousSum);
                return;
            }
            previousSum += ca.getItemCount();
        }
    }

    @Override
    public int getItemCount() {
        // all adapter counts
        int count = 0;
        for (CategoriesAdapter ca : this.mAdapters) {
            count += ca.getItemCount();
        }

        // plus the headers (one for each adapter)
        count += this.mAdapters.size();
        return count;
    }

    public int getItemViewType (int position) {
        int sum = 0;
        for (CategoriesAdapter ca : mAdapters) {
            if (position == sum) {
                return R.layout.list_header;
            }
            sum += ca.getItemCount() + 1;
            if (position < sum) {
                return R.layout.category;
            }
        }

        throw new IllegalArgumentException("Invalid position passed to getItemViewType(" + position + ")");
    }

    public String getCategory(int position) {
        int previousSum = 0;
        int sum = 0;
        for (CategoriesAdapter ca : mAdapters) {
            if (position == sum) {
                // this is a header
                throw new IllegalArgumentException("This position is a header view, not a category (" + position + ")");
            }
            sum += ca.getItemCount() + 1;
            previousSum += 1;
            if (position < sum) {
                return ca.getCategory(position - previousSum);
            }
            previousSum += ca.getItemCount();
        }

        // never happens
        throw new IllegalArgumentException("Invalid position passed to getCategory(" + position + ")");
    }

    public Language getLanguage(int position) {
        int sum = 0;
        for (CategoriesAdapter ca : mAdapters) {
            if (position == sum) {
                throw new IllegalArgumentException("This position is a header view, not a category (" + position + ")");
            }
            sum += ca.getItemCount() + 1;
            if (position < sum) {
                return ca.getLanguage();
            }
        }

        // never happens
        return null;
    }

    public boolean isHeader(int position) {
        int sum = 0;
        for (CategoriesAdapter ca : mAdapters) {
            if (position == sum) {
                return true;
            }
            sum += ca.getItemCount() + 1;
        }

        return false;
    }

    static class CategoryHeaderHolder extends RecyclerView.ViewHolder {
        protected TextView language;

        public CategoryHeaderHolder(View itemView) {
            super(itemView);

            language = (TextView) itemView.findViewById(R.id.header_language);
        }
    }
}
