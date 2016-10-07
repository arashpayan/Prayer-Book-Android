package com.arashpayan.prayerbook;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

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
}