package com.arashpayan.prayerbook;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.arashpayan.util.DividerItemDecoration;

public class SearchFragment extends Fragment implements OnPrayerSelectedListener, TextWatcher {

    static String SEARCHPRAYERS_TAG = "SearchPrayers";

    private SearchAdapter mSearchAdapter;
    private View mSearchView;
    private ImageButton mClearButton;
    private CharSequence mQuery = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchAdapter = new SearchAdapter();
        mSearchAdapter.setListener(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(mSearchAdapter);

        final Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar == null) {
            throw new RuntimeException("Where's the toolbar?");
        }
        toolbar.getMenu().clear();

        mSearchView = inflater.inflate(R.layout.search_field, toolbar, false);
        final EditText searchField = mSearchView.findViewById(R.id.search_field);
        searchField.addTextChangedListener(this);
        mClearButton = mSearchView.findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchField.setText("");
            }
        });

        toolbar.addView(mSearchView);

        return recyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();

        final Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(null);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm == null) {
                    // should never happen
                    return;
                }
                imm.hideSoftInputFromWindow(toolbar.getWindowToken(), 0);
                requireFragmentManager().popBackStack();
            }
        });
        // if there's no query saved, then show the keyboard
        if (mQuery == null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null) {
                // should never happen
                return;
            }
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    public void onDestroyView() {
        final Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.removeView(mSearchView);

        super.onDestroyView();
    }

    @Override
    public void onPrayerSelected(long prayerId) {
        // the keyboard might still be present, so dismiss it
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            // should never happen
            return;
        }
        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar == null) {
            throw new RuntimeException("where's the toolbar?");
        }
        imm.hideSoftInputFromWindow(toolbar.getWindowToken(), 0);

        Intent intent =  PrayerActivity.newIntent(getContext(), prayerId);
        startActivity(intent);

        requireActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mQuery = s;
        if (mQuery.length() > 0) {
            mClearButton.setVisibility(View.VISIBLE);
        } else {
            mClearButton.setVisibility(View.INVISIBLE);
        }

        final String trimmed = s.toString().trim();
        if (trimmed.length() < 3) {
            mSearchAdapter.setCursor(null);
            return;
        }

        App.runInBackground(new Runnable() {
            @Override
            public void run() {
                String[] keywords = trimmed.split(" ");
                final Cursor c = DB.get().getPrayersWithKeywords(keywords,
                        Prefs.get(App.getApp()).getEnabledLanguages());
                App.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSearchAdapter.setCursor(c);
                    }
                });
            }
        });
    }

    @Override
    public void afterTextChanged(Editable s) {}
}
