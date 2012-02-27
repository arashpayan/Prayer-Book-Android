/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.arashpayan.util.Graphics;

/**
 *
 * @author arash
 */
public class CategoriesFragment extends Fragment {
    
    private CategoriesAdapter categoriesAdapter;
    
    private static final int ACTIONITEM_SEARCH          = 0;
    private static final int ACTIONITEM_LANGUAGES       = 1;
    private static final int ACTIONITEM_ABOUT           = 2;
    
    private int firstVisiblePosition;
    private ListView list;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
            
        Log.i(PrayerBook.TAG, "CategoriesFragment.onAttach()");
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        setHasOptionsMenu(true);
    }
    
    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        MenuItem menuItem = menu.add(0, ACTIONITEM_SEARCH, ACTIONITEM_SEARCH, R.string.search);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setIcon(R.drawable.ic_action_search);
//        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = new SearchView(getActivity());
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        item.setActionView(searchView);
//
//        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        item.setIcon(R.drawable.ic_action_search);
//        menu
        menu.add(0, ACTIONITEM_LANGUAGES, ACTIONITEM_LANGUAGES, R.string.languages);
        menu.add(0, ACTIONITEM_ABOUT, ACTIONITEM_ABOUT, R.string.about);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case ACTIONITEM_SEARCH:
                
                break;
            case ACTIONITEM_LANGUAGES:
                Intent i = new Intent(getActivity(), LanguagesActivity.class);
                startActivityForResult(i, 0);
                break;
            case ACTIONITEM_ABOUT:
                AboutDialogFragment adf = new AboutDialogFragment();
                adf.show(getFragmentManager(), "dialog");
                break;
            default:
                return false;
        }
        
        return true;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        getActivity().getActionBar().setTitle("Prayer Book");
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        getActivity().getActionBar().setHomeButtonEnabled(false);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        firstVisiblePosition = list.getFirstVisiblePosition();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        list.setSelectionFromTop(firstVisiblePosition, 0);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        list = new ListView(getActivity());
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
            setBackgroundColor(Color.argb(0, 0, 0, 0));
            
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
            return prayerCountTextView.getText();
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
