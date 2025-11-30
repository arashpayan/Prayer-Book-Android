package com.arashpayan.prayerbook

import android.R.attr.value
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.UiThread
import com.arashpayan.util.L
//import java.util.LinkedList
import java.util.Locale
import kotlin.concurrent.Volatile

class Prefs private constructor(ctx: Context) {
    private val mPrefs: SharedPreferences
    private val listeners: MutableSet<Listener> = HashSet<Listener>()

    init {
        mPrefs = ctx.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    }

    var databaseVersion: Int
        get() = mPrefs.getInt(PREFERENCE_DATABASE_VERSION, 0)
        set(version) {
            mPrefs.edit().putInt(PREFERENCE_DATABASE_VERSION, version).apply()
        }

    var prayerTextScalar: Float
        get() = mPrefs.getFloat(PREFERENCE_PRAYER_TEXT_SCALAR, 1.0F)
        set(version) {
            mPrefs.edit().putFloat(PREFERENCE_PRAYER_TEXT_SCALAR, value.toFloat()).apply()
        }

    val enabledLanguages: ArrayList<Language> // Changed type to ArrayList<Language>
        get() {
            // Use ArrayList directly instead of LinkedList
            val langs = ArrayList<Language>()
            for (l in Language.values()) {
                if (isLanguageEnabled(l)) {
                    langs.add(l)
                }
            }

            if (langs.isEmpty()) {
                // find the user's locale and see if it matches any of the known languages
                val defaultLocale = Locale.getDefault()
                val langCode = defaultLocale.language // Use .language for consistency
                for (l in Language.values()) {
                    // Make sure to use the language code property (e.g., l.code)
                    if (langCode.startsWith(l.code)) {
                        langs.add(l)
                    }
                }
            }

            // if it's still empty, just enable English
            if (langs.isEmpty()) {
                // Use the correct enum name: Language.English
                langs.add(Language.English)
            }

            return langs
        }

    fun isLanguageEnabled(lang: Language): Boolean {
        return mPrefs.getBoolean(lang.code + "_enabled", false)
    }

    @UiThread
    fun setLanguageEnabled(lang: Language, shouldEnable: Boolean) {
        mPrefs.edit().putBoolean(lang.code + "_enabled", shouldEnable).apply()

        notifyEnabledLanguagesChanged()
    }

    fun useClassicTheme(): Boolean {
        return mPrefs.getBoolean(PREFERENCE_USE_CLASSIC_THEME, false)
    }

    fun setUseClassicTheme(useClassicTheme: Boolean) {
        mPrefs.edit().putBoolean(PREFERENCE_USE_CLASSIC_THEME, useClassicTheme).apply()
    }


    /**
     * Returns the language code of the first enabled language.
     * Defaults to the user's locale if it matches an available language,
     * otherwise falls back to English ("en").
     */
    fun getLanguage(): String {
        val enabled = enabledLanguages
        if (enabled.isNotEmpty()) {
            // Return the code of the first enabled language
            return enabled[0].code
        }

        // As a fallback, this should not happen since enabledLanguages always returns at least one
        return Language.English.code
    }

    fun setSpeechRate(rate: Float) {
        mPrefs.edit().putFloat(PREFERENCE_SPEECH_RATE, rate).apply()
    }

    fun getSpeechRate(): Float {
        return mPrefs.getFloat(PREFERENCE_SPEECH_RATE, DEFAULT_SPEECH_RATE)
    }

    fun setSpeechPitch(pitch: Float) {
        mPrefs.edit().putFloat(PREFERENCE_SPEECH_PITCH, pitch).apply()
    }

    fun getSpeechPitch(): Float {
        return mPrefs.getFloat(PREFERENCE_SPEECH_PITCH, DEFAULT_SPEECH_PITCH)
    }

    var speakPrayerOnOpen: Boolean
        get() = mPrefs.getBoolean(PREFERENCE_SPEAK_PRAYER_ON_OPEN, false)
        set(value) {
            mPrefs.edit().putBoolean(PREFERENCE_SPEAK_PRAYER_ON_OPEN, value).apply()
        }

    //region Listener
    interface Listener {
        @UiThread
        fun onEnabledLanguagesChanged()
    }

    @UiThread
    fun addListener(l: Listener) {
        listeners.add(l)
    }

    @UiThread
    private fun notifyEnabledLanguagesChanged() {
        for (l in listeners) {
            try {
                l.onEnabledLanguagesChanged()
            } catch (t: Throwable) {
                L.w("Error notifying listener", t)
            }
        }
    }

    @UiThread
    fun removeListener(l: Listener) {
        listeners.remove(l)
    } //endregion

    companion object {
        @Volatile
        private var singleton: Prefs? = null
        private const val PREFERENCES_FILE_NAME = "PrayerBookPreferences"
        private const val PREFERENCE_DATABASE_VERSION = "DatabaseVersion"

        private const val PREFERENCE_PRAYER_TEXT_SCALAR = "PrayerTextScalar"
        private const val PREFERENCE_USE_CLASSIC_THEME = "UseClassicTheme"
        private const val PREFERENCE_SPEAK_PRAYER_ON_OPEN = "speak_prayer_on_open"

        private const val PREFERENCE_SPEECH_RATE = "speech_rate"
        private const val PREFERENCE_SPEECH_PITCH = "speech_pitch"
        private const val DEFAULT_SPEECH_RATE = 1.0f
        private const val DEFAULT_SPEECH_PITCH = 1.0f

        fun init(app: Application) {
            singleton = Prefs(app)
        }

        @JvmStatic
        fun get(): Prefs {
            return singleton!!
        }
    }
}
