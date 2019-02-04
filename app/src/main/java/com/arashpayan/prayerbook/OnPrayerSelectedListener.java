package com.arashpayan.prayerbook;

import androidx.annotation.UiThread;

/**
 * PrayerBook
 * Created by arash on 5/2/16.
 */
interface OnPrayerSelectedListener {
    @UiThread
    void onPrayerSelected(long prayerId);
}
