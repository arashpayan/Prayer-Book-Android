/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 *
 * @author arash
 */
public class CategoryPrayersFragment extends Fragment {
    
    public static final String CATEGORYPRAYERS_TAG = "CategoryPrayers";
    public static final String CATEGORY_ARGUMENT = "Category";
    public static final String LANGUAGE_ARGUMENT = "Language";
    private String mCategory;
    private Language mLanguage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mCategory = getArguments().getString(CATEGORY_ARGUMENT, null);
        mLanguage = getArguments().getParcelable(LANGUAGE_ARGUMENT);
        if (mCategory == null) {
            throw new IllegalArgumentException("You must provide a category");
        }
        if (mLanguage == null) {
            throw new IllegalArgumentException("You must provide a language");
        }
        
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView list = new ListView(getActivity());
        CategoryPrayersAdapter adapter = new CategoryPrayersAdapter(getActivity(), mCategory, mLanguage);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View itemView, int index, long itemId) {
                PrayerFragment prayerFragment = new PrayerFragment();
                Bundle args = new Bundle();
                args.putLong(PrayerFragment.PRAYER_ID_ARGUMENT, itemId);
                prayerFragment.setArguments(args);
                
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(android.R.id.content, prayerFragment, PrayerFragment.PRAYER_TAG);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack(PrayerFragment.PRAYER_TAG);
                ft.commit();
            }
        });
        
        return list;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        getActivity().getActionBar().setTitle(mCategory);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setHomeButtonEnabled(true);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
        }
        
        return true;
    }
    
    static class CategoryPrayersAdapter extends BaseAdapter {
        
        private final Database prayersDb;
        private final Cursor prayersCursor;
        private final Context mContext;

        public CategoryPrayersAdapter(Context context, String category, Language language) {
            this.mContext = context;
            prayersDb = Database.getInstance();
            prayersCursor = prayersDb.getPrayers(category, language);
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
            PrayerItemView piv;
            if (convertView != null)
                piv = (PrayerItemView)convertView;
            else
                piv = new PrayerItemView(mContext);
            
            prayersCursor.moveToPosition(position);
            
            int openingWordsColumnIndex = prayersCursor.getColumnIndexOrThrow(Database.OPENINGWORDS_COLUMN);
            String openingWords = prayersCursor.getString(openingWordsColumnIndex);
            piv.setTitle(openingWords);
            
            int authorColumnIndex = prayersCursor.getColumnIndexOrThrow(Database.AUTHOR_COLUMN);
            String author = prayersCursor.getString(authorColumnIndex);
            piv.setAuthor(author);
            
            int wordCountColumnIndex = prayersCursor.getColumnIndexOrThrow(Database.WORDCOUNT_COLUMN);
            String wordCount = prayersCursor.getString(wordCountColumnIndex);
            piv.setWordCount(wordCount + " " + mContext.getString(R.string.words));
            
            return piv;
        }
    }
}
