package com.arashpayan.prayerbook

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.annotation.ColorInt
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.arashpayan.prayerbook.App.Companion.runInBackground
import com.arashpayan.prayerbook.App.Companion.runOnUiThread
import com.arashpayan.prayerbook.database.Prayer
import com.arashpayan.prayerbook.database.PrayersDB
import com.arashpayan.prayerbook.database.UserDB
import com.arashpayan.prayerbook.thread.UiRunnable
import com.arashpayan.prayerbook.thread.WorkerRunnable
import com.google.android.material.appbar.MaterialToolbar
import com.samskivert.mustache.Mustache
import java.io.InputStreamReader
import java.util.Locale
//import androidx.activity.OnBackPressedDispatcher.*
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
//import androidx.compose.ui.text.fromHtml
import androidx.core.text.HtmlCompat


class PrayerFragment : Fragment(), UserDB.Listener, MenuProvider {
    private var mWebView: WebView? = null
    private var prayer: Prayer? = null
    private var mScale = 1.0f
    private var prayerId: Long = 0
    private var bookmarkMenuItem: MenuItem? = null
    private var bookmarked = false

    private var tts: TextToSpeech? = null
    private var isTtsInitialized: Boolean = false
    private var isSpeaking: Boolean = false
    private var isPaused: Boolean = false
    private var currentSentenceIndex: Int = 0
    private var bookSentences: List<String> = emptyList()

    private var playButton: ImageButton? = null


