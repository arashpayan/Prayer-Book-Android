package com.arashpayan.prayerbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.arashpayan.util.DividerItemDecoration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LanguagesFragment extends Fragment implements MenuProvider {

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.addMenuProvider(this, getViewLifecycleOwner());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.languages));
            toolbar.setNavigationIcon(null);
        }
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.categories, menu);
        // Remove search icon from languages menu
        MenuItem searchItem = menu.findItem(R.id.search_prayers);
        if (searchItem != null) {
            searchItem.setVisible(false);
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_speech_settings) {
            SpeechSettingsDialogFragment.newInstance(Prefs.get().getLanguage()).show(getParentFragmentManager(), SpeechSettingsDialogFragment.TAG);
            return true;
        }

        return false;
    }


}
