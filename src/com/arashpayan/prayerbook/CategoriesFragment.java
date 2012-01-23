/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings.TextSize;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.arashpayan.util.Graphics;

/**
 *
 * @author arash
 */
public class CategoriesFragment extends Fragment {
    
    private CategoriesAdapter categoriesAdapter;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
            
        Log.i(PrayerBook.TAG, "CategoriesFragment.onAttach()");
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView list = new ListView(getActivity());
        categoriesAdapter = new CategoriesAdapter();
        list.setAdapter(categoriesAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View itemView, int index, long itemId) {
                Log.i(PrayerBook.TAG, "You tapped item " + index);
                
                CategoryPrayersFragment categoryPrayersFragment = new CategoryPrayersFragment((String)categoriesAdapter.getItem(index));
                
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.addToBackStack(null);
                ft.replace(PrayerBook.CONTENT_VIEW_ID, categoryPrayersFragment).commit();
            }
        });
        
        return list;
    }
    
    class CategoryView extends RelativeLayout {
        private TextView categoryTextView;
        private TextView prayerCountTextView;
        
        private static final int CATEGORY_TEXTVIEW_ID       = 38;
        private static final int PRAYER_COUNT_TEXTVIEW_ID   = 32;
        
        public CategoryView(Context context) {
            super(context);
            
            setMinimumHeight(Graphics.pixels(context, 48));
            
            categoryTextView = new TextView(context);
            categoryTextView.setTextSize(17 * getResources().getConfiguration().fontScale);
            categoryTextView.setPadding(Graphics.pixels(context, 8), Graphics.pixels(context, 8), Graphics.pixels(context, 8), Graphics.pixels(context, 8));
            categoryTextView.setTypeface(Typeface.DEFAULT_BOLD);
            categoryTextView.setId(CATEGORY_TEXTVIEW_ID);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            params.addRule(ALIGN_PARENT_LEFT, NO_ID);
            params.addRule(RelativeLayout.CENTER_VERTICAL, CATEGORY_TEXTVIEW_ID);
            categoryTextView.setLayoutParams(params);
            addView(categoryTextView);
            
            prayerCountTextView = new TextView(context);
            prayerCountTextView.setTextSize(17 * getResources().getConfiguration().fontScale);
            prayerCountTextView.setPadding(Graphics.pixels(context, 8), Graphics.pixels(context, 8), Graphics.pixels(context, 16), Graphics.pixels(context, 8));
            prayerCountTextView.setId(PRAYER_COUNT_TEXTVIEW_ID);
            params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            params.addRule(ALIGN_PARENT_RIGHT, NO_ID);
            params.addRule(CENTER_VERTICAL, PRAYER_COUNT_TEXTVIEW_ID);
            prayerCountTextView.setLayoutParams(params);
            addView(prayerCountTextView);
        }
        
        public CharSequence getCategory() {
            return categoryTextView.getText();
        }
        
        public void setCategory(CharSequence aCategory) {
            categoryTextView.setText(aCategory);
        }
        
        public CharSequence getPrayerCount() {
            return (String)prayerCountTextView.getText();
        }
        
        public void setPrayerCount(CharSequence aPrayerCount) {
            prayerCountTextView.setText(aPrayerCount);
        }
    }
    
    class CategoriesAdapter extends BaseAdapter {
        
        private Database prayersDb;
        private Cursor categoriesCursor;
        
        public CategoriesAdapter() {
            prayersDb = Database.getInstance();
            categoriesCursor = prayersDb.getCategories("en");
        }
        
        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }
        
        public boolean isEnabled() {
            return true;
        }
        
        public int getCount() {
            return categoriesCursor.getCount();
        }
        
        public Object getItem(int position) {
            categoriesCursor.moveToPosition(position);
            int categoryColumnIndex = categoriesCursor.getColumnIndexOrThrow(Database.CATEGORY_COLUMN);
            return categoriesCursor.getString(categoryColumnIndex);
        }
        
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            CategoryView categoryView = null;
            if (convertView != null)
                categoryView = (CategoryView)convertView;
            else
                categoryView = new CategoryView(getActivity());
            
            categoriesCursor.moveToPosition(position);
            int categoryColumnIndex = categoriesCursor.getColumnIndexOrThrow(Database.CATEGORY_COLUMN);
            String category = categoriesCursor.getString(categoryColumnIndex);
            categoryView.setCategory(category);
            
            int prayerCount = Database.getInstance().getPrayerCountForCategory(category, "en");
            categoryView.setPrayerCount(Integer.toString(prayerCount));
            
            return categoryView;
        }
    }
}
