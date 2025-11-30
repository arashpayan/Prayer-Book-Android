package com.arashpayan.prayerbook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.recyclerview.widget.RecyclerView
import com.arashpayan.prayerbook.App.Companion.runInBackground
import com.arashpayan.prayerbook.App.Companion.runOnUiThread
import com.arashpayan.prayerbook.database.PrayersDB.Companion.get
import com.arashpayan.prayerbook.thread.UiRunnable
import com.arashpayan.prayerbook.thread.WorkerRunnable

internal class CategoriesAdapter @WorkerThread constructor(
    languages: ArrayList<Language>,
    private val listener: OnCategorySelectedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val items: ArrayList<CategoryItem> = ArrayList<CategoryItem>()

    init {
        for (l in languages) {
            items.add(CategoryItem(R.layout.list_header, l, null))
            val categories = get().getCategories(l)
            for (c in categories) {
                items.add(CategoryItem(R.layout.category, l, c))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false)
        if (viewType == R.layout.list_header) {
            return CategoryHeaderHolder(itemView)
        } else if (viewType == R.layout.category) {
            val holder = CategoryViewHolder(itemView)
            itemView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val pos = holder.getAbsoluteAdapterPosition()
                    val item = items.get(pos)
                    listener.onCategorySelected(item.text, item.language)
                }
            })
            return holder
        }

        throw RuntimeException("Unknown viewtype")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items.get(position)
        val ctx = holder.itemView.getContext()
        if (item.viewType == R.layout.list_header) {
            (holder as CategoryHeaderHolder).language.setText(ctx.getString(item.language.humanName))
        } else if (item.viewType == R.layout.category) {
            val h = holder as CategoryViewHolder
            h.category?.setText(item.text)
            h.setLanguage(item.language)
            h.prayerCount?.setText(null)

            runInBackground(object : WorkerRunnable {
                override fun run() {
                    // This only executes for actual category view holders, but because the headers
                    // are also represented by CategoryItems, the 'text' property can be null.
                    // To keep the linter quiet, we'll check that the 'text' property is not null.
                    // When we eventually switch to Jetpack Compose, this problem should disappear.
                    if (item.text == null) {
                        return
                    }
                    val count = get().getPrayerCountForCategory(item.text, item.language.code)
                    runOnUiThread(object : UiRunnable {
                        override fun run() {
                            h.prayerCount?.setText(String.format(item.language.locale, "%d", count))
                        }
                    })
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = items.get(position)
        return item.viewType
    }

    internal class CategoryHeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val language: TextView

        init {
            language = itemView.findViewById<TextView>(R.id.header_language)
        }
    }

    internal class CategoryItem(
        @field:LayoutRes @param:LayoutRes val viewType: Int,
        val language: Language,
        val text: String?
    )

    internal interface OnCategorySelectedListener {
        @UiThread
        fun onCategorySelected(category: String?, language: Language?)
    }
}
