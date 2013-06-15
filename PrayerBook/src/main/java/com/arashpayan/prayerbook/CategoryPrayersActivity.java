/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

/**
 *
 * @author arash
 */
public class CategoryPrayersActivity extends FragmentActivity {
    
    public static final String CATEGORY_ARGUMENT = "Category";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState == null) {
            String category = getIntent().getStringExtra(CATEGORY_ARGUMENT);
            CategoryPrayersFragment fragment = new CategoryPrayersFragment();
            Bundle args = new Bundle();
            args.putString(CategoryPrayersFragment.CATEGORY_ARGUMENT, category);
            fragment.setArguments(args);
            
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, fragment, CategoryPrayersFragment.CATEGORYPRAYERS_TAG);
            ft.commit();
        }
    }
}
