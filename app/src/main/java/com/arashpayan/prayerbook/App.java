package com.arashpayan.prayerbook;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebSettings;

import com.arashpayan.prayerbook.database.Prayer;
import com.arashpayan.prayerbook.database.PrayersDB;
import com.arashpayan.prayerbook.database.UserDB;
import com.arashpayan.prayerbook.thread.UiRunnable;
import com.arashpayan.prayerbook.thread.WorkerRunnable;
import com.arashpayan.util.L;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

public class App extends Application {

    private Handler mMainThreadHandler;
    private ExecutorService mExecutor;

    private static volatile App app;
    private static final int LatestDatabaseVersion = 24;

    @Override
    public void onCreate() {
        app = this;
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate();

        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Prefs.init(this);
        UserDB.set(new UserDB(this, false));
        copyDatabaseFile();

        // Load as much of the webview libraries as much as possible in the background
        // https://groups.google.com/a/chromium.org/d/msg/android-webview-dev/hjn1h7dBlH8/Iv0j08O6AQAJ
        runInBackground(new WorkerRunnable() {
            @Override
            public void run() {
                WebSettings.getDefaultUserAgent(App.this);
            }
        });
    }

    private void copyDatabaseFile() {
        int dbVersion = Prefs.get().getDatabaseVersion();
        File databaseFile = new File(getFilesDir(), "pbdb.db");
        PrayersDB.databaseFile = databaseFile;
        if (dbVersion != LatestDatabaseVersion) {
            // then we need to copy over the latest database
            L.i("database file: " + databaseFile.getAbsolutePath());
            try {
                BufferedInputStream is = new BufferedInputStream(getAssets().open("pbdb.jet"), 8192);
                OutputStream os = new BufferedOutputStream(new FileOutputStream(databaseFile), 8192);
                byte[] data = new byte[4096];
                while (is.available() != 0) {
                    int numRead = is.read(data);
                    if (numRead != 0)
                        os.write(data);
                }
                is.close();
                os.close();
                Prefs.get().setDatabaseVersion(LatestDatabaseVersion);
                filterBrokenPrayerIds(PrayersDB.get(), UserDB.get());
            } catch (IOException ex) {
                L.w("Error writing prayer database", ex);
            }
        }
    }

    private void filterBrokenPrayerIds(@NonNull PrayersDB db, UserDB userDB) {
        ArrayList<Long> bookmarks = userDB.getBookmarks();
        for (Long id : bookmarks) {
            Prayer prayer = db.getPrayer(id);
            if (prayer == null) {
                userDB.deleteBookmark(id);
            }
        }

        userDB.clearRecents();
    }

    @AnyThread
    public static void runOnUiThread(UiRunnable r) {
        app.mMainThreadHandler.post(r);
    }

    @AnyThread
    public static void runInBackground(WorkerRunnable r) {
        app.mExecutor.submit(r);
    }

}
