package com.arashpayan.prayerbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arashpayan.util.DividerItemDecoration

class LanguagesFragment : Fragment() {
    private var adapter: LanguagesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = LanguagesAdapter()
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

    internal class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val language: TextView?
        val checkBox: CheckBox?

        init {
            language = itemView.findViewById<TextView?>(R.id.language)
            checkBox = itemView.findViewById<CheckBox?>(R.id.checkbox)
        }
    }

    override fun onResume() {
        super.onResume()

        val toolbar = requireActivity().findViewById<Toolbar?>(R.id.toolbar)
        if (toolbar != null) {
            toolbar.getMenu().clear()
            toolbar.setTitle(getString(R.string.languages))
            toolbar.setNavigationIcon(null)
        }
    }


    companion object {
        const val TAG: String = "languages"

        @JvmStatic
        fun newInstance(): LanguagesFragment {
            return LanguagesFragment()
        }
    }
}
