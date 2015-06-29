package com.arashpayan.prayerbook;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class PrayerBook extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.prayer_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.pb_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            CategoriesFragment categoriesFragment = new CategoriesFragment();

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.pb_container, categoriesFragment, CategoriesFragment.CATEGORIES_TAG);
            ft.commit();
        }
    }
}
