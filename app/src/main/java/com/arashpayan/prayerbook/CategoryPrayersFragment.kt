package com.arashpayan.prayerbook

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arashpayan.prayerbook.App.Companion.runInBackground
import com.arashpayan.prayerbook.App.Companion.runOnUiThread
import com.arashpayan.prayerbook.database.PrayersDB.Companion.get
import com.arashpayan.prayerbook.thread.UiRunnable
import com.arashpayan.prayerbook.thread.WorkerRunnable
import com.arashpayan.util.DividerItemDecoration

class CategoryPrayersFragment : Fragment(), OnPrayerSelectedListener {
    private var category: String? = null
    private var adapter: CategoryPrayersAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var recyclerState: Parcelable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = getArguments()
        if (bundle == null) {
            throw RuntimeException("Fragment should be started via newInstance")
        }
        category = bundle.getString(ARG_CATEGORY, null)
        val language = getArguments()!!.getParcelable<Language?>(ARG_LANGUAGE)
        requireNotNull(category) { "You must provide a category" }
        requireNotNull(language) { "You must provide a language" }
        adapter = CategoryPrayersAdapter(language, this)
        runInBackground(object : WorkerRunnable {
            override fun run() {
                val ids = get().getPrayerIds(category, language)
                runOnUiThread(object : UiRunnable {
                    override fun run() {
                        adapter!!.setPrayerIds(ids)
                    }
                })
            }
        })
    }

    override fun onPause() {
        super.onPause()

        // We save our RecyclerView's state here, because onSaveInstanceState() doesn't get called
        // when your Fragments are just getting swapped within the same Activity.
        if (recyclerView != null) {
            val lm = recyclerView!!.getLayoutManager()
            if (lm != null) {
                recyclerState = lm.onSaveInstanceState()
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (recyclerState != null) {
            val lm = recyclerView!!.getLayoutManager()
            if (lm != null) {
                lm.onRestoreInstanceState(recyclerState)
            }
        }
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

        return recyclerView
    }

    override fun onResume() {
        super.onResume()

        val toolbar = requireActivity().findViewById<Toolbar?>(R.id.toolbar)
        if (toolbar != null) {
            toolbar.getMenu().clear()
            toolbar.setTitle(category)
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp)
            toolbar.setNavigationOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    getParentFragmentManager().popBackStack()
                }
            })
        }
    }

    override fun onPrayerSelected(prayerId: Long) {
        val intent = PrayerActivity.newIntent(requireContext(), prayerId)
        startActivity(intent)
    }

    companion object {
        const val TAG: String = "CategoryPrayers"
        private const val ARG_CATEGORY = "category"
        private const val ARG_LANGUAGE = "language"
        fun newInstance(category: String, language: Language): CategoryPrayersFragment {
            val fragment = CategoryPrayersFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            args.putParcelable(ARG_LANGUAGE, language)
            fragment.setArguments(args)

            return fragment
        }
    }
}
