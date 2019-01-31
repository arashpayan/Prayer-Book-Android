package com.arashpayan.prayerbook.thread;

import androidx.annotation.WorkerThread;

public interface WorkerRunnable extends Runnable {

    @WorkerThread
    void run();

}
