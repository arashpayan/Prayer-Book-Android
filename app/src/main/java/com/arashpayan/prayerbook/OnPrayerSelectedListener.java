package com.arashpayan.prayerbook;

import androidx.annotation.UiThread;

/**
 * PrayerBook
 * Created by arash on 5/2/16.
 */
public interface OnPrayerSelectedListener {
    @UiThread
    void onPrayerSelected(long prayerId);
}
