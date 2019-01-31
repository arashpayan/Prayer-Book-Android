package com.arashpayan.prayerbook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LanguagesAdapter extends RecyclerView.Adapter<LanguagesFragment.LanguageViewHolder> {

    private Language[] languages;
    final private Prefs prefs;

    LanguagesAdapter() {
        languages = Language.values();
        prefs = Prefs.get();
    }

    @Override
    public int getItemCount() {
        return languages.length;
    }

    @Override
    public void onBindViewHolder(@NonNull LanguagesFragment.LanguageViewHolder holder, int position) {
        Language l = languages[position];
        holder.language.setText(l.humanName);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(prefs.isLanguageEnabled(l));
    }

    @NonNull
    @Override
    public LanguagesFragment.LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.language_item, parent, false);
        final LanguagesFragment.LanguageViewHolder holder = new LanguagesFragment.LanguageViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                boolean shouldEnable = !holder.checkBox.isChecked();
                holder.checkBox.setChecked(shouldEnable);
                prefs.setLanguageEnabled(languages[pos], shouldEnable);
            }
        });
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                // by the time onClick is called, the check box state has already changed,
                // so we don't need to ! (not) the isChecked value
                boolean shouldEnable = holder.checkBox.isChecked();
                prefs.setLanguageEnabled(languages[pos], shouldEnable);
            }
        });
        return holder;
    }
}
