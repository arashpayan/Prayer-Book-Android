package com.arashpayan.prayerbook.thread;

import androidx.annotation.UiThread;

public interface UiRunnable extends Runnable {

    @UiThread
    void run();

}
