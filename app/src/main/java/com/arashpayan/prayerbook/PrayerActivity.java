package com.arashpayan.prayerbook;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PrayerActivity extends AppCompatActivity {

    private static final String ARG_PRAYER_ID = "prayer_id";

    public static Intent newIntent(Context context, long prayerId) {
        Intent intent = new Intent(context, PrayerActivity.class);
        intent.putExtra(ARG_PRAYER_ID, prayerId);

        return intent;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.prayer_activity);
        Toolbar toolbar = findViewById(R.id.prayer_toolbar);

        String appName = getString(R.string.app_name);
        if (Build.VERSION.SDK_INT > 27) {
            setTaskDescription(new ActivityManager.TaskDescription(appName, R.mipmap.ic_launcher, R.color.task_header));
        } else if (Build.VERSION.SDK_INT > 20) {
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
            long prayerId = extras.getLong(ARG_PRAYER_ID, 0);
            if (prayerId == 0) {
                throw new IllegalArgumentException("You need to provide an 'extra' with the prayer id");
            }

            PrayerFragment fragment = PrayerFragment.newInstance(prayerId);

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
