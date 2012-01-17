/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
            TextView tv = null;
            if (convertView != null)
                tv = (TextView)convertView;
            else
            {
                tv = new TextView(getActivity());
                tv.setTextColor(Color.argb(255, 255, 255, 255));
                tv.setPadding(Graphics.pixels(tv.getContext(), 8), 0, 0, 0);
                tv.setMinimumHeight(Graphics.pixels(tv.getContext(), 48));
                tv.setGravity(Graphics.GRAVITY_CENTER_VERTICAL | Graphics.GRAVITY_LEFT);
                tv.setTextSize(17);
            }
            categoriesCursor.moveToPosition(position);
            int categoryColumnIndex = categoriesCursor.getColumnIndexOrThrow(Database.CATEGORY_COLUMN);
            String category = categoriesCursor.getString(categoryColumnIndex);
            tv.setText(category);
            
            return tv;
        }
    }
}
