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
import com.arashpayan.util.L;

/**
 *
 * @author arash
 */
public class CategoryPrayersFragment extends Fragment {
    
    public static final String CATEGORYPRAYERS_TAG = "CategoryPrayers";
    public static final String CATEGORY_ARGUMENT = "Category";
    public static final String LANGUAGE_ARGUMENT = "Language";
    private String mCategory;
    private Language mLanguage;
    private CategoryPrayersAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Parcelable mRecyclerState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mCategory = getArguments().getString(CATEGORY_ARGUMENT, null);
        mLanguage = getArguments().getParcelable(LANGUAGE_ARGUMENT);
        if (mCategory == null) {
            throw new IllegalArgumentException("You must provide a category");
        }
        if (mLanguage == null) {
            throw new IllegalArgumentException("You must provide a language");
        }
        
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
            L.i("onback");
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
        mAdapter = new CategoryPrayersAdapter(mCategory, mLanguage);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                onPrayerClicked(position);
            }
        }));
        
        return mRecyclerView;
    }

    private void onPrayerClicked(int position) {
        Intent intent = new Intent(getActivity(), PrayerActivity.class);
        long prayerID = mAdapter.getItemId(position);
        intent.putExtra(PrayerFragment.PRAYER_ID_ARGUMENT, prayerID);
        startActivity(intent);

        getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
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
}
