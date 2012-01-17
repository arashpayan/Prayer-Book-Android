/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 * @author arash
 */
public class Database {
    
    static Database singleton = null;
    public static File databaseFile;
    
    private SQLiteDatabase pbDatabase;
    
    private String[] languages = {"en", "es", "fa", "fr"};
    
    private final static String PRAYERS_TABLE    = "prayers";
    
    public final static String ID_COLUMN            = "id";
    public final static String CATEGORY_COLUMN      = "category";
    public final static String LANGUAGE_COLUMN      = "language";
    public final static String OPENINGWORDS_COLUMN  = "openingWords";
    public final static String AUTHOR_COLUMN        = "author";
    public final static String PRAYERTEXT_COLUMN    = "prayerText";
    public final static String CITATION_COLUMN      = "citation";
    public final static String WORDCOUNT_COLUMN     = "wordCount";
    
    private Database() {
        pbDatabase = SQLiteDatabase.openDatabase(databaseFile.toString(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        if (pbDatabase == null)
            Log.w(PrayerBook.TAG, "Got a null when trying to popen the database.");
        else
            Log.i(PrayerBook.TAG, "Opened prayer database");
    }
    
    public synchronized static Database getInstance() {
        if (singleton == null)
            singleton = new Database();
        
        return singleton;
    }
    
    public Cursor getCategories(String lang) {
        String[] cols = {CATEGORY_COLUMN};
        String[] selectionArgs = {lang};
        Cursor cursor = pbDatabase.query(
                true,
                PRAYERS_TABLE,
                cols,
                LANGUAGE_COLUMN+"=?",
                selectionArgs,
                null,
                null,
                null,
                null);
                
        return cursor;
    }
    
    private Map<String, List> getCategories() {
        for (String lang : languages)
        {
            
        }
        
        return null;
    }
    
    public Cursor getPrayers(String category) {
        String[] cols = {ID_COLUMN,
//                            PRAYERTEXT_COLUMN,
                            OPENINGWORDS_COLUMN,
//                            CITATION_COLUMN,
                            CATEGORY_COLUMN,
                            AUTHOR_COLUMN,
                            LANGUAGE_COLUMN,
                            WORDCOUNT_COLUMN};
        String selectionClause = "category=? AND language='en'";
        String[] selectionArgs = {category};
        Cursor cursor = pbDatabase.query(
                true,
                PRAYERS_TABLE,
                cols,
                selectionClause,
                selectionArgs,
                null,
                null,
                null,
                null);
        
        return cursor;
    }
}
