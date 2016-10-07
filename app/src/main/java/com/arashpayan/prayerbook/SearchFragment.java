package com.arashpayan.prayerbook;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.arashpayan.util.DividerItemDecoration;

public class SearchFragment extends Fragment implements OnPrayerSelectedListener {

    public static String SEARCHPRAYERS_TAG = "SearchPrayers";

    private SearchAdapter mSearchAdapter;
    private CharSequence mQuery = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchAdapter = new SearchAdapter();
        mSearchAdapter.setListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        installSearchView();

        RecyclerView recyclerView = new RecyclerView(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(mSearchAdapter);

        return recyclerView;
    }

    private void installSearchView() {
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (toolbar == null) {
            throw new RuntimeException("Where's the toolbar?");
        }
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.search);
        MenuItem item = toolbar.getMenu().getItem(0);
        if (item == null) {
            throw new RuntimeException("Where's the search menu?");
        }
        SearchView sv = (SearchView) item.getActionView();
        sv.setIconifiedByDefault(false);
        sv.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        sv.setInputType(EditorInfo.TYPE_CLASS_TEXT);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mQuery = newText;
                final String trimmed = newText.trim();
                if (trimmed.length() < 3) {
                    mSearchAdapter.setCursor(null);
                    return true;
                }

                App.postOnBackgroundThread(new Runnable() {
                    @Override
                    public void run() {
                        String[] keywords = trimmed.split(" ");
                        final Cursor c = Database.getInstance().getPrayersWithKeywords(keywords,
                                Preferences.getInstance(App.getApp()).getEnabledLanguages());
                        App.postOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                mSearchAdapter.setCursor(c);
                            }
                        });
                    }
                });

                return true;
            }
        });
        sv.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(null);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(toolbar.getWindowToken(), 0);
                getFragmentManager().popBackStack();
            }
        });
        MenuItem item = toolbar.getMenu().getItem(0);
        if (item == null) {
            throw new RuntimeException("where's the searchview menu item");
        }
        SearchView sv = (SearchView) item.getActionView();
        if (mQuery != null) {
            sv.setQuery(mQuery, true);
        } else {
            sv.requestFocus();
        }

        // if there's no query saved, then show the keyboard
        if (mQuery == null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowCustomEnabled(false);
        }
    }

    @Override
    public void onPrayerSelected(long prayerId) {
        // the keyboard might still be present, so dismiss it
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (toolbar == null) {
            throw new RuntimeException("where's the toolbar?");
        }
        imm.hideSoftInputFromWindow(toolbar.getWindowToken(), 0);

        Intent intent =  PrayerActivity.newIntent(getContext(), prayerId);
        startActivity(intent);

        getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
    }
}
