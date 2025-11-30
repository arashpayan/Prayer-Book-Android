package com.arashpayan.prayerbook

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.arashpayan.prayerbook.databinding.FragmentAboutBinding
import com.arashpayan.util.L
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringWriter
import java.nio.charset.StandardCharsets

class AboutFragment : Fragment() {
    private var binding: FragmentAboutBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentAboutBinding>(
            inflater,
            R.layout.fragment_about,
            container,
            false
        )
        loadAboutHtml()
        binding!!.webview.setWebViewClient(WVC())

        return binding!!.getRoot()
    }

    override fun onResume() {
        super.onResume()

        val toolbar = requireActivity().findViewById<Toolbar?>(R.id.toolbar)
        if (toolbar != null) {
            toolbar.getMenu().clear()
            toolbar.setTitle(getString(R.string.about))
            toolbar.setNavigationIcon(null)
        }
    }

    //region Business logic
    @Throws(IOException::class)
    private fun getStringFromInputStream(inputStream: InputStream?): String {
        val reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
        val buffer = CharArray(8)
        val sw = StringWriter()
        var numRead: Int
        while ((reader.read(buffer, 0, buffer.size).also { numRead = it }) != -1) {
            sw.write(buffer, 0, numRead)
        }

        return sw.toString()
    }

    private fun loadAboutHtml() {
        try {
            val html = getStringFromInputStream(getResources().openRawResource(R.raw.about))
            binding!!.webview.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        } catch (ex: IOException) {
            L.w("Unable to open 'about' html", ex)
        }
    }

    //endregion
    //region WebViewClient
    private inner class WVC : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)

            return true
        }
    } //endregion

    companion object {
        const val TAG: String = "about"

        @JvmStatic
        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }
}
