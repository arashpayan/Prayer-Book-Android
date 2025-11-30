package com.arashpayan.prayerbook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arashpayan.prayerbook.LanguagesFragment.LanguageViewHolder

internal class LanguagesAdapter : RecyclerView.Adapter<LanguageViewHolder>() {
    private val languages: Array<Language> = Language.values()
    private val prefs: Prefs = Prefs.get()

    override fun getItemCount(): Int {
        return languages.size
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val l = languages[position]
        holder.language?.setText(l.humanName)
        holder.checkBox?.isChecked = prefs.isLanguageEnabled(l)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.language_item, parent, false)
        val holder = LanguageViewHolder(itemView)

        val clickListener = View.OnClickListener {
            val pos = holder.absoluteAdapterPosition
            if (pos == RecyclerView.NO_POSITION) {
                return@OnClickListener
            }

            val selectedLanguage = languages[pos]

            // Single-selection logic (radio button effect)
            for (i in languages.indices) {
                val lang = languages[i]
                val shouldBeEnabled = lang == selectedLanguage
                if (prefs.isLanguageEnabled(lang) != shouldBeEnabled) {
                    prefs.setLanguageEnabled(lang, shouldBeEnabled)
                    notifyItemChanged(i)
                }
            }
        }

        itemView.setOnClickListener(clickListener)
        holder.checkBox?.setOnClickListener(clickListener)

        return holder
    }
}
