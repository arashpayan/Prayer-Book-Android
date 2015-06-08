/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.arashpayan.prayerbook.event.LanguagesChangedEvent;
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
        Language[] enabledLanguages = Preferences.getInstance(App.getApp()).getEnabledLanguages();
        MergeAdapter mergeAdapter = new MergeAdapter();
        boolean showSectionTitles = enabledLanguages.length > 1;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        App.registerOnBus(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.categories, menu);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_languages:
                showLanguageDialog();
                break;
            case R.id.action_about:
                AboutDialogFragment adf = new AboutDialogFragment();
                adf.show(getFragmentManager(), "dialog");
                break;
            case R.id.search_prayers:
                onSearch();
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setTitle(getString(R.string.app_name));
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setHomeButtonEnabled(false);
            ab.setDisplayShowTitleEnabled(true);
        }
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
        CategoryPrayersFragment fragment = new CategoryPrayersFragment();
        Bundle args = new Bundle();
        args.putString(CategoryPrayersFragment.CATEGORY_ARGUMENT, item.first);
        args.putParcelable(CategoryPrayersFragment.LANGUAGE_ARGUMENT, item.second);
        fragment.setArguments(args);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(CategoryPrayersFragment.CATEGORYPRAYERS_TAG);
        ft.replace(R.id.pb_container, fragment, CategoryPrayersFragment.CATEGORYPRAYERS_TAG);
        ft.commit();
    }

    private void onSearch() {
        SearchFragment sf = new SearchFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(SearchFragment.SEARCHPRAYERS_TAG);
        ft.replace(R.id.pb_container, sf, SearchFragment.SEARCHPRAYERS_TAG);
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

    private void showLanguageDialog() {
        Preferences prefs = Preferences.getInstance(App.getApp());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.languages);
        final Language[] langs = Language.values();
        CharSequence[] langSequences = new CharSequence[langs.length];
        boolean[] langEnabled = new boolean[langs.length];
        for (int i=0; i<langs.length; i++) {
            langSequences[i] = getString(langs[i].humanName);
            langEnabled[i] = prefs.isLanguageEnabled(langs[i]);
        }
        builder.setMultiChoiceItems(langSequences, langEnabled,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        Preferences.getInstance(App.getApp()).setLanguageEnabled(langs[which], isChecked);
                    }
                });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                App.postOnBus(new LanguagesChangedEvent());
            }
        });
        builder.show();
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
