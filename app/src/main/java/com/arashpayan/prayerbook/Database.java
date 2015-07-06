/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.HashMap;

/**
 *
 * @author arash
 */
public class Database {

    private static Database singleton = null;
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
    
    private Database() {
        pbDatabase = SQLiteDatabase.openDatabase(databaseFile.toString(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        
        prayerCountCache = new HashMap<>();
    }
    
    public synchronized static Database getInstance() {
        if (singleton == null) {
            singleton = new Database();
        }
        
        return singleton;
    }
    
    public Cursor getCategories(Language language) {
        String[] cols = {CATEGORY_COLUMN};
        String[] selectionArgs = {language.code};
        return pbDatabase.query(
                true,
                PRAYERS_TABLE,
                cols,
                LANGUAGE_COLUMN+"=?",
                selectionArgs,
                null,
                null,
                CATEGORY_COLUMN + " ASC",
                null);
    }
    
    public int getPrayerCountForCategory(String category, String language) {
        // check the cache first
        if (prayerCountCache.containsKey(language + category)) {
            return prayerCountCache.get(language + category);
        }
        
        String[] selectionArgs = {category, language};
        Cursor cursor = pbDatabase.rawQuery(
                "SELECT COUNT(id) FROM prayers WHERE category=? and language=?",
                selectionArgs);
        
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            prayerCountCache.put(language+category, count);
            return count;
        }
        
        // should never happen
        return 0;
    }
    
    public Cursor getPrayers(String category, Language language) {
        String[] cols = {ID_COLUMN,
                            OPENINGWORDS_COLUMN,
                            CATEGORY_COLUMN,
                            AUTHOR_COLUMN,
                            LANGUAGE_COLUMN,
                            WORDCOUNT_COLUMN};
        String selectionClause = "category=? AND language=?";
        String[] selectionArgs = {category, language.code};
        return pbDatabase.query(
                true,
                PRAYERS_TABLE,
                cols,
                selectionClause,
                selectionArgs,
                null,
                null,
                OPENINGWORDS_COLUMN + " ASC",
                null);
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
    
    public Cursor getPrayer(long prayerId) {
        String[] cols = {PRAYERTEXT_COLUMN, AUTHOR_COLUMN, CITATION_COLUMN, SEARCHTEXT_COLUMN, LANGUAGE_COLUMN};
        String selectionClause = ID_COLUMN + "=?";
        String[] selectionArgs = {Long.valueOf(prayerId).toString()};

        return pbDatabase.query(PRAYERS_TABLE, cols, selectionClause, selectionArgs, null, null, null);
    }
}
