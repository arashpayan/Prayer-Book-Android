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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.arashpayan.util.Graphics;

/**
 *
 * @author arash
 */
public class CategoryPrayersFragment extends Fragment {
    
    private String category;
    private CategoryPrayersAdapter adapter;
    
//    public CategoryPrayersFragment() {
//        
//    }
    
    public CategoryPrayersFragment(String category) {
        super();
        
        this.category = category;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        Log.i(PrayerBook.TAG, "CategoryPrayersFragment.onAttach()");
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(false);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView list = new ListView(getActivity());
        adapter = new CategoryPrayersAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View itemView, int index, long itemId) {
                PrayerFragment prayerFragment = new PrayerFragment(itemId);
                
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.addToBackStack(null);
                ft.replace(PrayerBook.CONTENT_VIEW_ID, prayerFragment).commit();
            }
        });
        
        return list;
    }
    
    
    class CategoryPrayersAdapter extends BaseAdapter {
        
        private Database prayersDb;
        private Cursor prayersCursor;

        public CategoryPrayersAdapter() {
            prayersDb = Database.getInstance();
            prayersCursor = prayersDb.getPrayers(category);
        }
        
        public int getCount() {
            return prayersCursor.getCount();
        }

        public Object getItem(int position) {
            return "prayer: " + position;
        }

        public long getItemId(int position) {
            prayersCursor.moveToPosition(position);
            int idColumnIndex = prayersCursor.getColumnIndexOrThrow(Database.ID_COLUMN);
            long prayerId = prayersCursor.getLong(idColumnIndex);
            
            return prayerId;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = null;
            if (convertView != null)
                tv = (TextView)convertView;
            else
            {
                tv = new TextView(getActivity());
//                tv.setTextColor(Color.argb(255, 255, 255, 255));
                tv.setPadding(Graphics.pixels(tv.getContext(), 8), 0, 0, 0);
                tv.setMinimumHeight(Graphics.pixels(tv.getContext(), 48));
                tv.setGravity(Graphics.GRAVITY_CENTER_VERTICAL | Graphics.GRAVITY_LEFT);
                tv.setTextSize(15);
            }
            
            prayersCursor.moveToPosition(position);
            int openingWordsColumnIndex = prayersCursor.getColumnIndexOrThrow(Database.OPENINGWORDS_COLUMN);
            String openingWords = prayersCursor.getString(openingWordsColumnIndex);
            tv.setText(openingWords);
            
            return tv;
        }
    }
}
