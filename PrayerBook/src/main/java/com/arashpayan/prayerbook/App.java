package com.arashpayan.prayerbook;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.arashpayan.util.L;
import com.squareup.otto.Bus;

/**
 * Created by arash on 6/16/13.
 */
public class App extends Application {

    private Bus mBus;
    private Handler mMainThreadHandler;
    private static volatile App mApp;

    public void onCreate() {
        mApp = this;
        super.onCreate();

        mBus = new Bus();
        mMainThreadHandler = new Handler(Looper.getMainLooper());
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
