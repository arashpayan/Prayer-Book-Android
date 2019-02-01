package com.arashpayan.prayerbook.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.arashpayan.prayerbook.App;
import com.arashpayan.prayerbook.thread.UiRunnable;
import com.arashpayan.util.L;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserDB {

    private final static String BOOKMARKS_TABLE = "bookmarks";
    private final static String BOOKMARKS_COL_PRAYER_ID = "prayer_id";
    private final static String BOOKMARKS_COL_POSITION = "position";

    private final static String RECENTS_TABLE = "recents";
    private final static String RECENTS_COL_PRAYER_ID = "prayer_id";
    private final static String RECENTS_COL_ACCESS_TIME = "access_time";

    private static UserDB singleton;
    @NonNull private final Helper helper;
    @NonNull private final CopyOnWriteArrayList<WeakReference<Listener>> listeners = new CopyOnWriteArrayList<>();

    public UserDB(@NonNull Context ctx, boolean inMemory) {
        helper = new Helper(ctx, inMemory);
    }

    @NonNull
    public static UserDB get() {
        if (singleton == null) {
            throw new RuntimeException("UserDD singleton needs to be initialized at app launch");
        }
        return singleton;
    }


    public static void set(@NonNull UserDB db) {
        singleton = db;
    }

    //region Bookmarks

    @WorkerThread
    // returns true on success. false otherwise.
    public boolean addBookmark(long prayerId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String insertSQL = "INSERT OR IGNORE INTO "+BOOKMARKS_TABLE +
                "("  +BOOKMARKS_COL_PRAYER_ID + ", " + BOOKMARKS_COL_POSITION +
                ") SELECT " + prayerId + ", COALESCE(MAX(" + BOOKMARKS_COL_POSITION + "), 0)+1 FROM " + BOOKMARKS_TABLE;
        try {
            db.execSQL(insertSQL);
        } catch (SQLException ex) {
            L.w("Error adding bookmark", ex);
            return false;
        }

        notifyBookmarkAdded(prayerId);

        return true;
    }

    @WorkerThread
    public void deleteBookmark(long prayerId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(BOOKMARKS_TABLE, "prayer_id=?", new String[]{""+prayerId});
        notifyBookmarkDeleted(prayerId);
    }

    @WorkerThread
    @NonNull
    public ArrayList<Long> getBookmarks() {
        ArrayList<Long> bookmarks = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.query(BOOKMARKS_TABLE, new String[]{BOOKMARKS_COL_PRAYER_ID}, null, null, null, null, "position ASC")) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(BOOKMARKS_COL_PRAYER_ID));
                bookmarks.add(id);
            }
        }

        return bookmarks;
    }

    @WorkerThread
    public boolean isBookmarked(long prayerId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] cols = new String[]{BOOKMARKS_COL_PRAYER_ID};
        String selection = BOOKMARKS_COL_PRAYER_ID + "=?";
        String[] args = new String[]{""+prayerId};
        try (Cursor cursor = db.query(BOOKMARKS_TABLE, cols, selection, args, null, null, null)) {
            return cursor.moveToNext();
        }
    }

    //endregion


    //region Recents

    @WorkerThread
    public boolean accessedPrayer(long prayerId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String insertSql = "INSERT OR REPLACE INTO " + RECENTS_TABLE + " ("+RECENTS_COL_PRAYER_ID + ", " + RECENTS_COL_ACCESS_TIME + ") VALUES (?, ?)";
        try {
            db.execSQL(insertSql, new Object[]{"" + prayerId, "" + System.currentTimeMillis()});
        } catch (SQLException ex) {
            L.w("Error updating prayer access time", ex);
            return false;
        }

        notifyPrayerAccessed(prayerId);

        return true;
    }

    @WorkerThread
    public void clearRecents() {
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.delete(RECENTS_TABLE, null, null);
        } catch (SQLException ex) {
            L.w("Error deleting recent records", ex);
        }
    }

    @WorkerThread
    @NonNull
    public ArrayList<Long> getRecents() {
        ArrayList<Long> recents = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.query(RECENTS_TABLE, new String[]{RECENTS_COL_PRAYER_ID}, null, null, null, null, RECENTS_COL_ACCESS_TIME+" DESC", "50")) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(RECENTS_COL_PRAYER_ID));
                recents.add(id);
            }
        }

        return recents;
    }

    //endregion

    private static class Helper extends SQLiteOpenHelper {
        private Helper(@NonNull Context ctx, boolean inMemory) {
            super(ctx, inMemory ? null : "user_data", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createBookmarks = "CREATE TABLE " + BOOKMARKS_TABLE + " (prayer_id INTEGER PRIMARY KEY, position INTEGER NOT NULL)";
            db.execSQL(createBookmarks);

            String createRecents = "CREATE TABLE " + RECENTS_TABLE + " (prayer_id INTEGER PRIMARY KEY, access_time INTEGER NOT NULL)";
            db.execSQL(createRecents);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            L.i("onUpgrade - old: " + oldVersion + ", new: " + newVersion);
        }
    }

    // region Listener management

    public interface Listener {
        @UiThread
        default void onBookmarkAdded(long prayerId) {}
        @UiThread
        default void onBookmarkDeleted(long prayerId) {}
        @UiThread
        default void onPrayerAccessed(long prayerId) {}
    }

    @AnyThread
    public void addListener(@NonNull Listener listener) {
        WeakReference<Listener> ref = new WeakReference<>(listener);
        listeners.add(ref);
    }

    @AnyThread
    private void notifyBookmarkAdded(long prayerId) {
        App.runOnUiThread(new UiRunnable() {
            @Override
            public void run() {
                for (WeakReference<Listener> ref : listeners) {
                    Listener l = ref.get();
                    if (l == null) {
                        continue;
                    }
                    l.onBookmarkAdded(prayerId);
                }
            }
        });
    }

    @AnyThread
    private void notifyBookmarkDeleted(long prayerId) {
        App.runOnUiThread(new UiRunnable() {
            @Override
            public void run() {
                for (WeakReference<Listener> ref : listeners) {
                    Listener l = ref.get();
                    if (l == null) {
                        continue;
                    }
                    l.onBookmarkDeleted(prayerId);
                }
            }
        });
    }

    @AnyThread
    private void notifyPrayerAccessed(long prayerId) {
        App.runOnUiThread(new UiRunnable() {
            @Override
            public void run() {
                for (WeakReference<Listener> ref : listeners) {
                    Listener l = ref.get();
                    if (l == null) {
                        continue;
                    }
                    l.onPrayerAccessed(prayerId);
                }
            }
        });
    }

    @AnyThread
    public void removeListener(@NonNull Listener listener) {
        int i=0;
        while (i<listeners.size()) {
            WeakReference<Listener> ref = listeners.get(i);
            Listener l = ref.get();
            if (l == null || l == listener) {
                listeners.remove(i);
                continue;
            }
            i++;
        }
    }

    //endregion
}