    private val utteranceListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            runOnUiThread {
                if (utteranceId?.startsWith("BookSentence_") == true) {
                    isSpeaking = true // Mark as active
                    isPaused = false
                    updatePlayButtonIcon() // Update icon to "Stop"
                    //Log.d("TTS_BookViewer", "Speech started for $utteranceId")
                }
            }
        }

        override fun onDone(utteranceId: String?) {
            runOnUiThread {
                if (utteranceId?.startsWith("BookSentence_") == true) {
                    //Log.d("TTS_BookViewer", "Speech done for $utteranceId")
                     // Only proceed if we are still in an active, non-paused speaking state.
                    if (isSpeaking && !isPaused) {
                        currentSentenceIndex++
                        speakNextSentence()
                    }
                }
            }
        }

        @Deprecated("deprecated")
        override fun onError(utteranceId: String?) {
            runOnUiThread {
                if (utteranceId?.startsWith("BookSentence_") == true) {
                    stopSpeaking() // Use the new stop function
                    Log.e("TTS_BookViewer", "Speech error for $utteranceId")
                }
            }
        }
    }

    private fun initializeTts() {
        if (tts != null) return // Already initialized

        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {

                // --- START: LOG AVAILABLE VOICES ---

                val availableVoices = tts?.voices
                if (availableVoices.isNullOrEmpty()) {
                    //Log.d("TTS_BookViewer", "No voices found on this device.")
                } else {
                    //Log.d("TTS_BookViewer", "------------ AVAILABLE TTS VOICES (${availableVoices.size}) ------------")
                    for (voice in availableVoices) {
                        // Log details about each voice
                        if (voice.isNetworkConnectionRequired == false) {
                            //Log.d("TTS_BookViewer", "Voice: ${voice.name}, Lang: ${voice.locale}, Quality: ${voice.quality}, Requires Network: ${voice.isNetworkConnectionRequired}")
                        }
                    }
                    //Log.d("TTS_BookViewer", "----------------------------------------------------")
                }

                // --- END: LOG AVAILABLE VOICES ---


                isTtsInitialized = true
                //tts?.language = Locale.getDefault()

                val languageCode = Prefs.get().getLanguage()
                //Log.d("TTS_BookViewer", "Setting TTS language to: $languageCode")

                // Create a Locale with the retrieved language code
                val locale = Locale(languageCode)

                // Set the language on the TTS engine
                val result = tts?.setLanguage(locale)

                // Check if the language is supported
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Log.e("TTS_BookViewer", "Language '$languageCode' is not supported by the TTS engine.")
                } else {
                    //Log.d("TTS_BookViewer", "TTS language set successfully.")
                }

                tts?.setOnUtteranceProgressListener(utteranceListener)
                //Log.d("TTS_BookViewer", "TTS Initialization Success")

                if (Prefs.get().speakPrayerOnOpen) {
                    startSpeakingPrayer()
                }

            } else {
                // Log.e("TTS_BookViewer", "TTS Initialization Failed: Status code $status")
            }
        }
    }

    fun startSpeakingPrayer() {
        //Log.d("TTS_BookViewer", "Play button clicked. isSpeaking: $isSpeaking, isPaused: $isPaused")

        if (!isTtsInitialized) {        Log.w("TTS_BookViewer", "TTS not ready.")
            return
        }

        if (isSpeaking && !isPaused) {
            // --- PAUSE LOGIC ---
            // Currently speaking, so pause it.
            tts?.stop() // This stops the current utterance, but we'll remember our place.
            isPaused = true
            //Log.d("TTS_BookViewer", "Playback paused at sentence $currentSentenceIndex.")
            updatePlayButtonIcon()

        } else if (isPaused) {
            // --- RESUME LOGIC ---
            // Paused, so resume from the current sentence.
            isPaused = false
            //Log.d("TTS_BookViewer", "Playback resumed from sentence $currentSentenceIndex.")
            speakNextSentence() // This will also update the icon.

        } else {
            // --- PLAY FROM START LOGIC ---
            // Not speaking and not paused, so start from the beginning.
            if (prayer == null) {
                Log.w("TTS_BookViewer", "Prayer not loaded.")
                return
            }

            // Re-apply settings here in case they were just changed
            tts?.setSpeechRate(Prefs.get().getSpeechRate())
            tts?.setPitch(Prefs.get().getSpeechPitch())

            // Prepare sentences
            val prayerText = prayer?.text
            if (prayerText.isNullOrEmpty()) {
                // Log.w("TTS_BookViewer", "Prayer text is null or empty. Cannot speak.")
                return
            }

            // Prepare sentences (your existing code is good)
            val plainTextPrayer = HtmlCompat.fromHtml(prayerText, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            val fullPrayerText = "$plainTextPrayer\n\n - ${prayer!!.author}"
            bookSentences = fullPrayerText.split(Regex("(?<=[.!?])\\s*")).filter { it.isNotBlank() }

            if (bookSentences.isEmpty()) {
                // Log.w("TTS_BookViewer", "No sentences to speak.")
                return
            }

            // Start from the beginning
            currentSentenceIndex = 0
            speakNextSentence()
        }
    }

    private fun stopSpeaking() {
        if (tts != null) {
            tts?.stop()
        }
        isSpeaking = false
        isPaused = false
        currentSentenceIndex = 0
        updatePlayButtonIcon()
        //Log.d("TTS_BookViewer", "Playback stopped completely.")
    }

    @UiThread
    private fun updatePlayButtonIcon() {
        if (isSpeaking && !isPaused) {
            // Set the "Stop/Cancel" icon when speaking is active
            playButton?.setImageResource(R.drawable.text_to_speech_cancel_24px)
        } else {
            // Set the "Play" icon when not speaking or paused
            playButton?.setImageResource(R.drawable.text_to_speech_24px)
        }
    }

    private fun speakNextSentence() {
        if (!isTtsInitialized || tts == null) {
            // Log.e("TTS_BookViewer", "TTS engine is not available.")
            isSpeaking = false
            return
        }

        if (currentSentenceIndex < bookSentences.size) {
            val sentence = bookSentences[currentSentenceIndex]
            val utteranceId = "BookSentence_$currentSentenceIndex"
            val result = tts?.speak(sentence, TextToSpeech.QUEUE_ADD, null, utteranceId)
            if (result == TextToSpeech.ERROR) {
                // Log.e("TTS_BookViewer", "Error queuing sentence $currentSentenceIndex: '$sentence'")
                isSpeaking = false // Stop on error
            } else {
                //Log.d("TTS_BookViewer", "Queued sentence $currentSentenceIndex: '$sentence'")
            }
        } else {
            // All sentences have been spoken
            //Log.d("TTS_BookViewer", "All sentences spoken.")
            isSpeaking = false
            isPaused = false
            currentSentenceIndex = 0 // Reset for next time
            updatePlayButtonIcon()
        }
    }

    override fun onPause() {
        super.onPause()

        // --- Logic from your new TTS implementation ---
        stopSpeaking() // Use the new function to fully stop and reset state.

        // --- Logic from your original implementation ---
        requireActivity().window.decorView
            .systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        UserDB.get().removeListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Shutdown TTS completely to release resources
        tts?.shutdown()
        tts = null
        // ...
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = getArguments()
        if (arguments == null) {
            throw RuntimeException("Fragment should be created via newInstance")
        }
        prayerId = arguments.getLong(PRAYER_ID_ARGUMENT, -1)
        if (prayerId == -1L) {
            throw RuntimeException("You must provide a prayer id to this fragment")
        }
        runInBackground(object : WorkerRunnable {
            override fun run() {
                val p = PrayersDB.get().getPrayer(prayerId)
                runOnUiThread(object : UiRunnable {
                    override fun run() {
                        prayer = p
                        reloadPrayer()
                    }
                })
            }
        })
        mScale = Prefs.get().prayerTextScalar
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().getWindow().getDecorView()
            .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE)

        mWebView = WebView(requireContext())
        mWebView!!.getSettings().setSupportZoom(true)
        mWebView!!.setLayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        mWebView!!.setKeepScreenOn(true)
        reloadPrayer()

        return mWebView
    }

    private fun reloadPrayer() {
        if (prayer == null) {
            return
        }
        if (mWebView == null) {
            return
        }
        mWebView!!.loadDataWithBaseURL(null, this.prayerHTML!!, "text/html", "UTF-8", null)
        initializeTts()
    }

    override fun onResume() {
        super.onResume()
        updatePlayButtonIcon() // Make sure icon is correct when fragment is shown

        UserDB.get().addListener(this)
        // check if this prayer is bookmarked, and if so, change the icon color
        runInBackground(object : WorkerRunnable {
            override fun run() {
                // It's bookmarked, so we need to change the color
                bookmarked = UserDB.get().isBookmarked(prayerId)
                runOnUiThread(object : UiRunnable {
                    override fun run() {
                        updateBookmarkIconColor()
                    }
                })
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.prayer_toolbar)
        toolbar.setTitle("")
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp)

        // Create an ImageButton
        playButton = ImageButton(requireContext()).apply {
            // Set a default icon. onResume will correct it.
            setImageResource(R.drawable.text_to_speech_24px)

            // Remove the default background to make it look like a toolbar action
            setBackgroundResource(android.R.color.transparent)

            // Set the click listener to call your TTS function
            setOnClickListener {
                startSpeakingPrayer()
            }
        }

        // Define Layout Parameters to position the button
        val params = Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.END
            marginEnd = 24 * resources.displayMetrics.density.toInt() // 16dp
        }

        toolbar.addView(playButton, params)

        toolbar.setNavigationOnClickListener {
            (requireActivity() as? AppCompatActivity)?.onBackPressedDispatcher?.onBackPressed()
        }

        toolbar.addMenuProvider(this)
    }



    private val prayerHTML: String?
        get() {
            val pFontWidth = 1.1f * mScale
            val pFontHeight = 1.575f * mScale
            val pComment = 0.8f * mScale
            val authorWidth = 1.03f * mScale
            val authorHeight = 1.825f * mScale
            val versalWidth = 3.5f * mScale
            val versalHeight = 0.75f * mScale

            val args =
                HashMap<String?, String?>()
            args.put("fontWidth", String.format(Locale.US, "%f", pFontWidth))
            args.put("fontHeight", String.format(Locale.US, "%f", pFontHeight))
            args.put("commentSize", String.format(Locale.US, "%f", pComment))
            args.put("authorWidth", String.format(Locale.US, "%f", authorWidth))
            args.put("authorHeight", String.format(Locale.US, "%f", authorHeight))
            args.put("versalWidth", String.format(Locale.US, "%f", versalWidth))
            args.put("versalHeight", String.format(Locale.US, "%f", versalHeight))
            val useClassicTheme = Prefs.get().useClassicTheme()
            val bgColor: String?
            val textColor: String?
            val commentColor: String?
            val versalAndAuthorColor: String?
            val font: String?
            val italicOrNothing: String?
            if (useClassicTheme) {
                bgColor = "#D6D2C9"
                textColor = "#333333"
                commentColor = "#444433"
                versalAndAuthorColor = "#992222"
                font = "Georgia"
                italicOrNothing = "italic"
            } else {
                val isDark =
                    (getResources().getConfiguration().uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                if (isDark) {
                    bgColor = "#000000"
                    textColor = "#cccccc"
                    commentColor = "#ddddcc"
                } else {
                    bgColor = "#ffffff"
                    textColor = "#333333"
                    commentColor = "#444433"
                }
                versalAndAuthorColor = "#33b5e5"
                font = "sans-serif"
                italicOrNothing = ""
            }
            args.put("backgroundColor", bgColor)
            args.put("textColor", textColor)
            args.put("commentColor", commentColor)
            args.put("versalAndAuthorColor", versalAndAuthorColor)
            args.put("font", font)
            args.put("italicOrNothing", italicOrNothing)
            args.put("prayer", prayer!!.text)
            args.put("author", prayer!!.author)

            if (prayer!!.citation!!.isEmpty()) {
                args.put("citation", "")
            } else {
                val citationHTML =
                    String.format("<p class=\"comment\"><br/><br/>%s</p>", prayer!!.citation)
                args.put("citation", citationHTML)
            }

            if (prayer!!.language!!.rightToLeft) {
                args.put("layoutDirection", "rtl")
            } else {
                args.put("layoutDirection", "ltr")
            }

            val `is` = getResources().openRawResource(R.raw.prayer_template)
            val isr = InputStreamReader(`is`)

            return Mustache.compiler().escapeHTML(false).compile(isr).execute(args)
        }

    private val prayerText: String
        get() = prayer!!.searchText + "\n\n" + prayer!!.author

    private fun printPrayer() {
        if (mWebView == null) {
            // shouldn't happen, but just in case
            return
        }

        val manager = requireContext().getSystemService(Context.PRINT_SERVICE) as PrintManager
        if (manager == null) {
            throw RuntimeException("Where's the print manager?")
        }
        val adapter = mWebView!!.createPrintDocumentAdapter("Prayer")

        val jobName = getString(R.string.app_name) + " " + getString(R.string.document)
        manager.print(jobName, adapter, PrintAttributes.Builder().build())
    }

    @UiThread
    private fun updateBookmarkIconColor() {
        if (bookmarkMenuItem == null) {
            return
        }
        @ColorInt val color: Int
        if (bookmarked) {
            color = ContextCompat.getColor(requireContext(), R.color.prayer_book_accent)
        } else {
            color = Color.WHITE
        }
        val icon = bookmarkMenuItem!!.getIcon()
        if (icon != null) { // it should never be null
            DrawableCompat.setTint(icon, color)
        }
    }

    @UiThread
    private fun toggleBookmark() {
        runInBackground(object : WorkerRunnable {
            override fun run() {
                val db = UserDB.get()
                val isBookmarked = db.isBookmarked(prayerId)
                if (isBookmarked) {
                    db.deleteBookmark(prayerId)
                } else {
                    db.addBookmark(prayerId)
                }
            }
        })
    }

    //region UserDB.Listener
    override fun onBookmarkAdded(bookmarkPrayerId: Long) {
        if (bookmarkPrayerId != prayerId) {
            return
        }
        bookmarked = true
        updateBookmarkIconColor()
    }

    override fun onBookmarkDeleted(bookmarkPrayerId: Long) {
        if (bookmarkPrayerId != prayerId) {
            return
        }
        bookmarked = false
        updateBookmarkIconColor()
    }

    //region MenuProvider
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.prayer, menu)

        // set the current value for classic theme
        menu.findItem(R.id.action_classic_theme).setChecked(Prefs.get().useClassicTheme())

        bookmarkMenuItem = menu.findItem(R.id.action_toggle_bookmark)
        updateBookmarkIconColor()
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        val prefs = Prefs.get()
        // .75 to 1.60
        val itemId = item.getItemId()
        if (itemId == R.id.action_increase_text_size) {
            if (mScale < 1.6f) {
                mScale += 0.05f
                prefs.prayerTextScalar = mScale
                reloadPrayer()
            }
        } else if (itemId == R.id.action_decrease_text_size) {
            if (mScale > .75) {
                mScale -= 0.05f
                prefs.prayerTextScalar = mScale
                reloadPrayer()
            }
        } else if (itemId == R.id.action_classic_theme) {
            val useClassic = !item.isChecked() // toggle the value
            item.setChecked(useClassic)
            prefs.setUseClassicTheme(useClassic)
            reloadPrayer()
        } else if (itemId == R.id.action_share_prayer) {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.setType("text/plain")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, this.prayerText)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        } else if (itemId == R.id.action_print_prayer) {
            printPrayer()
        } else if (itemId == R.id.action_toggle_bookmark) {
            toggleBookmark()
        } else if (itemId == R.id.action_speech_settings){
            SpeechSettingsDialogFragment()
                .show(parentFragmentManager, SpeechSettingsDialogFragment.TAG)
            return true
        } else {
            // unexpected id
            return false
        }

        return true
    } //endregion

    companion object {
        private const val PRAYER_ID_ARGUMENT = "PrayerId"

        fun newInstance(prayerId: Long): PrayerFragment {
            val fragment = PrayerFragment()
            val args = Bundle()
            args.putLong(PRAYER_ID_ARGUMENT, prayerId)
            fragment.setArguments(args)
            return fragment
        }
    }
}