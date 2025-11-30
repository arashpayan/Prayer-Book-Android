package com.arashpayan.prayerbook.database

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import com.arashpayan.prayerbook.App
import com.arashpayan.prayerbook.thread.UiRunnable
import com.arashpayan.util.L
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

class UserDB(ctx: Context, inMemory: Boolean) {
    private val helper: Helper
    private val listeners = CopyOnWriteArrayList<WeakReference<Listener?>>()

    init {
        helper = Helper(ctx, inMemory)
    }

    //region Bookmarks
    @WorkerThread // returns true on success. false otherwise.
    fun addBookmark(prayerId: Long): Boolean {
        val db = helper.getWritableDatabase()
        val insertSQL = "INSERT OR IGNORE INTO " + BOOKMARKS_TABLE +
                "(" + BOOKMARKS_COL_PRAYER_ID + ", " + BOOKMARKS_COL_POSITION +
                ") SELECT " + prayerId + ", COALESCE(MAX(" + BOOKMARKS_COL_POSITION + "), 0)+1 FROM " + BOOKMARKS_TABLE
        try {
            db.execSQL(insertSQL)
        } catch (ex: SQLException) {
            L.w("Error adding bookmark", ex)
            return false
        }

        notifyBookmarkAdded(prayerId)

        return true
    }

    @WorkerThread
    fun deleteBookmark(prayerId: Long) {
        val db = helper.getWritableDatabase()
        db.delete(BOOKMARKS_TABLE, "prayer_id=?", arrayOf<String>("" + prayerId))
        notifyBookmarkDeleted(prayerId)
    }

