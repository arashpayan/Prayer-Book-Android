/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.arashpayan.util.L;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

// ****************** new code ************************
import java.util.ArrayList;
// ****************** end new code ************************


/**
 *
 * @author arash
 */
@SuppressWarnings("WeakerAccess")
public class Prefs {
    private static volatile Prefs singleton = null;
    private final SharedPreferences mPrefs;
    private final Set<Listener> listeners = new HashSet<>();
    
    private static final String PREFERENCES_FILE_NAME = "PrayerBookPreferences";
    private static final String PREFERENCE_DATABASE_VERSION = "DatabaseVersion";
    private static final String PREFERENCE_PRAYER_TEXT_SCALAR = "PrayerTextScalar";
    private static final String PREFERENCE_USE_CLASSIC_THEME = "UseClassicTheme";

    // ****************** new code ************************
    private static final String PREFERENCE_SPEAK_PRAYER_ON_OPEN = "speak_prayer_on_open";
    private static final String PREFERENCE_SPEECH_RATE = "speech_rate";
    private static final String PREFERENCE_SPEECH_PITCH = "speech_pitch";
    private static final String PREFERENCE_SPEECH_VOICE = "speech_voice";
    private static final float DEFAULT_SPEECH_RATE = 1.0f;
    private static final float DEFAULT_SPEECH_PITCH = 1.0f;
    // ****************** end new code ************************


    private Prefs(@NonNull Context ctx) {
        mPrefs = ctx.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }

    public static void init(@NonNull Application app) {
        singleton = new Prefs(app);
    }

    @NonNull
    public static Prefs get() {
        return singleton;
    }

    public int getDatabaseVersion() {
        return mPrefs.getInt(PREFERENCE_DATABASE_VERSION, 0);
    }

    @NonNull
    public Language[] getEnabledLanguages() {
        LinkedList<Language> langs = new LinkedList<>();
        for (Language l : Language.values()) {
            if (isLanguageEnabled(l)) {
                langs.add(l);
            }
        }

        if (langs.isEmpty()) {
            // find the user's locale and see if it matches any of the known languages
            Locale defaultLocale = Locale.getDefault();
            String langCode = defaultLocale.getLanguage();
            for (Language l : Language.values()) {
                if (langCode.startsWith(l.code)) {
                    langs.add(l);
                }
            }
        }

        // if it's still empty, just enable English
        if (langs.isEmpty()) {
            langs.add(Language.English);
        }

        return langs.toArray(new Language[0]);
    }
    
    public void setDatabaseVersion(int version) {
        mPrefs.edit().putInt(PREFERENCE_DATABASE_VERSION, version).apply();
    }
    
    public boolean isLanguageEnabled(Language lang) {
        return mPrefs.getBoolean(lang.code + "_enabled", false);
    }

    @UiThread
    public void setLanguageEnabled(Language lang, boolean shouldEnable) {
        mPrefs.edit().putBoolean(lang.code + "_enabled", shouldEnable).apply();

        notifyEnabledLanguagesChanged();
    }
    
    public float getPrayerTextScalar() {
        return mPrefs.getFloat(PREFERENCE_PRAYER_TEXT_SCALAR, 1.0f);
    }
    
    public void setPrayerTextScalar(float scalar) {
        mPrefs.edit().putFloat(PREFERENCE_PRAYER_TEXT_SCALAR, scalar).apply();
    }

    public boolean useClassicTheme() {
        return mPrefs.getBoolean(PREFERENCE_USE_CLASSIC_THEME, false);
    }

    public void setUseClassicTheme(boolean useClassicTheme) {
        mPrefs.edit().putBoolean(PREFERENCE_USE_CLASSIC_THEME, useClassicTheme).apply();
    }

    //region Listener

    interface Listener {
        @UiThread
        void onEnabledLanguagesChanged();
    }

    @UiThread
    void addListener(@NonNull Listener l) {
        listeners.add(l);
    }

    @UiThread
    private void notifyEnabledLanguagesChanged() {
        for (Listener l : listeners) {
            try {
                l.onEnabledLanguagesChanged();
            } catch (Throwable t) {
                //L.w("Error notifying listener", t);
            }
        }
    }

    @UiThread
    void removeListener(@NonNull Listener l) {
        listeners.remove(l);
    }

    //endregion


    // ****************** new code ************************

    public String getLanguage() {
        Language[] enabled = getEnabledLanguages();
        if (enabled.length != 0 && !enabled[0].code.isEmpty()) {
            return enabled[0].code;
        }
        return Language.English.code;
    }

    public void setSpeechRate(float rate) {
        mPrefs.edit().putFloat(PREFERENCE_SPEECH_RATE, rate).apply();
    }

    public float getSpeechRate() {
        return mPrefs.getFloat(PREFERENCE_SPEECH_RATE, DEFAULT_SPEECH_RATE);
    }

    public void setSpeechPitch(float pitch) {
        mPrefs.edit().putFloat(PREFERENCE_SPEECH_PITCH, pitch).apply();
    }

    public float getSpeechPitch() {
        return mPrefs.getFloat(PREFERENCE_SPEECH_PITCH, DEFAULT_SPEECH_PITCH);
    }

    public void setSpeechVoice(String voiceName) {
        mPrefs.edit().putString(PREFERENCE_SPEECH_VOICE, voiceName).apply();
    }

    public String getSpeechVoice() {
        return mPrefs.getString(PREFERENCE_SPEECH_VOICE, null);
    }

    public boolean getSpeakPrayerOnOpen() {
        return mPrefs.getBoolean(PREFERENCE_SPEAK_PRAYER_ON_OPEN, false);
    }

    public void setSpeakPrayerOnOpen(boolean value) {
        mPrefs.edit().putBoolean(PREFERENCE_SPEAK_PRAYER_ON_OPEN, value).apply();
    }

    // ****************** end new code ************************

}
