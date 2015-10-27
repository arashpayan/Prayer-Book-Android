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
import android.support.v7.widget.Toolbar;

/**
 * Created by arash on 7/6/15.
 */
public class PrayerActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.prayer_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.prayer_toolbar);

        if (Build.VERSION.SDK_INT >= 21) {
            String appName = getString(R.string.app_name);
            Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            int headerColor = ContextCompat.getColor(this, R.color.task_header);
            setTaskDescription(new ActivityManager.TaskDescription(appName, appIcon, headerColor));
        }

        setSupportActionBar(toolbar);

        if (state == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                throw new IllegalArgumentException("You need to provide an 'extra' with the prayer id");
            }
            long prayerID = extras.getLong(PrayerFragment.PRAYER_ID_ARGUMENT, 0);
            if (prayerID == 0) {
                throw new IllegalArgumentException("You need to provide an 'extra' with the prayer id");
            }

            PrayerFragment fragment = new PrayerFragment();
            Bundle args = new Bundle();
            args.putLong(PrayerFragment.PRAYER_ID_ARGUMENT, prayerID);
            fragment.setArguments(args);

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.prayer_container, fragment);
            ft.commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isFinishing()) {
            overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
        }
    }

}
