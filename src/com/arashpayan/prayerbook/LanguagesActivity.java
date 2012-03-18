/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockPreferenceActivity;

/**
 *
 * @author arash
 */
public class LanguagesActivity extends SherlockPreferenceActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);
    }
    
}
