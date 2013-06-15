package com.arashpayan.prayerbook;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PrayerBook extends FragmentActivity implements ActionBar.TabListener
{
    private static final int VIEW_PAGER_ID = 10101010;
    private static final String TAG = "PrayerBook";
    
    public final static int ACTIONITEM_SETTINGS             = 5;
    public final static int ACTIONITEM_ABOUT                = 6;
    
    private BookmarksFragment bookmarksFragment;
    private CategoriesFragment categoriesFragment;
    private RecentPrayersFragment recentPrayersFragment;
    
    private TabsPagerAdapter tabsPagerAdapter;
    private ViewPager viewPager;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        
        ActionBar.Tab bookmarksTab = getActionBar().newTab();
        bookmarksTab.setTabListener(this);
        bookmarksTab.setText(R.string.bookmarks);
        bookmarksTab.setTag(BookmarksFragment.BOOKMARKS_TAG);
        getActionBar().addTab(bookmarksTab);
        bookmarksFragment = new BookmarksFragment();
        
        ActionBar.Tab categoriesTab = getActionBar().newTab();
        categoriesTab.setTabListener(this);
        categoriesTab.setText(R.string.prayers);
        categoriesTab.setTag(CategoriesFragment.CATEGORIES_TAG);
        getActionBar().addTab(categoriesTab);
        categoriesFragment = new CategoriesFragment();
        
        ActionBar.Tab recentPrayersTab = getActionBar().newTab();
        recentPrayersTab.setTabListener(this);
        recentPrayersTab.setText(R.string.recents);
        recentPrayersTab.setTag(RecentPrayersFragment.RECENTS_TAG);
        getActionBar().addTab(recentPrayersTab);
        recentPrayersFragment = new RecentPrayersFragment();
        
        viewPager = new ViewPager(this);
        viewPager.setId(VIEW_PAGER_ID);
        viewPager.setAdapter(tabsPagerAdapter);
        viewPager.setOnPageChangeListener(tabsPagerAdapter);
        setContentView(viewPager);
        
        getActionBar().selectTab(categoriesTab);
        
        int dbVersion = Preferences.getInstance(getApplication()).getDatabaseVersion();
        if (dbVersion != 1)
        {
            // then we need to copy over the latest database
            File databaseFile = new File(getFilesDir(), "pbdb.db");
            Database.databaseFile = databaseFile; 
            Log.i(TAG, "database file: " + databaseFile.getAbsolutePath());
            try {
                BufferedInputStream is = new BufferedInputStream(getAssets().open("pbdb.jet") /*getResources().openRawResource(R.raw.pbdb)*/, 8192);
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
                
            } catch (IOException ex) {
                Log.w(TAG, "Error writing prayer database", ex);
            }
        }
        
        Database.getInstance();
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
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onStop() {
        super.onStop();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        Log.i(TAG, "PrayerBook::onConfigurationChanged");
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (viewPager == null)
            return;
        
        if (tab.getTag() == BookmarksFragment.BOOKMARKS_TAG)
            viewPager.setCurrentItem(0, true);
        else if (tab.getTag() == CategoriesFragment.CATEGORIES_TAG)
            viewPager.setCurrentItem(1, true);
        else if (tab.getTag() == RecentPrayersFragment.RECENTS_TAG)
            viewPager.setCurrentItem(2, true);
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    public class TabsPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

        public TabsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        
        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return bookmarksFragment;
                case 1:
                    return categoriesFragment;
                case 2:
                    return recentPrayersFragment;
                default:
                    throw new IllegalArgumentException("Invalid page number");
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        public void onPageSelected(int position) {
            getActionBar().setSelectedNavigationItem(position);
        }
        
        public void onPageScrolled(int i, float f, int i1) {}

        public void onPageScrollStateChanged(int i) {}
        
    }
}
