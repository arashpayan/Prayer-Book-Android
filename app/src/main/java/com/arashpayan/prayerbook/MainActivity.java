package com.arashpayan.prayerbook;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.arashpayan.util.L;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        BottomNavigationView bar = findViewById(R.id.bottom_bar);
        disableShiftMode(bar);
        bar.setOnNavigationItemSelectedListener(barItemListener);
        bar.setOnNavigationItemReselectedListener(reselectListener);

        if (Build.VERSION.SDK_INT >= 21) {
            String appName = getString(R.string.app_name);
            Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            int headerColor = ContextCompat.getColor(this, R.color.task_header);
            setTaskDescription(new ActivityManager.TaskDescription(appName, appIcon, headerColor));
        }

        if (savedInstanceState == null) {
            CategoriesFragment categoriesFragment = new CategoriesFragment();

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.main_container, categoriesFragment, CategoriesFragment.CATEGORIES_TAG);
            ft.commit();
        }
    }

    @SuppressLint("RestrictedApi")
    private void disableShiftMode(BottomNavigationView bar) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bar.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            for (int i=0; i<menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException nsfe) {
            L.e("Unable to get mShiftingMode field", nsfe);
        } catch (IllegalAccessException iae) {
            L.w("Unable to disable shifting mode", iae);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener barItemListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.prayers:
                    L.i("PRAYERS");
                    break;
                case R.id.bookmarks:
                    L.i("BOOKMARKS");
                    break;
                case R.id.recents:
                    L.i("RECENTS");
                    break;
                case R.id.languages:
                    L.i("LANGUAGES");
                    break;
                case R.id.about:
                    L.i("ABOUT");
                    break;
            }
            return true;
        }
    };

    private BottomNavigationView.OnNavigationItemReselectedListener reselectListener = new BottomNavigationView.OnNavigationItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.prayers:
                    L.i("reselected PRAYERS");
                    break;
                case R.id.bookmarks:
                    L.i("reselected BOOKMARKS");
                    break;
                case R.id.recents:
                    L.i("reselected RECENTS");
                    break;
                case R.id.languages:
                    L.i("reselected LANGUAGES");
                    break;
                case R.id.about:
                    L.i("reselected ABOUT");
                    break;
            }
        }
    };
}