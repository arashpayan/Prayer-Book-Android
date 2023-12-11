package com.arashpayan.prayerbook;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arashpayan.prayerbook.database.UserDB;
import com.arashpayan.prayerbook.thread.UiRunnable;
import com.arashpayan.prayerbook.thread.WorkerRunnable;
import com.arashpayan.util.DividerItemDecoration;
import com.arashpayan.util.L;

import java.util.ArrayList;

public class RecentsFragment extends Fragment implements UserDB.Listener {

    static final String TAG = "recents";

    @NonNull
    static RecentsFragment newInstance() {
        return new RecentsFragment();
    }

    private RecentsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        L.i("Recents.onCreate");
        super.onCreate(savedInstanceState);

        adapter = new RecentsAdapter(prayerSelectionListener);
        App.runInBackground(new WorkerRunnable() {
            @Override
            public void run() {
                ArrayList<Long> recents = UserDB.get().getRecents();
                App.runOnUiThread(new UiRunnable() {
                    @Override
                    public void run() {
                        adapter.setRecentIds(recents);
                    }
                });
            }
        });

        UserDB.get().addListener(this);
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
    public void onDestroy() {
        UserDB.get().removeListener(this);

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.recents);
            toolbar.setTitle(getString(R.string.recents));
            toolbar.setNavigationIcon(null);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.action_clear_all) {
                        clearRecentsAction();
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    //region Business logic

    private void clearRecentsAction() {
        AlertDialog.Builder bldr = new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        bldr.setMessage(getString(R.string.clear_recents_interrogative));
        bldr.setPositiveButton(R.string.clear, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserDB.get().clearRecents();
                // We'll receive a call back on our listener method that will cause us to update
                // the UI.
            }
        });
        bldr.setNegativeButton(R.string.no, null);
        bldr.setCancelable(true);
        bldr.show();
    }

    //endregion

    private final OnPrayerSelectedListener prayerSelectionListener = new OnPrayerSelectedListener() {
        @Override
        public void onPrayerSelected(long prayerId) {
            Intent intent = PrayerActivity.newIntent(requireContext(), prayerId);
            startActivity(intent);
        }
    };

    //region Recents listener

    @Override
    public void onPrayerAccessed(long prayerId) {
        adapter.onPrayerAccessed(prayerId);
    }

    @Override
    public void onRecentsCleared() {
        adapter.clearAll();
    }

    //endregion
}
