package com.arashpayan.prayerbook;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arashpayan.prayerbook.database.PrayersDB;
import com.arashpayan.prayerbook.thread.UiRunnable;
import com.arashpayan.prayerbook.thread.WorkerRunnable;
import com.arashpayan.util.DividerItemDecoration;

import java.util.ArrayList;

/**
 *
 * @author arash
 */
public class CategoryPrayersFragment extends Fragment implements OnPrayerSelectedListener {
    
    static final String TAG = "CategoryPrayers";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_LANGUAGE = "language";
    private String category;
    private CategoryPrayersAdapter adapter;
    private RecyclerView recyclerView;
    private Parcelable recyclerState;

    static CategoryPrayersFragment newInstance(@NonNull String category, @NonNull Language language) {
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

        Bundle bundle = getArguments();
        if (bundle == null) {
            throw new RuntimeException("Fragment should be started via newInstance");
        }
        category = bundle.getString(ARG_CATEGORY, null);
        Language language = getArguments().getParcelable(ARG_LANGUAGE);
        if (category == null) {
            throw new IllegalArgumentException("You must provide a category");
        }
        if (language == null) {
            throw new IllegalArgumentException("You must provide a language");
        }
        adapter = new CategoryPrayersAdapter(language, this);
        App.runInBackground(new WorkerRunnable() {
            @Override
            public void run() {
                ArrayList<Long> ids = PrayersDB.get().getPrayerIds(category, language);
                App.runOnUiThread(new UiRunnable() {
                    @Override
                    public void run() {
                        adapter.setPrayerIds(ids);
                    }
                });
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        // We save our RecyclerView's state here, because onSaveInstanceState() doesn't get called
        // when your Fragments are just getting swapped within the same Activity.
        if (recyclerView != null) {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm != null) {
                recyclerState = lm.onSaveInstanceState();
            }
        }
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (recyclerState != null) {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm != null) {
                lm.onRestoreInstanceState(recyclerState);
            }
        }
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
    public void onResume() {
        super.onResume();

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.getMenu().clear();
            toolbar.setTitle(category);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requireFragmentManager().popBackStack();
                }
            });
        }
    }

    @Override
    public void onPrayerSelected(long prayerId) {
        Intent intent = PrayerActivity.newIntent(requireContext(), prayerId);
        startActivity(intent);
    }
}
