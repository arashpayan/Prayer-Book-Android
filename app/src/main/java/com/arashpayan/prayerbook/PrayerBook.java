package com.arashpayan.prayerbook;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class PrayerBook extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.prayer_book);

        if (savedInstanceState == null) {
            CategoriesFragment categoriesFragment = new CategoriesFragment();

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.pb_container, categoriesFragment, CategoriesFragment.CATEGORIES_TAG);
            ft.commit();
        }
    }
}
