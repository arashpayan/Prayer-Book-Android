package com.arashpayan.prayerbook;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.arashpayan.util.L;
import com.squareup.otto.Bus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class App extends Application {

    private Bus mBus;
    private Handler mMainThreadHandler;

    private Handler mBackgroundHandler;

    private static volatile App mApp;
    private static final int LatestDatabaseVersion = 6;

    public static App getApp() {
        return mApp;
    }

    @Override
    public void onCreate() {
        mApp = this;
        super.onCreate();

        copyDatabaseFile();
        mBus = new Bus();
        mMainThreadHandler = new Handler(Looper.getMainLooper());

        HandlerThread bgThread = new HandlerThread("Prayer Book Background");
        bgThread.start();
        mBackgroundHandler = new Handler(bgThread.getLooper());
    }

    private void copyDatabaseFile() {
        int dbVersion = Preferences.getInstance(this).getDatabaseVersion();
        File databaseFile = new File(getFilesDir(), "pbdb.db");
        Database.databaseFile = databaseFile;
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
                Preferences.getInstance(this).setDatabaseVersion(LatestDatabaseVersion);
            } catch (IOException ex) {
                L.w("Error writing prayer database", ex);
            }
        }
    }

    public static void postOnBus(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mApp.mBus.post(event);
        } else {
            postOnMainThread(new Runnable() {

                public void run() {
                    try {
                        mApp.mBus.post(event);
                    } catch (Throwable t) {
                        L.e("Unexpected throwable encountered while dispatching event", t);
                    }
                }
            });
        }
    }

    public static void postOnMainThread(Runnable r) {
        mApp.mMainThreadHandler.post(r);
    }

    public static void postOnBackgroundThread(Runnable r) {
        mApp.mBackgroundHandler.post(r);
    }

    public static void registerOnBus(final Object object) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (mApp == null) {
                L.w("THE APP IS NULL!!! YOU'RE GONNA CRASH");
            }
            mApp.mBus.register(object);
        } else {
            postOnMainThread(new Runnable() {

                public void run() {
                    mApp.mBus.register(object);
                }
            });
        }
    }

    public static void unregisterFromBus(final Object object) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mApp.mBus.unregister(object);
        } else {
            postOnMainThread(new Runnable() {

                public void run() {
                    mApp.mBus.unregister(object);
                }
            });
        }
    }
}
