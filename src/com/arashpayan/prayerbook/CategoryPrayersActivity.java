/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

/**
 *
 * @author arash
 */
public class CategoryPrayersActivity extends FragmentActivity {
    
    public static final String CATEGORY_ARGUMENT = "Category";
    public static final String LANGUAGE_ARGUMENT = "Language";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);

        if (savedInstanceState == null) {
            String category = getIntent().getStringExtra(CATEGORY_ARGUMENT);
            Language language = getIntent().getParcelableExtra(LANGUAGE_ARGUMENT);
            CategoryPrayersFragment fragment = new CategoryPrayersFragment();
            Bundle args = new Bundle();
            args.putString(CategoryPrayersFragment.CATEGORY_ARGUMENT, category);
            args.putParcelable(CategoryPrayersFragment.LANGUAGE_ARGUMENT, language);
            fragment.setArguments(args);
            
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, fragment, CategoryPrayersFragment.CATEGORYPRAYERS_TAG);
            ft.commit();
        }
    }
}
