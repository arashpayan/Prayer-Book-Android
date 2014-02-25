/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 *
 * @author arash
 */
public class Preferences {
    private static volatile Preferences mSingleton = null;
    private SharedPreferences mPrefs = null;
    
    private static final String PREFERENCES_FILE_NAME = "PrayerBookPreferences";
    
    private static final String PREFERENCE_DATABASE_VERSION = "DatabaseVersion";

    // language preferences
    private static final String PREFERENCE_ENGLISH_PRAYERS = "EnglishPrayers";
    private static final String PREFERENCE_SPANISH_PRAYERS = "SpanishPrayers";
    private static final String PREFERENCE_PERSIAN_PRAYERS = "PersianPrayers";
    private static final String PREFERENCE_FRENCH_PRAYERS = "FrenchPrayers";
    private static final String PREFERENCE_DUTCH_PRAYERS = "DutchPrayers";
    
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

    public boolean isDutchEnabled() {
        return mPrefs.getBoolean(PREFERENCE_DUTCH_PRAYERS, false);
    }

    public boolean isEnglishEnabled() {
        return mPrefs.getBoolean(PREFERENCE_ENGLISH_PRAYERS, false);
    }

    public boolean isFrenchEnabled() {
        return mPrefs.getBoolean(PREFERENCE_FRENCH_PRAYERS, false);
    }

    public boolean isPersianEnabled() {
        return mPrefs.getBoolean(PREFERENCE_PERSIAN_PRAYERS, false);
    }

    public boolean isSpanishEnabled() {
        return mPrefs.getBoolean(PREFERENCE_SPANISH_PRAYERS, false);
    }
    
    public void setDatabaseVersion(int version) {
        mPrefs.edit().putInt(PREFERENCE_DATABASE_VERSION, version).apply();
    }

    public void setDutchEnabled(boolean shouldEnable) {
        mPrefs.edit().putBoolean(PREFERENCE_DUTCH_PRAYERS, shouldEnable).apply();
    }

    public void setEnglishEnabled(boolean shouldEnable) {
        mPrefs.edit().putBoolean(PREFERENCE_ENGLISH_PRAYERS, shouldEnable).apply();
    }

    public void setFrenchEnabled(boolean shouldEnable) {
        mPrefs.edit().putBoolean(PREFERENCE_FRENCH_PRAYERS, shouldEnable).apply();
    }

    public void setPersianEnabled(boolean shouldEnable) {
        mPrefs.edit().putBoolean(PREFERENCE_PERSIAN_PRAYERS, shouldEnable).apply();
    }

    public void setSpanishEnabled(boolean shouldEnable) {
        mPrefs.edit().putBoolean(PREFERENCE_SPANISH_PRAYERS, shouldEnable).apply();
    }
}
