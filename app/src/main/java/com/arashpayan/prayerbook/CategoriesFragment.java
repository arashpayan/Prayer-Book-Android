/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
public class CategoriesFragment extends Fragment {
    
    public static final String CATEGORIES_TAG = "Categories";

    private MergeAdapter mMergeAdapter;
    
    private int firstVisiblePosition;
    private ListView mListView;

    private MergeAdapter buildAdapter() {
        LinkedList<Language> enabledLanguages = getEnabledLanguages();
        MergeAdapter mergeAdapter = new MergeAdapter();
        boolean showSectionTitles = enabledLanguages.size() > 1;
        for (Language l : enabledLanguages) {
            if (showSectionTitles) {
                ListSectionTitle title = new ListSectionTitle(getActivity(), getString(l.humanName));
                mergeAdapter.addView(title, false);
            }
            CategoriesAdapter adapter = new CategoriesAdapter(l);
            mergeAdapter.addAdapter(adapter);
        }

        return mergeAdapter;
    }

    private LinkedList<Language> getEnabledLanguages() {
        Preferences preferences = Preferences.getInstance(getActivity().getApplication());
        LinkedList<Language> enabledLanguages = new LinkedList<Language>();
        for (Language l : Language.values()) {
            if (preferences.isLanguageEnabled(l)) {
                enabledLanguages.add(l);
            }
        }

        if (enabledLanguages.isEmpty()) {
            // find the user's locale and see if it matches any of the known languages
            Locale defaultLocale = Locale.getDefault();
            String langCode = defaultLocale.getLanguage();
            for (Language l : Language.values()) {
                if (langCode.startsWith(l.code)) {
                    enabledLanguages.add(l);
                }
            }
        }

        // if it's still empty, just enable English
        if (enabledLanguages.isEmpty()) {
            enabledLanguages.add(Language.English);
        }

        return enabledLanguages;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.registerOnBus(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        App.unregisterFromBus(this);
    }

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
                onCategoryClicked(index);
            }
        });
        
        return mListView;
    }

    private void onCategoryClicked(int index) {
        @SuppressWarnings("unchecked")
        Pair<String, Language> item = (Pair<String, Language>) mMergeAdapter.getItem(index);
        Intent i = new Intent(getActivity(), CategoryPrayersActivity.class);
        i.putExtra(CategoryPrayersActivity.CATEGORY_ARGUMENT, item.first);
        i.putExtra(CategoryPrayersActivity.LANGUAGE_ARGUMENT, (Parcelable) item.second);
        startActivity(i);
    }

    @Subscribe @SuppressWarnings("unused")
    public void onLanguagesChanged(LanguagesChangedEvent event) {
        if (mListView == null) {
            return;
        }

        mMergeAdapter = buildAdapter();
        mListView.setAdapter(mMergeAdapter);
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
            categoryTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            categoryTextView.setPadding(Graphics.pixels(context, 16), eightDp, eightDp, eightDp);
            categoryTextView.setId(CATEGORY_TEXTVIEW_ID);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            params.addRule(ALIGN_PARENT_LEFT, NO_ID);
            params.addRule(RelativeLayout.CENTER_VERTICAL, CATEGORY_TEXTVIEW_ID);
            categoryTextView.setLayoutParams(params);
            addView(categoryTextView);
            
            prayerCountTextView = new TextView(context);
            prayerCountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            prayerCountTextView.setPadding(eightDp, eightDp, Graphics.pixels(context, 16), eightDp);
            prayerCountTextView.setId(PRAYER_COUNT_TEXTVIEW_ID);
            params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            params.addRule(ALIGN_PARENT_RIGHT, NO_ID);
            params.addRule(CENTER_VERTICAL, PRAYER_COUNT_TEXTVIEW_ID);
            prayerCountTextView.setLayoutParams(params);
            addView(prayerCountTextView);
        }
        
        public void setCategory(CharSequence aCategory) {
            categoryTextView.setText(aCategory);
        }
        
        public void setPrayerCount(CharSequence aPrayerCount) {
            prayerCountTextView.setText(aPrayerCount);
        }
    }

    class CategoriesAdapter extends BaseAdapter {
        
        private final Database prayersDb;
        private final Cursor categoriesCursor;
        private final Language mLanguage;
        
        public CategoriesAdapter(Language language) {
            this.mLanguage = language;
            prayersDb = Database.getInstance();
            categoriesCursor = prayersDb.getCategories(mLanguage);
        }
        
        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
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
