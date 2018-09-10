package com.arashpayan.prayerbook;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by arash on 7/5/15.
 */
public class EnabledCategoriesAdapter extends RecyclerView.Adapter {

    private ArrayList<CategoriesAdapter> mAdapters;
    private Context mContext;

    EnabledCategoriesAdapter(Context context, Language[] languages) {
        this.mContext = context;
        this.mAdapters = new ArrayList<>(languages.length);

        for (Language l : languages) {
            CategoriesAdapter ca = new CategoriesAdapter(l);
            this.mAdapters.add(ca);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == R.layout.list_header) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header, parent, false);
            return new CategoryHeaderHolder(itemView);
        } else {
            return mAdapters.get(0).onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
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

    void setListener(CategoriesAdapter.OnCategorySelectedListener l) {
        for (CategoriesAdapter a : mAdapters) {
            a.setListener(l);
        }
    }

    static class CategoryHeaderHolder extends RecyclerView.ViewHolder {
        protected TextView language;

        CategoryHeaderHolder(View itemView) {
            super(itemView);

            language = itemView.findViewById(R.id.header_language);
        }
    }
}
