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
    private static Preferences singleton = null;
    private SharedPreferences prefs = null;
    
    private static final String PREFERENCES_FILE_NAME = "PrayerBookPreferences";
    
    private static final String PREFERENCE_DATABASE_VERSION = "DatabaseVersion";
    
    private Preferences(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }
    
    public synchronized static Preferences getInstance(Application app) {
        if (singleton == null)
            singleton = new Preferences(app.getApplicationContext());
        
        return singleton;
    }
    
    public int getDatabaseVersion() {
        return prefs.getInt(PREFERENCE_DATABASE_VERSION, 0);
    }
    
    public void setDatabaseVersion(int version) {
        prefs.edit().putInt(PREFERENCE_DATABASE_VERSION, version);
    }
}
