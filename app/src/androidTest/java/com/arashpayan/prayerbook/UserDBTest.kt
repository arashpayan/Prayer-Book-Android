package com.arashpayan.prayerbook

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arashpayan.prayerbook.database.UserDB
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDBTest {
    @Test
    fun testBookmarks() {

        if (ctx == null) return

        val db = UserDB(ctx!!, true)

        // Make sure everything works when it's empty
        val prayerId: Long = 33
        Assert.assertFalse(db.isBookmarked(prayerId))

        var bookmarks = db.bookmarks
        Assert.assertEquals(0, bookmarks.size.toLong())

        // add an id
        Assert.assertTrue(db.addBookmark(prayerId))

        // it should be bookmarked
        Assert.assertTrue(db.isBookmarked(prayerId))

        // some other random id should not be
        Assert.assertFalse(db.isBookmarked(prayerId + 1))

        bookmarks = db.bookmarks
        Assert.assertEquals(1, bookmarks.size.toLong())
        Assert.assertEquals(prayerId, bookmarks.get(0) as Long)

        // add the same prayer again. nothing should change as a result
        Assert.assertTrue(db.addBookmark(prayerId))
        Assert.assertTrue(db.isBookmarked(prayerId))
        bookmarks = db.bookmarks
        Assert.assertEquals(1, bookmarks.size.toLong())
        Assert.assertEquals(prayerId, bookmarks.get(0) as Long)

        // delete it and we should be back to the empty state
        db.deleteBookmark(prayerId)
        Assert.assertFalse(db.isBookmarked(prayerId))
        bookmarks = db.bookmarks
        Assert.assertEquals(0, bookmarks.size.toLong())

        // try to add more than one bookmark
        val anotherPrayerId: Long = 44
        db.addBookmark(prayerId)
        db.addBookmark(anotherPrayerId)
        bookmarks = db.bookmarks
        Assert.assertEquals(2, bookmarks.size.toLong())
        Assert.assertTrue(db.isBookmarked(prayerId))
        Assert.assertTrue(db.isBookmarked(anotherPrayerId))
        Assert.assertFalse(db.isBookmarked(55))
    }

    @Test
    fun testRecents() {
        if (ctx == null) return

        val db = UserDB(ctx!!, true)

        var recents = db.recents
        Assert.assertEquals(0, recents.size.toLong())

        val prayerId: Long = 33
        Assert.assertTrue(db.accessedPrayer(prayerId))

        recents = db.recents
        Assert.assertEquals(1, recents.size.toLong())
        Assert.assertEquals(prayerId, recents.get(0) as Long)

        val anotherPrayerId: Long = 44
        try {
            Thread.sleep(5)
        } catch (ignore: Throwable) {
        }
        Assert.assertTrue(db.accessedPrayer(anotherPrayerId))

        recents = db.recents
        Assert.assertEquals(2, recents.size.toLong())
        Assert.assertEquals(anotherPrayerId, recents.get(0) as Long)
        Assert.assertEquals(prayerId, recents.get(1) as Long)

        // access the first prayer again and make sure the recents get reordered
        try {
            Thread.sleep(5)
        } catch (ignore: Throwable) {
        }
        Assert.assertTrue(db.accessedPrayer(prayerId))
        recents = db.recents
        Assert.assertEquals(2, recents.size.toLong())
        Assert.assertEquals(prayerId, recents.get(0) as Long)
        Assert.assertEquals(anotherPrayerId, recents.get(1) as Long)

        // clear the recents and make sure
        db.clearRecents()
        recents = db.recents
        Assert.assertEquals(0, recents.size.toLong())
    }

    companion object {
        private var ctx: Context? = null

        fun setUp() {
            ctx = InstrumentationRegistry.getInstrumentation().getContext()
        }
    }
}
