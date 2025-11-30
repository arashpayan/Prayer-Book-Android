package com.arashpayan.prayerbook

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arashpayan.prayerbook.App.Companion.runInBackground
import com.arashpayan.prayerbook.App.Companion.runOnUiThread
import com.arashpayan.prayerbook.CategoriesAdapter.OnCategorySelectedListener
import com.arashpayan.prayerbook.thread.UiRunnable
import com.arashpayan.prayerbook.thread.WorkerRunnable
import com.arashpayan.util.DividerItemDecoration
import com.google.android.material.appbar.MaterialToolbar

class CategoriesFragment : Fragment(), OnCategorySelectedListener, Prefs.Listener, MenuProvider {
    private var mRecyclerState: Parcelable? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: CategoriesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            mRecyclerState = savedInstanceState.getParcelable<Parcelable?>("recycler_state")
        }

        runInBackground(object : WorkerRunnable {
            override fun run() {
                val a = CategoriesAdapter(Prefs.get().enabledLanguages, this@CategoriesFragment)
                runOnUiThread(object : UiRunnable {
                    override fun run() {
                        adapter = a
                        if (recyclerView != null) {
                            recyclerView!!.setAdapter(adapter)
                        }
                    }
                })
            }
        })

        Prefs.get().addListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        recyclerView = inflater.inflate(R.layout.recycler_view, container, false) as RecyclerView?
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
        recyclerView!!.setAdapter(adapter)

        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.removeMenuProvider(this)
        toolbar.addMenuProvider(this)

        return recyclerView
    }

    override fun onDestroy() {
        super.onDestroy()

        Prefs.get().removeListener(this)
    }

    override fun onPause() {
        super.onPause()

        // We save our RecyclerView's state here, because onSaveInstanceState() doesn't get called
        // when your Fragments are just getting swapped within the same Activity.
        if (recyclerView != null) {
            val lm = recyclerView!!.getLayoutManager()
            if (lm != null) {
                mRecyclerState = lm.onSaveInstanceState()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (recyclerView != null) {
            val lm = recyclerView!!.getLayoutManager()
            if (lm != null) {
                val state = lm.onSaveInstanceState()
                outState.putParcelable("recycler_state", state)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val toolbar = requireActivity().findViewById<Toolbar?>(R.id.toolbar)
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (mRecyclerState != null) {
            val lm = recyclerView!!.getLayoutManager()
            if (lm != null) {
                lm.onRestoreInstanceState(mRecyclerState)
            }
        }
    }

    override fun onCategorySelected(category: String?, language: Language?) {

        if (category == null || language == null) {
            return
        }

        val fragment = CategoryPrayersFragment.newInstance(category, language)

        val ft = getParentFragmentManager().beginTransaction()
        ft.replace(R.id.main_container, fragment, CategoryPrayersFragment.TAG)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun onSearch() {
        val sf = SearchFragment()
        val ft = getParentFragmentManager().beginTransaction()
        ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
        ft.replace(R.id.main_container, sf, SearchFragment.SEARCHPRAYERS_TAG)
        ft.addToBackStack(null)
        ft.commit()
    }

    override fun onEnabledLanguagesChanged() {
        if (recyclerView == null) {
            return
        }

        runInBackground(object : WorkerRunnable {
            override fun run() {
                 val a = CategoriesAdapter(Prefs.get().enabledLanguages, this@CategoriesFragment)
                runOnUiThread(object : UiRunnable {
                    override fun run() {
                        adapter = a
                        if (recyclerView != null) {
                            recyclerView!!.setAdapter(adapter)
                        }
                    }
                })
            }
        })
    }

    //region MenuProvider
    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.categories, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == R.id.search_prayers) {
            onSearch()
            return true
        } else if (item.getItemId() == R.id.action_speech_settings) {
            SpeechSettingsDialogFragment()
                .show(parentFragmentManager, SpeechSettingsDialogFragment.TAG)
            return true
        }

        return false
    } //endregion

    companion object {
        const val TAG: String = "prayers"

        @JvmStatic
        fun newInstance(): CategoriesFragment {
            return CategoriesFragment()
        }
    }
}
