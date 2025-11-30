package com.arashpayan.prayerbook

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
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

class SearchFragment : Fragment(), OnPrayerSelectedListener, TextWatcher {
    private var mSearchAdapter: SearchAdapter? = null
    private var mSearchView: View? = null
    private var mClearButton: ImageButton? = null
    private var mQuery: CharSequence? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSearchAdapter = SearchAdapter()
        mSearchAdapter!!.setListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val recyclerView =
            inflater.inflate(R.layout.recycler_view, container, false) as RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
        val llm = LinearLayoutManager(requireContext())
        llm.setOrientation(RecyclerView.VERTICAL)
        recyclerView.setLayoutManager(llm)
        recyclerView.setAdapter(mSearchAdapter)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        if (toolbar == null) {
            throw RuntimeException("Where's the toolbar?")
        }
        toolbar.getMenu().clear()

        mSearchView = inflater.inflate(R.layout.search_field, toolbar, false)
        val searchField = mSearchView!!.findViewById<EditText>(R.id.search_field)
        searchField.addTextChangedListener(this)
        mClearButton = mSearchView!!.findViewById<ImageButton>(R.id.clear_button)
        mClearButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                searchField.setText("")
            }
        })

        toolbar.addView(mSearchView)

        return recyclerView
    }

    override fun onDestroyView() {
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.removeView(mSearchView)

        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(null)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp)
        toolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                if (imm == null) {
                    // should never happen
                    return
                }
                imm.hideSoftInputFromWindow(toolbar.getWindowToken(), 0)
                getParentFragmentManager().popBackStack()
            }
        })
        // if there's no query saved, then show the keyboard
        if (mQuery == null) {
            val edit = mSearchView!!.findViewById<View>(R.id.search_field)
            edit.requestFocus()
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            if (imm == null) {
                // should never happen
                return
            }
            imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putCharSequence("query", mQuery)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            mQuery = savedInstanceState.getCharSequence("query", null)
        }

        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        val bottomBar = requireActivity().findViewById<View?>(R.id.bottom_bar)
        if (bottomBar != null) {
            bottomBar.setVisibility(View.GONE)
        }
    }

    override fun onStop() {
        val bottomBar = requireActivity().findViewById<View?>(R.id.bottom_bar)
        if (bottomBar != null) {
            bottomBar.setVisibility(View.VISIBLE)
        }

        super.onStop()
    }

    override fun onPrayerSelected(prayerId: Long) {
        // the keyboard might still be present, so dismiss it
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        if (imm == null) {
            // should never happen
            return
        }
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        if (toolbar == null) {
            throw RuntimeException("where's the toolbar?")
        }
        imm.hideSoftInputFromWindow(toolbar.getWindowToken(), 0)

        val intent = PrayerActivity.newIntent(requireContext(), prayerId)
        startActivity(intent)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        mQuery = s
        if (mQuery!!.length > 0) {
            mClearButton!!.setVisibility(View.VISIBLE)
        } else {
            mClearButton!!.setVisibility(View.INVISIBLE)
        }

        val trimmed = s.toString().trim { it <= ' ' }
        if (trimmed.length < 3) {
            mSearchAdapter!!.setCursor(null)
            return
        }

        val prefs = Prefs.get()
        runInBackground(object : WorkerRunnable {
            override fun run() {
                val keywords: Array<String> =
                    trimmed.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val c = get().getPrayersWithKeywords(keywords, prefs.enabledLanguages)
                    runOnUiThread(object : UiRunnable {
                        override fun run() {
                            mSearchAdapter!!.setCursor(c)
                        }
                    })
            }
        })
    }

    override fun afterTextChanged(s: Editable?) {}

    companion object {
        const val SEARCHPRAYERS_TAG: String = "search_prayers"
    }
}
