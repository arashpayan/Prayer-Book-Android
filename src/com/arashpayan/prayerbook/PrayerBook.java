package com.arashpayan.prayerbook;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PrayerBook extends FragmentActivity
{
    public static final int CONTENT_VIEW_ID = 10101010;
    public static final String TAG = "PrayerBook";
    
    private FragmentActivity currentFragment;
    
    public final static int ACTIONITEM_SETTINGS             = 5;
    public final static int ACTIONITEM_ABOUT                = 6;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        
        int dbVersion = Preferences.getInstance(getApplication()).getDatabaseVersion();
        if (dbVersion != 1)
        {
            // then we need to copy over the latest database
            File databaseFile = new File(getFilesDir(), "pbdb.db");
            Database.databaseFile = databaseFile;
            Log.i(TAG, "database file: " + databaseFile.getAbsolutePath());
            try {
                BufferedInputStream is = new BufferedInputStream(getResources().openRawResource(R.raw.pbdb), 8192);
                OutputStream os = new BufferedOutputStream(new FileOutputStream(databaseFile), 8192);
                byte[] data = new byte[4096];
                while (is.available() != 0)
                {
                    int numRead = is.read(data);
                    if (numRead != 0)
                        os.write(data);
                }
                is.close();
                os.close();
                
//                Preferences.getInstance(getApplication()).setDatabaseVersion(1);
            } catch (IOException ex) {
                Log.w(TAG, "Error writing prayer database", ex);
            }
        }
        
        Database.getInstance();
        
        getActionBar().setListNavigationCallbacks(new NavigationList(this), new ActionBar.OnNavigationListener() {

            public boolean onNavigationItemSelected(int arg0, long arg1) {
                Toast.makeText(getApplicationContext(), "onNavigationItemSelected", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        
        FrameLayout frame = new FrameLayout(this);
        frame.setId(CONTENT_VIEW_ID);
        setContentView(frame);
        
        CategoriesFragment categoriesFragment = new CategoriesFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(CONTENT_VIEW_ID, categoriesFragment).commit();
    }
    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        menu.add(0, ACTIONITEM_SETTINGS, ACTIONITEM_SETTINGS, "Settings");
//        menu.add(0, ACTIONITEM_ABOUT, ACTIONITEM_ABOUT, "About");
//        
//        return true;
//    }
    
//    public boolean onOptionsItemSelected(Menu menu) {
//        Toast.makeText(this, "onOptionsItemSelected", Toast.LENGTH_SHORT).show();
//        return true;
//    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        Log.i(TAG, "PrayerBook::onStart");
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "PrayerBook::onPause");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        Log.i(TAG, "PrayerBook::onResume");
    }
    
    @Override
    public void onStop() {
        super.onStop();
        
        Log.i(TAG, "PrayerBook::onStop");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        Log.i(TAG, "PrayerBook::onDestroy");
        
//        FragmentManager fm = getSupportFragmentManager();
//        
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.replace(RESULT_OK, null);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        Log.i(TAG, "PrayerBook::onConfigurationChanged");
    }
}
