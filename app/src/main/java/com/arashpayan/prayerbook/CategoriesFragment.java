/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arashpayan.prayerbook.event.LanguagesChangedEvent;
import com.squareup.otto.Subscribe;

/**
 *
 * @author arash
 */
public class CategoriesFragment extends Fragment {
    
    public static final String CATEGORIES_TAG = "Categories";

    private Parcelable mRecyclerState;
    private RecyclerView mRecyclerView;
    private CategoriesAdapter mCategoriesAdapter;

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

        // We save our RecyclerView's state here, because onSaveInstanceState() doesn't get called
        // when your Fragments are just getting swapped within the same Activity.
        if (mRecyclerView != null) {
            mRecyclerState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        }
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
        expandToolbar();
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (mRecyclerState != null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mRecyclerState);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRecyclerView = new RecyclerView(getActivity());
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mCategoriesAdapter = new CategoriesAdapter(Language.English);
        mRecyclerView.setAdapter(mCategoriesAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                onCategoryClicked(position);
            }
        }));

        return mRecyclerView;
    }

    private void onCategoryClicked(int position) {
        CategoryPrayersFragment fragment = new CategoryPrayersFragment();
        Bundle args = new Bundle();
        String category = mCategoriesAdapter.getCategory(position);
        args.putString(CategoryPrayersFragment.CATEGORY_ARGUMENT, mCategoriesAdapter.getCategory(position));
        args.putParcelable(CategoryPrayersFragment.LANGUAGE_ARGUMENT, mCategoriesAdapter.getLanguage());
        fragment.setArguments(args);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.replace(R.id.pb_container, fragment, CategoryPrayersFragment.CATEGORYPRAYERS_TAG);
        ft.commit();
    }

    public void expandToolbar() {
        AppBarLayout appBarLayout = (AppBarLayout) getActivity().findViewById(R.id.appbar);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        final AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if(behavior!=null) {
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinator);
            behavior.onNestedFling(coordinatorLayout, appBarLayout, null, 0, -10000, false);
        }
    }

    private void onSearch() {
        SearchFragment sf = new SearchFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.replace(R.id.pb_container, sf, SearchFragment.SEARCHPRAYERS_TAG);
        ft.commit();
    }

    @Subscribe @SuppressWarnings("unused")
    public void onLanguagesChanged(LanguagesChangedEvent event) {
        if (mRecyclerView == null) {
            return;
        }

        mCategoriesAdapter = new com.arashpayan.prayerbook.CategoriesAdapter(Language.English);
        mRecyclerView.setAdapter(mCategoriesAdapter);
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
}
