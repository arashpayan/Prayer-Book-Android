package com.arashpayan.prayerbook;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class CategoriesAdapter extends RecyclerView.Adapter<CategoryViewHolder> {

    private final Cursor mCategoriesCursor;
    @NonNull private final Language mLanguage;
    private OnCategorySelectedListener mListener;

    CategoriesAdapter(@NonNull Language language) {
        this.mLanguage = language;
        mCategoriesCursor = DB.get().getCategories(language);
        setHasStableIds(false);
    }

    @Override
    @NonNull
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category, parent, false);
        final CategoryViewHolder holder = new CategoryViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener == null) {
                    return;
                }

                mListener.onCategorySelected(holder.category.getText().toString(), holder.getLanguage());
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        mCategoriesCursor.moveToPosition(position);

        int categoryColIdx = mCategoriesCursor.getColumnIndexOrThrow(DB.CATEGORY_COLUMN);
        String category = mCategoriesCursor.getString(categoryColIdx);
        holder.category.setText(category);
        holder.setLanguage(mLanguage);

        int prayerCount = DB.get().getPrayerCountForCategory(category, mLanguage.code);
        holder.prayerCount.setText(String.format(mLanguage.locale, "%d", prayerCount));
    }

    @Override
    public int getItemCount() {
        return mCategoriesCursor.getCount();
    }

    public Language getLanguage() {
        return mLanguage;
    }

    void setListener(OnCategorySelectedListener l) {
        mListener = l;
    }

    interface OnCategorySelectedListener {
        void onCategorySelected(String category, Language language);
    }
}
