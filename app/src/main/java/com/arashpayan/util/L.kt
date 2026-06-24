/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.util

import android.util.Log

/**
 * 
 * @author arash
 */
object L {
    private const val TAG = "PrayerBook"

    fun i(msg: String) {
        Log.i(TAG, msg)
    }

    fun d(msg: String?) {
        // Log.d(TAG, msg);
    }

    fun w(msg: String?) {
        // Log.w(TAG, msg);
    }

    fun w(msg: String?, t: Throwable?) {
        // Log.w(TAG, msg, t);
    }

    fun e(msg: String?, t: Throwable?) {
        //Log.e(TAG, msg, t);
    }
}
