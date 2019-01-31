package com.arashpayan.prayerbook;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arashpayan.prayerbook.thread.UiRunnable;
import com.arashpayan.prayerbook.thread.WorkerRunnable;
import com.arashpayan.util.DividerItemDecoration;
import com.arashpayan.util.L;

/**
 *
 * @author arash
 */
public class CategoriesFragment extends Fragment implements CategoriesAdapter.OnCategorySelectedListener, Toolbar.OnMenuItemClickListener, Prefs.Listener {
    
    static final String TAG = "prayers";

    private Parcelable mRecyclerState;
    private RecyclerView recyclerView;
    private CategoriesAdapter adapter;

    @NonNull
    static CategoriesFragment newInstance() {
        return new CategoriesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mRecyclerState = savedInstanceState.getParcelable("recycler_state");
            L.i("found a recycler state? " + mRecyclerState);
        }

        App.runInBackground(new WorkerRunnable() {
            @Override
            public void run() {
                CategoriesAdapter a = new CategoriesAdapter(Prefs.get().getEnabledLanguages(), CategoriesFragment.this);
                App.runOnUiThread(new UiRunnable() {
                    @Override
                    public void run() {
                        adapter = a;
                        if (recyclerView != null) {
                            recyclerView.setAdapter(adapter);
                        }
                    }
                });
            }
        });

        setHasOptionsMenu(true);
        Prefs.get().addListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.categories, menu);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, container, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        return recyclerView;
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
        if (recyclerView != null) {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm != null) {
                mRecyclerState = lm.onSaveInstanceState();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (recyclerView != null) {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm != null) {
                Parcelable state = lm.onSaveInstanceState();
                outState.putParcelable("recycler_state", state);
            }
        }
    }

    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.search_prayers) {
            onSearch();
            return true;
        }

        return false;
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
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm != null) {
                lm.onRestoreInstanceState(mRecyclerState);
            }
        }
    }

    @Override
    public void onCategorySelected(@NonNull String category, @NonNull Language language) {
        CategoryPrayersFragment fragment = CategoryPrayersFragment.newInstance(category, language);

        FragmentTransaction ft = requireFragmentManager().beginTransaction();
        ft.replace(R.id.main_container, fragment, CategoryPrayersFragment.TAG);
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

    public void onEnabledLanguagesChanged() {
        if (recyclerView == null) {
            return;
        }

        App.runInBackground(new WorkerRunnable() {
            @Override
            public void run() {
                CategoriesAdapter a = new CategoriesAdapter(Prefs.get().getEnabledLanguages(), CategoriesFragment.this);
                App.runOnUiThread(new UiRunnable() {
                    @Override
                    public void run() {
                        adapter = a;
                        if (recyclerView != null) {
                            recyclerView.setAdapter(adapter);
                        }
                    }
                });
            }
        });
    }
}
