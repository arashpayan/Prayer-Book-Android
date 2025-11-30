package com.arashpayan.prayerbook

import android.app.ActivityManager.TaskDescription
//import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.arashpayan.util.L
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationBarView.OnItemReselectedListener

class MainActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)
        val bar = findViewById<BottomNavigationView>(R.id.bottom_bar)
        bar.setOnItemSelectedListener(barItemListener)
        bar.setOnItemReselectedListener(reselectListener)

        val appName = getString(R.string.app_name)
        val headerColor = ContextCompat.getColor(this, R.color.task_header)
        if (Build.VERSION.SDK_INT > 27) {
            setTaskDescription(TaskDescription(appName, R.mipmap.ic_launcher, headerColor))
        } else {
            val appIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)
            setTaskDescription(TaskDescription(appName, appIcon, headerColor))
        }

        if (savedInstanceState == null) {
            val fragment = CategoriesFragment.newInstance()

            getSupportFragmentManager().beginTransaction()
                .add(R.id.main_container, fragment, CategoriesFragment.TAG)
                .setPrimaryNavigationFragment(fragment)
                .commit()
        }
    }

    private val barItemListener: NavigationBarView.OnItemSelectedListener =
        object : NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {

                if (item.itemId == R.id.about) {
                    val fm = getSupportFragmentManager()
                    val aboutFragment = AboutFragment.newInstance()
                    fm.beginTransaction()
                        .replace(R.id.main_container, aboutFragment, AboutFragment.TAG)
                        .addToBackStack(null)
                        .commit()
                    return true // Event is handled
                }

                val fm = getSupportFragmentManager()
                fm.popBackStackImmediate()
                var toShow: Fragment?
                var tag = ""
                var itemId = item.getItemId()
                if (itemId == R.id.prayers) {
                    tag = CategoriesFragment.TAG
                } else if (itemId == R.id.bookmarks) {
                    tag = BookmarksFragment.TAG
                } else if (itemId == R.id.recents) {
                    tag = RecentsFragment.TAG
                } else if (itemId == R.id.languages) {
                    tag = LanguagesFragment.TAG
                }
                toShow = fm.findFragmentByTag(tag)
                if (toShow != null) {
                    L.i("showing " + tag)
                    val ft =
                        fm.beginTransaction().attach(toShow).setPrimaryNavigationFragment(toShow)
                    if (fm.getPrimaryNavigationFragment() != null) {
                        ft.detach(fm.getPrimaryNavigationFragment()!!)
                    }
                    ft.commit()
                    return true
                }

                itemId = item.getItemId()
                if (itemId == R.id.prayers) {
                    toShow = CategoriesFragment.newInstance()
                } else if (itemId == R.id.bookmarks) {
                    toShow = BookmarksFragment.newInstance()
                } else if (itemId == R.id.recents) {
                    toShow = RecentsFragment.newInstance()
                } else if (itemId == R.id.languages) {
                    toShow = LanguagesFragment.newInstance()
                } else {
                    throw RuntimeException("Unknown bottom bar item id")
                }

                val ft = fm.beginTransaction()
                ft.add(R.id.main_container, toShow, tag)
                ft.setPrimaryNavigationFragment(toShow)
                if (fm.getPrimaryNavigationFragment() != null) {
                    ft.detach(fm.getPrimaryNavigationFragment()!!)
                }
                ft.commit()
                return true
            }
        }

    private val reselectListener: OnItemReselectedListener = object : OnItemReselectedListener {
        override fun onNavigationItemReselected(item: MenuItem) {
            if (item.getItemId() == R.id.prayers) {
                val fm = getSupportFragmentManager()
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
    }
}