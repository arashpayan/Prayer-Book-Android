package com.arashpayan.prayerbook;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.arashpayan.util.L;

/**
 * Created by arash on 12/22/14.
 */
public class SearchFragment extends Fragment {

    public static String SEARCHPRAYERS_TAG = "SearchPrayers";

    private RecyclerView mRecyclerView;
    private SearchAdapter mSearchAdapter;

    public void onStart() {
        super.onStart();

        ActionBarActivity activity = (ActionBarActivity)getActivity();
        ActionBar ab = activity.getSupportActionBar();
        ab.setDisplayShowCustomEnabled(true);

        // let the actionbar inflate the search view first so it can style it appropriately.
        // Then we can retrieve it to add our listeners.
        ab.setCustomView(R.layout.search_view);
        SearchView sv = (SearchView) ab.getCustomView();
        ab.setCustomView(sv);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                L.i("text submit: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                L.i("text change: " + newText);
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
                L.i("onClose");
                return false;
            }
        });
        sv.requestFocus();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRecyclerView = new RecyclerView(getActivity());
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mSearchAdapter = new SearchAdapter();
        mRecyclerView.setAdapter(mSearchAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                onPrayerClicked(position);
//                L.i("clicked item at position " + position);
//                long prayerID = mSearchAdapter.getItemId(position);

            }
        }));

        return mRecyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar ab = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);

            SearchView sv = (SearchView) ab.getCustomView();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        ActionBarActivity activity = (ActionBarActivity)getActivity();
        activity.getSupportActionBar().setDisplayShowCustomEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }

        return false;
    }

    public void onPrayerClicked(int position) {
        // the keyboard might still be present, so dismiss it
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        ActionBar ab = activity.getSupportActionBar();
        SearchView sv = (SearchView) ab.getCustomView();
        imm.hideSoftInputFromWindow(sv.getWindowToken(), 0);

        long prayerID = mSearchAdapter.getItemId(position);
        PrayerFragment prayerFragment = new PrayerFragment();
        Bundle args = new Bundle();
        args.putLong(PrayerFragment.PRAYER_ID_ARGUMENT, prayerID);
        prayerFragment.setArguments(args);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction().
                replace(R.id.pb_container, prayerFragment, PrayerFragment.PRAYER_TAG).
                addToBackStack(PrayerFragment.PRAYER_TAG);
        ft.commit();
    }
}
