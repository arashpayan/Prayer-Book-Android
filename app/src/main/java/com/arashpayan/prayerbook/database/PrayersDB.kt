/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.WorkerThread
import com.arashpayan.prayerbook.Language
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class PrayersDB private constructor() {
    private val pbDatabase: SQLiteDatabase

    private val prayerCountCache: HashMap<String?, Int?>
    private val summaryCache = ConcurrentHashMap<Long?, PrayerSummary?>()

    init {
        pbDatabase = SQLiteDatabase.openDatabase(
            databaseFile.toString(),
            null,
            SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
        )

        prayerCountCache = HashMap<String?, Int?>()
    }

    @WorkerThread
    fun getCategories(language: Language): ArrayList<String?> {
        val cols = arrayOf<String?>(CATEGORY_COLUMN)
        val args = arrayOf<String?>(language.code)

        val categories = ArrayList<String?>()
        pbDatabase.query(
            true,
            PRAYERS_TABLE,
            cols,
            LANGUAGE_COLUMN + "=?",
            args,
            null,
            null,
            CATEGORY_COLUMN + " ASC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                categories.add(c.getString(0))
            }
        }
        return categories
    }

    @WorkerThread
    fun getPrayerCountForCategory(category: String, language: String): Int {
        // check the cache first
        val key = language + category
        val cachedCount = prayerCountCache.get(key)
        if (cachedCount != null) {
            return cachedCount
        }

        val selectionArgs = arrayOf<String?>(category, language)
        val cursor = pbDatabase.rawQuery(
            "SELECT COUNT(id) FROM prayers WHERE category=? and language=?",
            selectionArgs
        )

        if (cursor.getCount() > 0) {
            cursor.moveToFirst()
            val count = cursor.getInt(0)
            cursor.close()
            prayerCountCache.put(language + category, count)
            return count
        }


        // should never happen
        return 0
    }

    @WorkerThread
    fun getPrayerIds(category: String?, language: Language): ArrayList<Long?> {
        val cols = arrayOf<String?>(ID_COLUMN)
        val selectionClause = "category=? AND language=?"
        val selectionArgs = arrayOf<String?>(category, language.code)

        val ids = ArrayList<Long?>()
        pbDatabase.query(
            true,
            PRAYERS_TABLE,
            cols,
            selectionClause,
            selectionArgs,
            null,
            null,
            OPENINGWORDS_COLUMN + " ASC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                ids.add(c.getLong(0))
            }
        }
        return ids
    }

    fun getPrayersWithKeywords(keywords: Array<String>, languages: ArrayList<Language>): Cursor {
        val cols = arrayOf<String?>(
            ID_COLUMN,
            OPENINGWORDS_COLUMN,
            CATEGORY_COLUMN,
            AUTHOR_COLUMN,
            WORDCOUNT_COLUMN
        )
        val whereClause = StringBuilder()
        var firstKeyword = true
        for (kw in keywords) {
            if (kw.isEmpty()) {
                continue
            }
            if (!firstKeyword) {
                whereClause.append(" AND")
            } else {
                firstKeyword = false
            }

            whereClause.append(" searchText LIKE '%")
            whereClause.append(kw)
            whereClause.append("%'")
        }

        // build the language portion of the query
        val languageClause = StringBuilder()
        for (i in languages.indices) {
            if (i == languages.size - 1) {
                languageClause.append("language='")
                languageClause.append(languages[i]!!.code)
                languageClause.append("'")
            } else {
                languageClause.append("language='")
                languageClause.append(languages[i]!!.code)
                languageClause.append("' OR ")
            }
        }

        // append the languages to the clause
        whereClause.append(" AND (")
        whereClause.append(languageClause)
        whereClause.append(")")

        return pbDatabase.query(
            PRAYERS_TABLE,
            cols,
            whereClause.toString(),
            null,
            null,
            null,
            LANGUAGE_COLUMN
        )
    }

    @WorkerThread
    fun getPrayer(prayerId: Long): Prayer? {
        val cols = arrayOf<String?>(
            PRAYERTEXT_COLUMN,
            AUTHOR_COLUMN,
            CITATION_COLUMN,
            SEARCHTEXT_COLUMN,
            LANGUAGE_COLUMN
        )
        val selectionClause: String = ID_COLUMN + "=?"
        val selectionArgs = arrayOf<String?>(prayerId.toString())

        pbDatabase.query(PRAYERS_TABLE, cols, selectionClause, selectionArgs, null, null, null)
            .use { c ->
                if (!c.moveToFirst()) {
                    return null
                }
                val p = Prayer()
                p.prayerId = prayerId
                p.text = c.getString(c.getColumnIndexOrThrow(PRAYERTEXT_COLUMN))
                p.author = c.getString(c.getColumnIndexOrThrow(AUTHOR_COLUMN))
                p.citation = c.getString(c.getColumnIndexOrThrow(CITATION_COLUMN))
                p.searchText = c.getString(c.getColumnIndexOrThrow(SEARCHTEXT_COLUMN))
                val langCode = c.getString(c.getColumnIndexOrThrow(LANGUAGE_COLUMN))
                p.language = Language.get(langCode)
                return p
            }
    }


    @WorkerThread
    fun getPrayerSummary(prayerId: Long): PrayerSummary? {
        if (summaryCache.containsKey(prayerId)) {
            return summaryCache.get(prayerId)
        }

        val cols = arrayOf<String?>(
            OPENINGWORDS_COLUMN,
            CATEGORY_COLUMN,
            AUTHOR_COLUMN,
            LANGUAGE_COLUMN,
            WORDCOUNT_COLUMN
        )
        val selectionClause: String = ID_COLUMN + "=?"
        val selectionArgs = arrayOf<String?>(prayerId.toString())

        pbDatabase.query(PRAYERS_TABLE, cols, selectionClause, selectionArgs, null, null, null)
            .use { c ->
                if (!c.moveToFirst()) {
                    return null
                }
                val ps = PrayerSummary()
                ps.prayerId = prayerId
                ps.openingWords = c.getString(c.getColumnIndexOrThrow(OPENINGWORDS_COLUMN))
                ps.category = c.getString(c.getColumnIndexOrThrow(CATEGORY_COLUMN))
                ps.author = c.getString(c.getColumnIndexOrThrow(AUTHOR_COLUMN))
                val langCode = c.getString(c.getColumnIndexOrThrow(LANGUAGE_COLUMN))
                ps.language = Language.get(langCode)
                ps.wordCount = c.getInt(c.getColumnIndexOrThrow(WORDCOUNT_COLUMN))

                summaryCache.put(prayerId, ps)
                return ps
            }
    }

    companion object {
        private var singleton: PrayersDB? = null
        @JvmField
        var databaseFile: File? = null

        private const val PRAYERS_TABLE = "prayers"

        const val ID_COLUMN: String = "id"
        const val CATEGORY_COLUMN: String = "category"
        const val LANGUAGE_COLUMN: String = "language"
        const val OPENINGWORDS_COLUMN: String = "openingWords"
        const val AUTHOR_COLUMN: String = "author"
        const val PRAYERTEXT_COLUMN: String = "prayerText"
        const val CITATION_COLUMN: String = "citation"
        const val WORDCOUNT_COLUMN: String = "wordCount"
        const val SEARCHTEXT_COLUMN: String = "searchText"

        @JvmStatic
        @Synchronized
        fun get(): PrayersDB {
            if (singleton == null) {
                singleton = PrayersDB()
            }

            return singleton!!
        }
    }
}
