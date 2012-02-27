/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
    
    public CategoryPrayersFragment() {}
    
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
        
        setHasOptionsMenu(true);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        getActivity().getActionBar().setTitle(category);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setHomeButtonEnabled(true);
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
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home)
            getFragmentManager().popBackStack();
        
        return true;
    }
    
    class PrayerItemView extends RelativeLayout {
        private TextView titleTextView;
        private TextView authorTextView;
        private TextView wordCountTextView;
        
        private static final int TITLE_TEXTVIEW_ID      = 29;
        private static final int AUTHOR_TEXTVIEW_ID     = 48;
        private static final int WORDCOUNT_TEXTVIEW_ID  = 82;
        
        public PrayerItemView(Context context) {
            super(context);
            
            titleTextView = new TextView(context);
            titleTextView.setTextSize(16 * getResources().getConfiguration().fontScale);
            titleTextView.setPadding(Graphics.pixels(context, 8), Graphics.pixels(context, 8), Graphics.pixels(context, 8), Graphics.pixels(context, 2));
            titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
            titleTextView.setLines(1);
            titleTextView.setId(TITLE_TEXTVIEW_ID);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(ALIGN_PARENT_LEFT);
            params.addRule(ALIGN_PARENT_TOP);
            params.addRule(LEFT_OF, WORDCOUNT_TEXTVIEW_ID);
            titleTextView.setLayoutParams(params);
            addView(titleTextView);
            
            authorTextView = new TextView(context);
            authorTextView.setTextSize(14 * getResources().getConfiguration().fontScale);
            authorTextView.setPadding(Graphics.pixels(context, 8), 0, Graphics.pixels(context, 8), Graphics.pixels(context, 8));
            authorTextView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            authorTextView.setTextColor(Color.rgb(128, 128, 128));
            authorTextView.setId(AUTHOR_TEXTVIEW_ID);
            params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(ALIGN_PARENT_LEFT);
            params.addRule(BELOW, TITLE_TEXTVIEW_ID);
            authorTextView.setLayoutParams(params);
            addView(authorTextView);
            
            wordCountTextView = new TextView(context);
            wordCountTextView.setTextSize(14 * getResources().getConfiguration().fontScale);
            wordCountTextView.setPadding(Graphics.pixels(context, 0), Graphics.pixels(context, 0), Graphics.pixels(context, 8), Graphics.pixels(context, 8));
            wordCountTextView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            wordCountTextView.setTextColor(Color.rgb(0, 0, 255));
            wordCountTextView.setId(WORDCOUNT_TEXTVIEW_ID);
            params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            params.addRule(ALIGN_PARENT_RIGHT);
            params.addRule(ALIGN_TOP, AUTHOR_TEXTVIEW_ID);
            wordCountTextView.setLayoutParams(params);
            addView(wordCountTextView);
        }
        
        public CharSequence getTitle() {
            return titleTextView.getText();
        }
        
        public void setTitle(CharSequence title) {
            titleTextView.setText(title);
        }
        
        public CharSequence getAuthor() {
            return authorTextView.getText();
        }
        
        public void setAuthor(CharSequence author) {
            authorTextView.setText(author);
        }
        
        public CharSequence getWordCount() {
            return wordCountTextView.getText();
        }
        
        public void setWordCount(CharSequence wordCount) {
            wordCountTextView.setText(wordCount);
        }
            
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
            PrayerItemView piv = null;
            if (convertView != null)
                piv = (PrayerItemView)convertView;
            else
                piv = new PrayerItemView(getActivity());
            
            prayersCursor.moveToPosition(position);
            
            int openingWordsColumnIndex = prayersCursor.getColumnIndexOrThrow(Database.OPENINGWORDS_COLUMN);
            String openingWords = prayersCursor.getString(openingWordsColumnIndex);
            piv.setTitle(openingWords);
            
            int authorColumnIndex = prayersCursor.getColumnIndexOrThrow(Database.AUTHOR_COLUMN);
            String author = prayersCursor.getString(authorColumnIndex);
            piv.setAuthor(author);
            
            int wordCountColumnIndex = prayersCursor.getColumnIndexOrThrow(Database.WORDCOUNT_COLUMN);
            String wordCount = prayersCursor.getString(wordCountColumnIndex);
            piv.setWordCount("~" + wordCount + " " + getResources().getString(R.string.words));
            
            return piv;
            
//            TextView tv = null;
//            if (convertView != null)
//                tv = (TextView)convertView;
//            else
//            {
//                tv = new TextView(getActivity());
////                tv.setTextColor(Color.argb(255, 255, 255, 255));
//                tv.setPadding(Graphics.pixels(tv.getContext(), 8), 0, 0, 0);
//                tv.setMinimumHeight(Graphics.pixels(tv.getContext(), 48));
//                tv.setGravity(Graphics.GRAVITY_CENTER_VERTICAL | Graphics.GRAVITY_LEFT);
//                tv.setTextSize(15);
//            }
//            
//            prayersCursor.moveToPosition(position);
//            int openingWordsColumnIndex = prayersCursor.getColumnIndexOrThrow(Database.OPENINGWORDS_COLUMN);
//            String openingWords = prayersCursor.getString(openingWordsColumnIndex);
//            tv.setText(openingWords);
//            
//            return tv;
        }
    }
}
