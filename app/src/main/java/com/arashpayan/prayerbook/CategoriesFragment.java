/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.arashpayan.prayerbook.event.LanguagesChangedEvent;
import com.arashpayan.util.L;
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
        L.i("CategoriesFragment.onCreate");
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
        L.i("CategoriesFragment.onResume");
        super.onResume();

        mListView.setSelectionFromTop(firstVisiblePosition, 0);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        L.i("CategoriesFragment.onCreateView");

        mListView = new ListView(getActivity());
//        mListView.setBackgroundColor(Color.WHITE);
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
        CategoryPrayersFragment fragment = new CategoryPrayersFragment();
        Bundle args = new Bundle();
        args.putString(CategoryPrayersFragment.CATEGORY_ARGUMENT, item.first);
        args.putParcelable(CategoryPrayersFragment.LANGUAGE_ARGUMENT, (Parcelable)item.second);
        fragment.setArguments(args);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(CategoryPrayersFragment.CATEGORYPRAYERS_TAG);
        ft.replace(R.id.pb_container, fragment, CategoryPrayersFragment.CATEGORYPRAYERS_TAG);
        ft.commit();

    }

    @Subscribe @SuppressWarnings("unused")
    public void onLanguagesChanged(LanguagesChangedEvent event) {
        if (mListView == null) {
            return;
        }

        mMergeAdapter = buildAdapter();
        mListView.setAdapter(mMergeAdapter);
    }

    static class CategoryViewHolderItem {
        TextView titleTextView;
        TextView countTextView;
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
            CategoryViewHolderItem holder;
            if (convertView != null) {
                holder = (CategoryViewHolderItem)convertView.getTag();
            } else {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.category, parent, false);

                holder = new CategoryViewHolderItem();
                holder.titleTextView = (TextView)convertView.findViewById(R.id.category_title);
                holder.countTextView = (TextView)convertView.findViewById(R.id.category_prayers_count);

                convertView.setTag(holder);
            }
            
            categoriesCursor.moveToPosition(position);
            int categoryColumnIndex = categoriesCursor.getColumnIndexOrThrow(Database.CATEGORY_COLUMN);

            String category = categoriesCursor.getString(categoryColumnIndex);
            holder.titleTextView.setText(category);

            int prayerCount = Database.getInstance().getPrayerCountForCategory(category, mLanguage.code);
            holder.countTextView.setText(Integer.toString(prayerCount));
            
            return convertView;
        }
    }
}