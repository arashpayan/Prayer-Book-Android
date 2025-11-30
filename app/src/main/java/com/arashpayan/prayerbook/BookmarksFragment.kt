package com.arashpayan.prayerbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arashpayan.prayerbook.App.Companion.runInBackground
import com.arashpayan.prayerbook.App.Companion.runOnUiThread
import com.arashpayan.prayerbook.database.UserDB
import com.arashpayan.prayerbook.database.UserDB.Companion.get
import com.arashpayan.prayerbook.thread.UiRunnable
import com.arashpayan.prayerbook.thread.WorkerRunnable

class BookmarksFragment : Fragment(), UserDB.Listener {
    private var adapter: BookmarksAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = BookmarksAdapter(prayerSelectedListener)

        get().addListener(this)

        runInBackground(object : WorkerRunnable {
            override fun run() {
                val bookmarks = get().bookmarks
                runOnUiThread(object : UiRunnable {
                    override fun run() {
                        adapter!!.setBookmarks(bookmarks)
                    }
                })
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val recyclerView =
            inflater.inflate(R.layout.recycler_view, container, false) as RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
        recyclerView.setAdapter(adapter)

        return recyclerView
    }

    override fun onDestroy() {
        get().removeListener(this)

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        val toolbar = requireActivity().findViewById<Toolbar?>(R.id.toolbar)
        if (toolbar != null) {
            toolbar.getMenu().clear()
            toolbar.setTitle(R.string.bookmarks)
            toolbar.setNavigationIcon(null)
        }
    }

    //region UserDB.Listener
    override fun onBookmarkAdded(prayerId: Long) {
        adapter!!.bookmarkAdded(prayerId)
    }

    override fun onBookmarkDeleted(prayerId: Long) {
        adapter!!.bookmarkDeleted(prayerId)
    }

    //endregion
    private val prayerSelectedListener: OnPrayerSelectedListener =
        object : OnPrayerSelectedListener {
            override fun onPrayerSelected(prayerId: Long) {
                val intent = PrayerActivity.newIntent(requireContext(), prayerId)
                startActivity(intent)
            }
        }

    companion object {
        const val TAG: String = "bookmarks"

        @JvmStatic
        fun newInstance(): BookmarksFragment {
            return BookmarksFragment()
        }
    }
}
