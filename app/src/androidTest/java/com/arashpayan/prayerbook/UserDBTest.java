package com.arashpayan.prayerbook;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.arashpayan.prayerbook.database.UserDB;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class UserDBTest {

    private static Context ctx;

    public static void setUp() {
        ctx = InstrumentationRegistry.getInstrumentation().getContext();
    }

    @Test
    public void testBookmarks() {
        UserDB db = new UserDB(ctx, true);

        // Make sure everything works when it's empty
        long prayerId = 33;
        assertFalse(db.isBookmarked(prayerId));

        ArrayList<Long> bookmarks = db.getBookmarks();
        assertEquals(0, bookmarks.size());

        // add an id
        assertTrue(db.addBookmark(prayerId));

        // it should be bookmarked
        assertTrue(db.isBookmarked(prayerId));

        // some other random id should not be
        assertFalse(db.isBookmarked(prayerId+1));

        bookmarks = db.getBookmarks();
        assertEquals(1, bookmarks.size());
        assertEquals(prayerId, (long)bookmarks.get(0));

        // add the same prayer again. nothing should change as a result
        assertTrue(db.addBookmark(prayerId));
        assertTrue(db.isBookmarked(prayerId));
        bookmarks = db.getBookmarks();
        assertEquals(1, bookmarks.size());
        assertEquals(prayerId, (long)bookmarks.get(0));

        // delete it and we should be back to the empty state
        db.deleteBookmark(prayerId);
        assertFalse(db.isBookmarked(prayerId));
        bookmarks = db.getBookmarks();
        assertEquals(0, bookmarks.size());

        // try to add more than one bookmark
        long anotherPrayerId = 44;
        db.addBookmark(prayerId);
        db.addBookmark(anotherPrayerId);
        bookmarks = db.getBookmarks();
        assertEquals(2, bookmarks.size());
        assertTrue(db.isBookmarked(prayerId));
        assertTrue(db.isBookmarked(anotherPrayerId));
        assertFalse(db.isBookmarked(55));
    }

    @Test
    public void testRecents() {
        UserDB db = new UserDB(ctx, true);

        ArrayList<Long> recents = db.getRecents();
        assertEquals(0, recents.size());

        long prayerId = 33;
        assertTrue(db.accessedPrayer(prayerId));

        recents = db.getRecents();
        assertEquals(1, recents.size());
        assertEquals(prayerId, (long)recents.get(0));

        long anotherPrayerId = 44;
        try { Thread.sleep(5); }
        catch (Throwable ignore) {}
        assertTrue(db.accessedPrayer(anotherPrayerId));

        recents = db.getRecents();
        assertEquals(2, recents.size());
        assertEquals(anotherPrayerId, (long)recents.get(0));
        assertEquals(prayerId, (long)recents.get(1));

        // access the first prayer again and make sure the recents get reordered
        try { Thread.sleep(5); }
        catch (Throwable ignore) {}
        assertTrue(db.accessedPrayer(prayerId));
        recents = db.getRecents();
        assertEquals(2, recents.size());
        assertEquals(prayerId, (long)recents.get(0));
        assertEquals(anotherPrayerId, (long)recents.get(1));

        // clear the recents and make sure
        db.clearRecents();
        recents = db.getRecents();
        assertEquals(0, recents.size());
    }
}
