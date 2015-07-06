package com.arashpayan.prayerbook;

import android.database.Cursor;
import android.graphics.drawable.RippleDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by arash on 7/4/15.
 */
public class CategoriesAdapter extends RecyclerView.Adapter<CategoryViewHolder> {

    private final Cursor mCategoriesCursor;
    private final Language mLanguage;

    public CategoriesAdapter(Language language) {
        this.mLanguage = language;
        mCategoriesCursor = Database.getInstance().getCategories(language);
        setHasStableIds(false);
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category, parent, false);

        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        mCategoriesCursor.moveToPosition(position);

        int categoryColIdx = mCategoriesCursor.getColumnIndexOrThrow(Database.CATEGORY_COLUMN);
        String category = mCategoriesCursor.getString(categoryColIdx);
        holder.category.setText(category);

        int prayerCount = Database.getInstance().getPrayerCountForCategory(category, mLanguage.code);
        holder.prayerCount.setText(Integer.toString(prayerCount));
    }

    @Override
    public int getItemCount() {
        return mCategoriesCursor.getCount();
    }

    public String getCategory(int position) {
        mCategoriesCursor.moveToPosition(position);
        int categoryColIdx = mCategoriesCursor.getColumnIndexOrThrow(Database.CATEGORY_COLUMN);
        return mCategoriesCursor.getString(categoryColIdx);
    }

    public Language getLanguage() {
        return mLanguage;
    }
}
