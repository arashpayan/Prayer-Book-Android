package com.arashpayan.prayerbook;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arashpayan.util.DividerItemDecoration;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 *
 * @author arash
 */
public class CategoriesFragment extends Fragment implements CategoriesAdapter.OnCategorySelectedListener, Toolbar.OnMenuItemClickListener, Prefs.Listener {
    
    static final String CATEGORIES_TAG = "Categories";

    private Parcelable mRecyclerState;
    private RecyclerView mRecyclerView;
    private EnabledCategoriesAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        Prefs.get().addListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.categories, menu);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRecyclerView = new RecyclerView(requireContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        mAdapter = new EnabledCategoriesAdapter(getActivity(), Prefs.get().getEnabledLanguages());
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        return mRecyclerView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Prefs.get().removeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        // We save our RecyclerView's state here, because onSaveInstanceState() doesn't get called
        // when your Fragments are just getting swapped within the same Activity.
        if (mRecyclerView != null) {
            RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
            if (lm != null) {
                mRecyclerState = lm.onSaveInstanceState();
            }
        }
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_languages:
                showLanguageDialog();
                break;
            case R.id.action_about:
                AboutDialogFragment adf = new AboutDialogFragment();
                adf.show(requireFragmentManager(), "dialog");
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

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.categories);
            toolbar.setOnMenuItemClickListener(this);
            toolbar.setTitle(R.string.app_name);
            toolbar.setNavigationIcon(null);

        }
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (mRecyclerState != null) {
            RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
            if (lm != null) {
                lm.onRestoreInstanceState(mRecyclerState);
            }
        }
    }

    @Override
    public void onCategorySelected(String category, Language language) {
        CategoryPrayersFragment fragment = CategoryPrayersFragment.newInstance(category, language);

        FragmentTransaction ft = requireFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        ft.replace(R.id.main_container, fragment, CategoryPrayersFragment.CATEGORYPRAYERS_TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void onSearch() {
        SearchFragment sf = new SearchFragment();
        FragmentTransaction ft = requireFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        ft.replace(R.id.main_container, sf, SearchFragment.SEARCHPRAYERS_TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    @UiThread
    public void onEnabledLanguagesChanged() {
        if (mRecyclerView == null) {
            return;
        }

        mAdapter = new EnabledCategoriesAdapter(getActivity(), Prefs.get().getEnabledLanguages());
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void showLanguageDialog() {
        Prefs prefs = Prefs.get();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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
                        Prefs.get().setLanguageEnabled(langs[which], isChecked);
                    }
                });
        builder.setPositiveButton(R.string.ok, null);
        builder.show();
    }
}
