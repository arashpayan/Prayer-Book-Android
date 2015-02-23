/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.LinkedList;
import java.util.Locale;

/**
 *
 * @author arash
 */
public class Preferences {
    private static volatile Preferences mSingleton = null;
    private SharedPreferences mPrefs = null;
    
    private static final String PREFERENCES_FILE_NAME = "PrayerBookPreferences";
    private static final String PREFERENCE_DATABASE_VERSION = "DatabaseVersion";
    private static final String PREFERENCE_PRAYER_TEXT_SCALAR = "PrayerTextScalar";
    private static final String PREFERENCE_USE_CLASSIC_THEME = "UseClassicTheme";
    
    private Preferences(Context ctx) {
        mPrefs = ctx.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }
    
    public static Preferences getInstance(Application app) {
        if (mSingleton == null) {
            synchronized (Preferences.class) {
                if (mSingleton == null) {
                    mSingleton = new Preferences(app.getApplicationContext());
                }
            }
        }
        
        return mSingleton;
    }

    public int getDatabaseVersion() {
        return mPrefs.getInt(PREFERENCE_DATABASE_VERSION, 0);
    }

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

        return langs.toArray(new Language[langs.size()]);
    }
    
    public void setDatabaseVersion(int version) {
        mPrefs.edit().putInt(PREFERENCE_DATABASE_VERSION, version).apply();
    }
    
    public boolean isLanguageEnabled(Language lang) {
        return mPrefs.getBoolean(lang.code + "_enabled", false);
    }
    
    public void setLanguageEnabled(Language lang, boolean shouldEnable) {
        mPrefs.edit().putBoolean(lang.code + "_enabled", shouldEnable).apply();
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
}
