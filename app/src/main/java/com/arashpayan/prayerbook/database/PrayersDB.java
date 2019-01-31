/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.arashpayan.prayerbook.Language;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author arash
 */
@SuppressWarnings("WeakerAccess")
public class PrayersDB {

    private static PrayersDB singleton = null;
    public static File databaseFile;
    
    private final SQLiteDatabase pbDatabase;
    
    private final static String PRAYERS_TABLE    = "prayers";

    public final static String ID_COLUMN                = "id";
    public final static String CATEGORY_COLUMN          = "category";
    public final static String LANGUAGE_COLUMN          = "language";
    public final static String OPENINGWORDS_COLUMN      = "openingWords";
    public final static String AUTHOR_COLUMN            = "author";
    public final static String PRAYERTEXT_COLUMN        = "prayerText";
    public final static String CITATION_COLUMN          = "citation";
    public final static String WORDCOUNT_COLUMN         = "wordCount";
    public final static String SEARCHTEXT_COLUMN        = "searchText";
    
    private final HashMap<String, Integer> prayerCountCache;
    private final ConcurrentHashMap<Long, PrayerSummary> summaryCache = new ConcurrentHashMap<>();
    
    private PrayersDB() {
        pbDatabase = SQLiteDatabase.openDatabase(databaseFile.toString(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        
        prayerCountCache = new HashMap<>();
    }
    
    public synchronized static PrayersDB get() {
        if (singleton == null) {
            singleton = new PrayersDB();
        }
        
        return singleton;
    }

    @WorkerThread
    @NonNull
    public ArrayList<String> getCategories(Language language) {
        String[] cols = {CATEGORY_COLUMN};
        String[] args = {language.code};

        ArrayList<String> categories = new ArrayList<>();
        try (Cursor c = pbDatabase.query(true, PRAYERS_TABLE, cols, LANGUAGE_COLUMN+"=?", args, null, null, CATEGORY_COLUMN+" ASC", null)) {
            while (c.moveToNext()) {
                categories.add(c.getString(0));
            }
        }

        return categories;
    }

    @WorkerThread
    public int getPrayerCountForCategory(@NonNull String category, @NonNull String language) {
        // check the cache first
        String key = language + category;
        Integer cachedCount = prayerCountCache.get(key);
        if (cachedCount != null) {
            return cachedCount;
        }
        
        String[] selectionArgs = {category, language};
        Cursor cursor = pbDatabase.rawQuery(
                "SELECT COUNT(id) FROM prayers WHERE category=? and language=?",
                selectionArgs);
        
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            prayerCountCache.put(language+category, count);
            return count;
        }
        
        // should never happen
        return 0;
    }

    @WorkerThread
    @NonNull
    public ArrayList<Long> getPrayerIds(String category, Language language) {
        String[] cols = {ID_COLUMN};
        String selectionClause = "category=? AND language=?";
        String[] selectionArgs = {category, language.code};

        ArrayList<Long> ids = new ArrayList<>();
        try (Cursor c = pbDatabase.query(true, PRAYERS_TABLE, cols, selectionClause, selectionArgs, null, null, OPENINGWORDS_COLUMN + " ASC", null)) {
            while (c.moveToNext()) {
                ids.add(c.getLong(0));
            }
        }

        return ids;
    }

    public Cursor getPrayersWithKeywords(String []keywords, Language[] languages) {
        String []cols = {ID_COLUMN, OPENINGWORDS_COLUMN, CATEGORY_COLUMN, AUTHOR_COLUMN, WORDCOUNT_COLUMN };
        StringBuilder whereClause = new StringBuilder();
        boolean firstKeyword = true;
        for (String kw : keywords) {
            if (kw.isEmpty()) {
                continue;
            }
            if (!firstKeyword) {
                whereClause.append(" AND");
            } else {
                firstKeyword = false;
            }

            whereClause.append(" searchText LIKE '%");
            whereClause.append(kw);
            whereClause.append("%'");
        }

        // build the language portion of the query
        StringBuilder languageClause = new StringBuilder();
        for (int i=0; i<languages.length; i++) {
            if (i == languages.length-1) {
                languageClause.append("language='");
                languageClause.append(languages[i].code);
                languageClause.append("'");
            } else {
                languageClause.append("language='");
                languageClause.append(languages[i].code);
                languageClause.append("' OR ");
            }
        }

        // append the languages to the clause
        whereClause.append(" AND (");
        whereClause.append(languageClause);
        whereClause.append(")");

        return pbDatabase.query(PRAYERS_TABLE, cols, whereClause.toString(), null, null, null, LANGUAGE_COLUMN);
    }

    @WorkerThread
    @NonNull
    public Prayer getPrayer(long prayerId) {
        String[] cols = {PRAYERTEXT_COLUMN, AUTHOR_COLUMN, CITATION_COLUMN, SEARCHTEXT_COLUMN, LANGUAGE_COLUMN};
        String selectionClause = ID_COLUMN + "=?";
        String[] selectionArgs = {Long.valueOf(prayerId).toString()};

        try (Cursor c = pbDatabase.query(PRAYERS_TABLE, cols, selectionClause, selectionArgs, null, null, null)) {
            if (!c.moveToFirst()) {
                throw new RuntimeException("Invalid prayer id: " + prayerId);
            }

            Prayer p = new Prayer();
            p.prayerId = prayerId;
            p.text = c.getString(c.getColumnIndexOrThrow(PRAYERTEXT_COLUMN));
            p.author = c.getString(c.getColumnIndexOrThrow(AUTHOR_COLUMN));
            p.citation = c.getString(c.getColumnIndexOrThrow(CITATION_COLUMN));
            p.searchText = c.getString(c.getColumnIndexOrThrow(SEARCHTEXT_COLUMN));
            String langCode = c.getString(c.getColumnIndexOrThrow(LANGUAGE_COLUMN));
            p.language = Language.get(langCode);
            return p;
        }
    }



    @Nullable
    @WorkerThread
    public PrayerSummary getPrayerSummary(long prayerId) {
        if (summaryCache.containsKey(prayerId)) {
            return summaryCache.get(prayerId);
        }

        String[] cols = {OPENINGWORDS_COLUMN,
                CATEGORY_COLUMN,
                AUTHOR_COLUMN,
                LANGUAGE_COLUMN,
                WORDCOUNT_COLUMN};
        String selectionClause = ID_COLUMN + "=?";
        String[] selectionArgs = {Long.valueOf(prayerId).toString()};

        try (Cursor c = pbDatabase.query(PRAYERS_TABLE, cols, selectionClause, selectionArgs, null, null, null)) {
            if (!c.moveToFirst()) {
                return null;
            }

            PrayerSummary ps = new PrayerSummary();
            ps.prayerId = prayerId;
            ps.openingWords = c.getString(c.getColumnIndexOrThrow(OPENINGWORDS_COLUMN));
            ps.category = c.getString(c.getColumnIndexOrThrow(CATEGORY_COLUMN));
            ps.author = c.getString(c.getColumnIndexOrThrow(AUTHOR_COLUMN));
            String langCode = c.getString(c.getColumnIndexOrThrow(LANGUAGE_COLUMN));
            ps.language = Language.get(langCode);
            ps.wordCount = c.getInt(c.getColumnIndexOrThrow(WORDCOUNT_COLUMN));

            summaryCache.put(prayerId, ps);

            return ps;
        }
    }
}
