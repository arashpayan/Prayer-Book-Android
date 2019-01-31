package com.arashpayan.prayerbook;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arashpayan.prayerbook.database.UserDB;
import com.arashpayan.prayerbook.thread.UiRunnable;
import com.arashpayan.prayerbook.thread.WorkerRunnable;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BookmarksFragment extends Fragment {

    static final String TAG = "bookmarks";

    @NonNull
    static BookmarksFragment newInstance() {
        return new BookmarksFragment();
    }

    private BookmarksAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new BookmarksAdapter(prayerSelectedListener);
        App.runInBackground(new WorkerRunnable() {
            @Override
            public void run() {
                ArrayList<Long> bookmarks = UserDB.get().getBookmarks();
                App.runOnUiThread(new UiRunnable() {
                    @Override
                    public void run() {
                        adapter.setBookmarks(bookmarks);
                    }
                });
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, container, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        return recyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.getMenu().clear();
            toolbar.setTitle(R.string.bookmarks);
            toolbar.setNavigationIcon(null);
        }
    }

    private OnPrayerSelectedListener prayerSelectedListener = new OnPrayerSelectedListener() {
        @Override
        public void onPrayerSelected(long prayerId) {
            Intent intent = PrayerActivity.newIntent(requireContext(), prayerId);
            startActivity(intent);
        }
    };
}
