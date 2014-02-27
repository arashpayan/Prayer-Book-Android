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
    
    public void setDatabaseVersion(int version) {
        mPrefs.edit().putInt(PREFERENCE_DATABASE_VERSION, version).apply();
    }
    
    public boolean isLanguageEnabled(Language lang) {
        return mPrefs.getBoolean(lang.code + "_enabled", false);
    }
    
    public void setLanguageEnabled(Language lang, boolean shouldEnable) {
        mPrefs.edit().putBoolean(lang.code + "_enabled", shouldEnable).apply();
    }
}
