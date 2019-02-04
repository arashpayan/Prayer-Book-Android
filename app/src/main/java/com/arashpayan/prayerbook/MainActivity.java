package com.arashpayan.prayerbook;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.arashpayan.util.L;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        BottomNavigationView bar = findViewById(R.id.bottom_bar);
        bar.setOnNavigationItemSelectedListener(barItemListener);
        bar.setOnNavigationItemReselectedListener(reselectListener);

        String appName = getString(R.string.app_name);
        int headerColor = ContextCompat.getColor(this, R.color.task_header);
        if (Build.VERSION.SDK_INT > 27) {
            setTaskDescription(new ActivityManager.TaskDescription(appName, R.mipmap.ic_launcher, headerColor));
        } else if (Build.VERSION.SDK_INT > 20) {
            Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            setTaskDescription(new ActivityManager.TaskDescription(appName, appIcon, headerColor));
        }

        if (savedInstanceState == null) {
            CategoriesFragment fragment = CategoriesFragment.newInstance();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, fragment, CategoriesFragment.TAG)
                    .setPrimaryNavigationFragment(fragment)
                    .commit();
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener barItemListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStackImmediate();
            Fragment toShow;
            String tag = "";
            switch (item.getItemId()) {
                case R.id.prayers:
                    tag = CategoriesFragment.TAG;
                    break;
                case R.id.bookmarks:
                    tag = BookmarksFragment.TAG;
                    break;
                case R.id.recents:
                    tag = RecentsFragment.TAG;
                    break;
                case R.id.languages:
                    tag = LanguagesFragment.TAG;
                    break;
                case R.id.about:
                    tag = AboutFragment.TAG;
                    break;
            }
            toShow = fm.findFragmentByTag(tag);
            if (toShow != null) {
                L.i("showing " + tag);
                FragmentTransaction ft = fm.beginTransaction().attach(toShow).setPrimaryNavigationFragment(toShow);
                if (fm.getPrimaryNavigationFragment() != null) {
                    ft.detach(fm.getPrimaryNavigationFragment());
                }
                ft.commit();
                return true;
            }

            switch (item.getItemId()) {
                case R.id.prayers:
                    toShow = CategoriesFragment.newInstance();
                    break;
                case R.id.bookmarks:
                    toShow = BookmarksFragment.newInstance();
                    break;
                case R.id.recents:
                    toShow = RecentsFragment.newInstance();
                    break;
                case R.id.languages:
                    toShow = LanguagesFragment.newInstance();
                    break;
                case R.id.about:
                    toShow = AboutFragment.newInstance();
                    break;
                default:
                    throw new RuntimeException("Unknown bottom bar item id");
            }

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.main_container, toShow, tag);
            ft.setPrimaryNavigationFragment(toShow);
            if (fm.getPrimaryNavigationFragment() != null) {
                ft.detach(fm.getPrimaryNavigationFragment());
            }
            ft.commit();
            return true;
        }
    };

    private final BottomNavigationView.OnNavigationItemReselectedListener reselectListener = new BottomNavigationView.OnNavigationItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.prayers:
                    FragmentManager fm = getSupportFragmentManager();
                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    break;
                case R.id.bookmarks:
                    break;
                case R.id.recents:
                    break;
                case R.id.languages:
                    break;
                case R.id.about:
                    break;
            }
        }
    };
}