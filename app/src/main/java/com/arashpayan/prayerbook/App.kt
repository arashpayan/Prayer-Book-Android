package com.arashpayan.prayerbook

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.webkit.WebSettings
import androidx.annotation.AnyThread
import androidx.appcompat.app.AppCompatDelegate
import com.arashpayan.prayerbook.database.PrayersDB
import com.arashpayan.prayerbook.database.UserDB
import com.arashpayan.prayerbook.database.UserDB.Companion.set
import com.arashpayan.prayerbook.thread.UiRunnable
import com.arashpayan.prayerbook.thread.WorkerRunnable
import com.arashpayan.util.L
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.Volatile

class App : Application() {
    private var mMainThreadHandler: Handler? = null
    private var mExecutor: ExecutorService? = null

    override fun onCreate() {
        app = this
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate()

        mMainThreadHandler = Handler(Looper.getMainLooper())
        mExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

        Prefs.init(this)
        set(UserDB(this, false))
        copyDatabaseFile()

        // Load as much of the webview libraries as much as possible in the background
        // https://groups.google.com/a/chromium.org/d/msg/android-webview-dev/hjn1h7dBlH8/Iv0j08O6AQAJ
        runInBackground(object : WorkerRunnable {
            override fun run() {
                WebSettings.getDefaultUserAgent(this@App)
            }
        })
    }

    private fun copyDatabaseFile() {
        val dbVersion = Prefs.get().databaseVersion
        val databaseFile = File(getFilesDir(), "pbdb.db")
        PrayersDB.databaseFile = databaseFile
        if (dbVersion != LatestDatabaseVersion) {
            // then we need to copy over the latest database
            L.i("database file: " + databaseFile.getAbsolutePath())
            try {
                val `is` = BufferedInputStream(getAssets().open("pbdb.jet"), 8192)
                val os: OutputStream = BufferedOutputStream(FileOutputStream(databaseFile), 8192)
                val data = ByteArray(4096)
                while (`is`.available() != 0) {
                    val numRead = `is`.read(data)
                    if (numRead != 0) os.write(data)
                }
                `is`.close()
                os.close()
                Prefs.get().databaseVersion = LatestDatabaseVersion
                filterBrokenPrayerIds(PrayersDB.get(), UserDB.get())
            } catch (ex: IOException) {
                L.w("Error writing prayer database", ex)
            }
        }
    }

    private fun filterBrokenPrayerIds(db: PrayersDB, userDB: UserDB) {
        // Acknowledge that the list from Java might contain nulls.
        val bookmarks: ArrayList<Long?> = userDB.bookmarks

        // Filter out any null IDs before iterating.
        // The 'for' loop now safely iterates over a List<Long>.
        for (id in bookmarks.filterNotNull()) {
            val prayer = db.getPrayer(id)
            if (prayer == null) {
                userDB.deleteBookmark(id)
            }
        }

        userDB.clearRecents()
    }

    companion object {
        @Volatile
        private var app: App? = null
        private const val LatestDatabaseVersion = 24

        @JvmStatic
        @AnyThread
        fun runOnUiThread(r: UiRunnable) {
            app!!.mMainThreadHandler!!.post(r)
        }

        @JvmStatic
        @AnyThread
        fun runInBackground(r: WorkerRunnable?) {
            app!!.mExecutor!!.submit(r)
        }
    }
}
