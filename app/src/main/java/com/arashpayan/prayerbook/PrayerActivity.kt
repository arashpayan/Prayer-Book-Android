package com.arashpayan.prayerbook

import android.app.ActivityManager.TaskDescription
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arashpayan.prayerbook.App.Companion.runInBackground
import com.arashpayan.prayerbook.database.UserDB.Companion.get
import com.arashpayan.prayerbook.thread.WorkerRunnable

class PrayerActivity : AppCompatActivity() {
    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        setContentView(R.layout.prayer_activity)

        val appName = getString(R.string.app_name)
        val headerColor = ContextCompat.getColor(this, R.color.task_header)
        if (Build.VERSION.SDK_INT > 27) {
            setTaskDescription(TaskDescription(appName, R.mipmap.ic_launcher, headerColor))
        } else {
            val appIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)
            setTaskDescription(TaskDescription(appName, appIcon, headerColor))
        }

        if (state == null) {
            val extras = getIntent().getExtras()
            requireNotNull(extras) { "You need to provide an 'extra' with the prayer id" }
            val prayerId = extras.getLong(ARG_PRAYER_ID, 0)
            require(prayerId != 0L) { "You need to provide an 'extra' with the prayer id" }

            val fragment = PrayerFragment.newInstance(prayerId)

            val fm = getSupportFragmentManager()
            val ft = fm.beginTransaction()
            ft.add(R.id.prayer_container, fragment)
            ft.commit()
        }
    }

    companion object {
        private const val ARG_PRAYER_ID = "prayer_id"

        fun newIntent(context: Context, prayerId: Long): Intent {
            runInBackground(object : WorkerRunnable {
                override fun run() {
                    get().accessedPrayer(prayerId)
                }
            })
            val intent = Intent(context, PrayerActivity::class.java)
            intent.putExtra(ARG_PRAYER_ID, prayerId)

            return intent
        }
    }
}
