/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.util.TypedValue;
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
import android.widget.SearchView;
import android.widget.TextView;

import com.arashpayan.prayerbook.event.LanguagesChangedEvent;
import com.arashpayan.util.Graphics;
import com.commonsware.cwac.merge.MergeAdapter;
import com.squareup.otto.Subscribe;

import java.util.LinkedList;
import java.util.Locale;

/**
 *
 * @author arash
 */
public class CategoriesFragment extends Fragment implements SearchView.OnQueryTextListener {
    
    public static final String CATEGORIES_TAG = "Categories";
    
    private CategoriesAdapter categoriesAdapter;
    private MergeAdapter mMergeAdapter;
    
    private int firstVisiblePosition;
    private ListView mListView;

    private final static int ACTIONITEM_SEARCH      = 1;

    private MergeAdapter buildAdapter() {
        LinkedList<Database.Language> enabledLanguages = getEnabledLanguages();
        MergeAdapter mergeAdapter = new MergeAdapter();
        boolean showSectionTitles = enabledLanguages.size() > 1;
        for (Database.Language l : enabledLanguages) {
            if (showSectionTitles) {
                ListSectionTitle title = new ListSectionTitle(getActivity(), getString(l.humanName));
                mergeAdapter.addView(title, false);
            }
            CategoriesAdapter adapter = new CategoriesAdapter(l);
            mergeAdapter.addAdapter(adapter);
        }

        return mergeAdapter;
    }

