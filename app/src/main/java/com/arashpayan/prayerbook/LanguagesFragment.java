package com.arashpayan.prayerbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.arashpayan.util.DividerItemDecoration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LanguagesFragment extends Fragment {

    static final String TAG = "languages";

    @NonNull
    static LanguagesFragment newInstance() {
        return new LanguagesFragment();
    }

    private LanguagesAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new LanguagesAdapter();
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

    static class LanguageViewHolder extends RecyclerView.ViewHolder {

        final TextView language;
        final CheckBox checkBox;

        LanguageViewHolder(@NonNull View itemView) {
            super(itemView);

            language = itemView.findViewById(R.id.language);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.getMenu().clear();
            toolbar.setTitle(getString(R.string.languages));
            toolbar.setNavigationIcon(null);
        }
    }


}
