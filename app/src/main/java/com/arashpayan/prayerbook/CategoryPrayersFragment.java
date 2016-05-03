/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arashpayan.util.DividerItemDecoration;

/**
 *
 * @author arash
 */
public class CategoryPrayersFragment extends Fragment implements OnPrayerSelectedListener {
    
    public static final String CATEGORYPRAYERS_TAG = "CategoryPrayers";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_LANGUAGE = "language";
    private String mCategory;
    private CategoryPrayersAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Parcelable mRecyclerState;

    public static CategoryPrayersFragment newInstance(String category, Language language) {
        CategoryPrayersFragment fragment = new CategoryPrayersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        args.putParcelable(ARG_LANGUAGE, language);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mCategory = getArguments().getString(ARG_CATEGORY, null);
        Language language = getArguments().getParcelable(ARG_LANGUAGE);
        if (mCategory == null) {
            throw new IllegalArgumentException("You must provide a category");
        }
        if (language == null) {
            throw new IllegalArgumentException("You must provide a language");
        }
        mAdapter = new CategoryPrayersAdapter(mCategory, language);
        mAdapter.setListener(this);
        
        setHasOptionsMenu(true);
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
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }

        return false;
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
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(mAdapter);
        
        return mRecyclerView;
    }
    
    @Override
    public void onResume() {
        super.onResume();

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setTitle(mCategory);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
        expandToolbar();
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

    @Override
    public void onPrayerSelected(long prayerId) {
        Intent intent = PrayerActivity.newIntent(getContext(), prayerId);
        startActivity(intent);

        getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
    }
}