    private LinkedList<Database.Language> getEnabledLanguages() {
        Preferences preferences = Preferences.getInstance(getActivity().getApplication());
        LinkedList<Database.Language> enabledLanguages = new LinkedList<Database.Language>();
        if (preferences.isEnglishEnabled()) {
            enabledLanguages.add(Database.Language.English);
        }
        if (preferences.isSpanishEnabled()) {
            enabledLanguages.add(Database.Language.Spanish);
        }
        if (preferences.isPersianEnabled()) {
            enabledLanguages.add(Database.Language.Persian);
        }
        if (preferences.isFrenchEnabled()) {
            enabledLanguages.add(Database.Language.French);
        }
        if (preferences.isDutchEnabled()) {
            enabledLanguages.add(Database.Language.Dutch);
        }

        if (enabledLanguages.isEmpty()) {
            // find the user's locale and see if it matches any of the known languages
            Locale defaultLocale = Locale.getDefault();
            String langCode = defaultLocale.getLanguage();
            if (langCode.startsWith(Database.Language.English.code)) {
                enabledLanguages.add(Database.Language.English);
            } else if (langCode.startsWith(Database.Language.Spanish.code)) {
                enabledLanguages.add(Database.Language.Spanish);
            } else if (langCode.startsWith(Database.Language.Persian.code)) {
                enabledLanguages.add(Database.Language.French);
            } else if (langCode.startsWith(Database.Language.French.code)) {
                enabledLanguages.add(Database.Language.Dutch);
            }
        }

        // if it's still empty, just enable English
        if (enabledLanguages.isEmpty()) {
            enabledLanguages.add(Database.Language.English);
        }

        return enabledLanguages;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.registerOnBus(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(0, ACTIONITEM_SEARCH, ACTIONITEM_SEARCH, R.string.search);
        item.setIcon(R.drawable.ic_action_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView searchView = new SearchView(getActivity());
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(this);
        item.setActionView(searchView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        App.unregisterFromBus(this);
    }

    @Override
    public void onStart() {
        super.onStart();

//        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
//        getActivity().getActionBar().setHomeButtonEnabled(false);
    }

//    public boolean onOptionsItemSelected (MenuItem item) {
//        if (item.getItemId() == ACTIONITEM_SEARCH) {
//            return true;
//        }
//
//        return false;
//    }

    @Override
    public void onPause() {
        super.onPause();
        
        firstVisiblePosition = mListView.getFirstVisiblePosition();
    }
    
    @Override
    public void onResume() {
        super.onResume();

        mListView.setSelectionFromTop(firstVisiblePosition, 0);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mListView = new ListView(getActivity());
        mMergeAdapter = buildAdapter();
        mListView.setAdapter(mMergeAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View itemView, int index, long itemId) {
                onCategoryClicked(index, itemId);
            }
        });
        
        return mListView;
    }

    private void onCategoryClicked(int index, long itemId) {
        Pair<String, Database.Language> item = (Pair<String, Database.Language>) mMergeAdapter.getItem(index);
        Intent i = new Intent(getActivity(), CategoryPrayersActivity.class);
        i.putExtra(CategoryPrayersActivity.CATEGORY_ARGUMENT, item.first);
        i.putExtra(CategoryPrayersActivity.LANGUAGE_ARGUMENT, (Parcelable) item.second);
        startActivity(i);
    }

    @Subscribe
    public void onLanguagesChanged(LanguagesChangedEvent event) {
        if (mListView == null) {
            return;
        }

        mMergeAdapter = buildAdapter();
        mListView.setAdapter(mMergeAdapter);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
//        L.i("onQueryTextSubmit: " + s);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
//        L.i("onQueryTextChange: " + s);
//        if (s.trim().isEmpty()) {
//            mMergeAdapter = buildAdapter();
//            mListView.setAdapter(mMergeAdapter);
//        } else {
//            mMergeAdapter = new MergeAdapter();
//            mMergeAdapter.ad
//        }

        return true;
    }

    class CategoryView extends RelativeLayout {
        private final TextView categoryTextView;
        private final TextView prayerCountTextView;
        
        private static final int CATEGORY_TEXTVIEW_ID       = 38;
        private static final int PRAYER_COUNT_TEXTVIEW_ID   = 32;
        
        public CategoryView(Context context) {
            super(context);
            
            setMinimumHeight(Graphics.pixels(context, 48));
            setBackgroundColor(Color.argb(0, 0, 0, 0));
            int eightDp = Graphics.pixels(context, 8);
            
            categoryTextView = new TextView(context);
            categoryTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            categoryTextView.setPadding(Graphics.pixels(context, 16), eightDp, eightDp, eightDp);
            categoryTextView.setTypeface(Typeface.DEFAULT_BOLD);
            categoryTextView.setId(CATEGORY_TEXTVIEW_ID);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            params.addRule(ALIGN_PARENT_LEFT, NO_ID);
            params.addRule(RelativeLayout.CENTER_VERTICAL, CATEGORY_TEXTVIEW_ID);
            categoryTextView.setLayoutParams(params);
            addView(categoryTextView);
            
            prayerCountTextView = new TextView(context);
            prayerCountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            prayerCountTextView.setPadding(eightDp, eightDp, Graphics.pixels(context, 16), eightDp);
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
        
        private final Database prayersDb;
        private final Cursor categoriesCursor;
        private final Database.Language mLanguage;
        
        public CategoriesAdapter(Database.Language language) {
            this.mLanguage = language;
            prayersDb = Database.getInstance();
            categoriesCursor = prayersDb.getCategories(mLanguage);
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
            return Pair.create(categoriesCursor.getString(categoryColumnIndex), mLanguage);
        }
        
        public long getItemId(int position) {
            return position;
        }

        public Database.Language getLanguage() {
            return mLanguage;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            CategoryView categoryView;
            if (convertView != null)
                categoryView = (CategoryView)convertView;
            else
                categoryView = new CategoryView(getActivity());
            
            categoriesCursor.moveToPosition(position);
            int categoryColumnIndex = categoriesCursor.getColumnIndexOrThrow(Database.CATEGORY_COLUMN);
            String category = categoriesCursor.getString(categoryColumnIndex);
            categoryView.setCategory(category);
            
            int prayerCount = Database.getInstance().getPrayerCountForCategory(category, mLanguage.code);
            categoryView.setPrayerCount(Integer.toString(prayerCount));
            
            return categoryView;
        }
    }
}