    @get:WorkerThread
    val bookmarks: ArrayList<Long?>
        get() {
            val bookmarks = java.util.ArrayList<Long?>()
            val db = helper.getReadableDatabase()
            db.query(
                BOOKMARKS_TABLE,
                arrayOf<String>(BOOKMARKS_COL_PRAYER_ID),
                null,
                null,
                null,
                null,
                "position ASC"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val id =
                        cursor.getLong(cursor.getColumnIndexOrThrow(BOOKMARKS_COL_PRAYER_ID))
                    bookmarks.add(id)
                }
            }
            return bookmarks
        }

    @WorkerThread
    fun isBookmarked(prayerId: Long): Boolean {
        val db = helper.getReadableDatabase()
        val cols: Array<String?> = arrayOf<String?>(BOOKMARKS_COL_PRAYER_ID)
        val selection: String = BOOKMARKS_COL_PRAYER_ID + "=?"
        val args: Array<String?> = arrayOf<String?>("" + prayerId)
        db.query(BOOKMARKS_TABLE, cols, selection, args, null, null, null).use { cursor ->
            return cursor.moveToNext()
        }
    }


    //endregion
    //region Recents
    @WorkerThread
    fun accessedPrayer(prayerId: Long): Boolean {
        val db = helper.getWritableDatabase()
        val insertSql =
            "INSERT OR REPLACE INTO " + RECENTS_TABLE + " (" + RECENTS_COL_PRAYER_ID + ", " + RECENTS_COL_ACCESS_TIME + ") VALUES (?, ?)"
        try {
            db.execSQL(insertSql, arrayOf<Any>("" + prayerId, "" + System.currentTimeMillis()))
        } catch (ex: SQLException) {
            L.w("Error updating prayer access time", ex)
            return false
        }

        notifyPrayerAccessed(prayerId)

        return true
    }

    @WorkerThread
    fun clearRecents() {
        val db = helper.getWritableDatabase()
        try {
            db.delete(RECENTS_TABLE, null, null)
        } catch (ex: SQLException) {
            L.w("Error deleting recent records", ex)
        }

        notifyRecentsCleared()
    }

    @get:WorkerThread
    val recents: ArrayList<Long?>
        get() {
            val recents = java.util.ArrayList<Long?>()
            val db = helper.getReadableDatabase()
            db.query(
                RECENTS_TABLE,
                arrayOf<String>(RECENTS_COL_PRAYER_ID),
                null,
                null,
                null,
                null,
                RECENTS_COL_ACCESS_TIME + " DESC",
                "50"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val id =
                        cursor.getLong(cursor.getColumnIndexOrThrow(RECENTS_COL_PRAYER_ID))
                    recents.add(id)
                }
            }
            return recents
        }

    //endregion
    private class Helper(ctx: Context, inMemory: Boolean) :
        SQLiteOpenHelper(ctx, if (inMemory) null else "user_data", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            val createBookmarks =
                "CREATE TABLE " + BOOKMARKS_TABLE + " (prayer_id INTEGER PRIMARY KEY, position INTEGER NOT NULL)"
            db.execSQL(createBookmarks)

            val createRecents =
                "CREATE TABLE " + RECENTS_TABLE + " (prayer_id INTEGER PRIMARY KEY, access_time INTEGER NOT NULL)"
            db.execSQL(createRecents)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            L.i("onUpgrade - old: " + oldVersion + ", new: " + newVersion)
        }
    }

    // region Listener management
    interface Listener {
        @UiThread
        fun onBookmarkAdded(prayerId: Long) {
        }

        @UiThread
        fun onBookmarkDeleted(prayerId: Long) {
        }

        @UiThread
        fun onPrayerAccessed(prayerId: Long) {
        }

        @UiThread
        fun onRecentsCleared() {
        }
    }

    @AnyThread
    fun addListener(listener: Listener) {
        val ref = WeakReference<Listener?>(listener)
        listeners.add(ref)
    }

    @AnyThread
    private fun notifyBookmarkAdded(prayerId: Long) {
        App.runOnUiThread(object : UiRunnable {
            override fun run() {
                for (ref in listeners) {
                    val l = ref.get()
                    if (l == null) {
                        continue
                    }
                    l.onBookmarkAdded(prayerId)
                }
            }
        })
    }

    @AnyThread
    private fun notifyBookmarkDeleted(prayerId: Long) {
        App.runOnUiThread(object : UiRunnable {
            override fun run() {
                for (ref in listeners) {
                    val l = ref.get()
                    if (l == null) {
                        continue
                    }
                    l.onBookmarkDeleted(prayerId)
                }
            }
        })
    }

    @AnyThread
    private fun notifyPrayerAccessed(prayerId: Long) {
        App.runOnUiThread(object : UiRunnable {
            override fun run() {
                for (ref in listeners) {
                    val l = ref.get()
                    if (l == null) {
                        continue
                    }
                    l.onPrayerAccessed(prayerId)
                }
            }
        })
    }

    @AnyThread
    private fun notifyRecentsCleared() {
        App.runOnUiThread(object : UiRunnable {
            override fun run() {
                for (ref in listeners) {
                    val l = ref.get()
                    if (l == null) {
                        continue
                    }
                    l.onRecentsCleared()
                }
            }
        })
    }

    @AnyThread
    fun removeListener(listener: Listener) {
        var i = 0
        while (i < listeners.size) {
            val ref = listeners.get(i)
            val l = ref.get()
            if (l == null || l === listener) {
                listeners.removeAt(i)
                continue
            }
            i++
        }
    } //endregion

    companion object {
        private const val BOOKMARKS_TABLE = "bookmarks"
        private const val BOOKMARKS_COL_PRAYER_ID = "prayer_id"
        private const val BOOKMARKS_COL_POSITION = "position"

        private const val RECENTS_TABLE = "recents"
        private const val RECENTS_COL_PRAYER_ID = "prayer_id"
        private const val RECENTS_COL_ACCESS_TIME = "access_time"

        private var singleton: UserDB? = null
        @JvmStatic
        fun get(): UserDB {
            if (singleton == null) {
                throw RuntimeException("UserDD singleton needs to be initialized at app launch")
            }
            return singleton!!
        }


        @JvmStatic
        fun set(db: UserDB) {
            singleton = db
        }
    }
}
