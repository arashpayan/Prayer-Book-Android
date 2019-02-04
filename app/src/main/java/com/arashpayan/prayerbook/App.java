package com.arashpayan.prayerbook;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebSettings;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.AnyThread;
import androidx.appcompat.app.AppCompatDelegate;

public class App extends Application {

    private Handler mMainThreadHandler;
    private ExecutorService mExecutor;

    private static volatile App app;
    private static final int LatestDatabaseVersion = 19;

    @Override
    public void onCreate() {
        app = this;
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate();

        Prefs.init(this);
        copyDatabaseFile();
        UserDB.set(new UserDB(this, false));
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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
            } catch (IOException ex) {
                L.w("Error writing prayer database", ex);
            }
        }
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
