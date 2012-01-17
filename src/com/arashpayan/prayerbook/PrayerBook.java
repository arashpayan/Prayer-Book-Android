package com.arashpayan.prayerbook;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PrayerBook extends FragmentActivity
{
    public static final int CONTENT_VIEW_ID = 10101010;
    public static final String TAG = "PrayerBook";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
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
        
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setTitle("");
        getActionBar().setListNavigationCallbacks(new NavigationList(this), new ActionBar.OnNavigationListener() {

            public boolean onNavigationItemSelected(int arg0, long arg1) {
                Log.i(PrayerBook.TAG, "onNavigationItemSelected("+arg0+", "+arg1+")");
                return false;
            }
        });
        
        FrameLayout frame = new FrameLayout(this);
        frame.setId(CONTENT_VIEW_ID);
        setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        CategoriesFragment categoriesFragment = new CategoriesFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(CONTENT_VIEW_ID, categoriesFragment).commit();
        
//        CategoryPrayersFragment categoryPrayersFragment = new CategoryPrayersFragment("Detachment");
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.add(CONTENT_VIEW_ID, categoryPrayersFragment).commit();
    }
}
