package com.arashpayan.prayerbook;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.arashpayan.util.L;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by arash on 7/5/15.
 */
public class EnabledCategoriesAdapter extends RecyclerView.Adapter {

    private ArrayList<CategoriesAdapter> mAdapters;
    private Language[] languages;

    public EnabledCategoriesAdapter(Language[] languages) {
        this.languages = languages;
        this.mAdapters = new ArrayList<CategoriesAdapter>(this.languages.length);

        for (Language l : this.languages) {
            CategoriesAdapter ca = new CategoriesAdapter(l);
            this.mAdapters.add(ca);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mAdapters.get(0).onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int previousSum = 0;
        int sum = 0;
        for (CategoriesAdapter ca : this.mAdapters) {
            sum += ca.getItemCount();
            if (position < sum) {
                ca.onBindViewHolder((CategoryViewHolder) holder, position - previousSum);
                return;
            }
            previousSum += ca.getItemCount();
        }
    }

    @Override
    public int getItemCount() {
        // all adapter counts + num adapters (for the header views)
        int count = 0;
        for (CategoriesAdapter ca : this.mAdapters) {
            count += ca.getItemCount();
        }
//        count += this.mAdapters.size();
        return count;
    }

    public String getCategory(int position) {
        int previousSum = 0;
        int sum = 0;
        for (CategoriesAdapter ca : mAdapters) {
            sum += ca.getItemCount();
            if (position < sum) {
                return ca.getCategory(position - previousSum);
            }
            previousSum += ca.getItemCount();
        }

        // never happens
        return null;
    }

    public Language getLanguage(int position) {
        int previousSum = 0;
        int sum = 0;
        for (CategoriesAdapter ca : mAdapters) {
            sum += ca.getItemCount();
            if (position < sum) {
                return ca.getLanguage();
            }
            previousSum += ca.getItemCount();
        }

        // never happens
        return null;
    }
}
