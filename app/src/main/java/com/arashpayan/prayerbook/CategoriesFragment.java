package com.arashpayan.prayerbook;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arashpayan.prayerbook.event.LanguagesChangedEvent;
import com.arashpayan.util.DividerItemDecoration;
import com.squareup.otto.Subscribe;

/**
 *
 * @author arash
 */
public class CategoriesFragment extends Fragment implements CategoriesAdapter.OnCategorySelectedListener, Toolbar.OnMenuItemClickListener {
    
    public static final String CATEGORIES_TAG = "Categories";

    private Parcelable mRecyclerState;
    private RecyclerView mRecyclerView;
    private EnabledCategoriesAdapter mAdapter;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRecyclerView = new RecyclerView(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        mAdapter = new EnabledCategoriesAdapter(getActivity(), Prefs.get(App.getApp()).getEnabledLanguages());
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        return mRecyclerView;
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

    public boolean onMenuItemClick(MenuItem item) {
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

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.categories);
            toolbar.setOnMenuItemClickListener(this);
            toolbar.setTitle(R.string.app_name);
            toolbar.setNavigationIcon(null);

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
    public void onCategorySelected(String category, Language language) {
        CategoryPrayersFragment fragment = CategoryPrayersFragment.newInstance(category, language);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        ft.replace(R.id.main_container, fragment, CategoryPrayersFragment.CATEGORYPRAYERS_TAG);
        ft.addToBackStack(null);
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
        ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        ft.replace(R.id.main_container, sf, SearchFragment.SEARCHPRAYERS_TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Subscribe @SuppressWarnings("unused")
    public void onLanguagesChanged(LanguagesChangedEvent event) {
        if (mRecyclerView == null) {
            return;
        }

        mAdapter = new EnabledCategoriesAdapter(getActivity(), Prefs.get(App.getApp()).getEnabledLanguages());
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void showLanguageDialog() {
        Prefs prefs = Prefs.get(App.getApp());
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
                        Prefs.get(App.getApp()).setLanguageEnabled(langs[which], isChecked);
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
