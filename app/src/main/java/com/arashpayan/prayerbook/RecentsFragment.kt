package com.arashpayan.prayerbook

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arashpayan.prayerbook.App.Companion.runInBackground
import com.arashpayan.prayerbook.App.Companion.runOnUiThread
import com.arashpayan.prayerbook.database.UserDB
import com.arashpayan.prayerbook.database.UserDB.Companion.get
import com.arashpayan.prayerbook.thread.UiRunnable
import com.arashpayan.prayerbook.thread.WorkerRunnable
import com.arashpayan.util.DividerItemDecoration
import com.arashpayan.util.L

class RecentsFragment : Fragment(), UserDB.Listener {
    private var adapter: RecentsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        L.i("Recents.onCreate")
        super.onCreate(savedInstanceState)

        adapter = RecentsAdapter(prayerSelectionListener)
        runInBackground(object : WorkerRunnable {
            override fun run() {
                val recents = get().recents
                runOnUiThread(object : UiRunnable {
                    override fun run() {
                        adapter!!.setRecentIds(recents)
                    }
                })
            }
        })

        get().addListener(this)
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
            toolbar.inflateMenu(R.menu.recents)
            toolbar.setTitle(getString(R.string.recents))
            toolbar.setNavigationIcon(null)
            toolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    if (item.getItemId() == R.id.action_clear_all) {
                        clearRecentsAction()
                        return true
                    }

                    return false
                }
            })
        }
    }

    //region Business logic
    private fun clearRecentsAction() {
        val bldr = AlertDialog.Builder(
            requireContext(),
            com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog_Alert
        )
        bldr.setMessage(getString(R.string.clear_recents_interrogative))
        bldr.setPositiveButton(R.string.clear, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                get().clearRecents()
                // We'll receive a call back on our listener method that will cause us to update
                // the UI.
            }
        })
        bldr.setNegativeButton(R.string.no, null)
        bldr.setCancelable(true)
        bldr.show()
    }
    //endregion

    private val prayerSelectionListener: OnPrayerSelectedListener =
        object : OnPrayerSelectedListener {
            override fun onPrayerSelected(prayerId: Long) {
                val intent = PrayerActivity.newIntent(requireContext(), prayerId)
                startActivity(intent)
            }
        }

    //region Recents listener
    override fun onPrayerAccessed(prayerId: Long) {
        adapter!!.onPrayerAccessed(prayerId)
    }

    override fun onRecentsCleared() {
        adapter!!.clearAll()
    } //endregion

    companion object {
        const val TAG: String = "recents"

        @JvmStatic
        fun newInstance(): RecentsFragment {
            return RecentsFragment()
        }
    }
}
