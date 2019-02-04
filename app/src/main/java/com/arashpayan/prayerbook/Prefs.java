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
                L.w("Error notifying listener", t);
            }
        }
    }

    @UiThread
    void removeListener(@NonNull Listener l) {
        listeners.remove(l);
    }

    //endregion
}
