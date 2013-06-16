/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.arashpayan.util.L;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author arash
 */
public class Database {

    public enum Language implements Parcelable {

        Dutch("nl", R.string.nederlands),
        English("en", R.string.english),
        French("fr", R.string.francais),
        Persian("fa", R.string.farsi),
        Spanish("es", R.string.espanol);

        public final String code;
        public final int humanName;

        Language(String code, int humanName) {
            this.code = code;
            this.humanName = humanName;
        }

        public static Language get(String code) {
            for (Language l : values()) {
                if (l.code.equals(code)) {
                    return l;
                }
            }

            return English;
        }

        public static final Creator<Language> CREATOR = new Parcelable.Creator<Language>() {
            public Language createFromParcel(Parcel p) {
                String code = p.readString();
                return get(code);
            }

            public Language[] newArray(int size) {
                return new Language[size];
            }
        };

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(code);
        }
    }

    private static Database singleton = null;
    public static File databaseFile;
    
    private SQLiteDatabase pbDatabase;
    
//    private String[] languages = {"en", "es", "fa", "fr"};
    
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
    
    private HashMap<String, Integer> prayerCountCache;
    
    private Database() {
        pbDatabase = SQLiteDatabase.openDatabase(databaseFile.toString(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        if (pbDatabase == null)
            L.i("Got a null when trying to open the database.");
        else
            L.i("Opened prayer database");
        
        prayerCountCache = new HashMap<String, Integer>();
    }
    
    public synchronized static Database getInstance() {
        if (singleton == null)
            singleton = new Database();
        
        return singleton;
    }
    
    public Cursor getCategories(Language language) {
        String[] cols = {CATEGORY_COLUMN};
        String[] selectionArgs = {language.code};
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
    
    public int getPrayerCountForCategory(String category, String language) {
        // check the cache first
        if (prayerCountCache.containsKey(language + category))
            return prayerCountCache.get(language + category);
        
        String[] selectionArgs = {category, language};
        Cursor cursor = pbDatabase.rawQuery(
                "SELECT COUNT(id) FROM prayers WHERE category=? and language=?",
                selectionArgs);
        
        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            prayerCountCache.put(language+category, new Integer(count));
            return count;
        }
        
        // should never happen
        return 0;
    }
    
//    private Map<String, List> getCategories() {
//        for (String lang : languages)
//        {
//
//        }
//
//        return null;
//    }
    
    public Cursor getPrayers(String category, Language language) {
        String[] cols = {ID_COLUMN,
                            OPENINGWORDS_COLUMN,
                            CATEGORY_COLUMN,
                            AUTHOR_COLUMN,
                            LANGUAGE_COLUMN,
                            WORDCOUNT_COLUMN};
        String selectionClause = "category=? AND language=?";
        String[] selectionArgs = {category, language.code};
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
    
    public Cursor getPrayer(long prayerId) {
        String[] cols = {PRAYERTEXT_COLUMN, AUTHOR_COLUMN, CITATION_COLUMN, SEARCHTEXT_COLUMN};
        String selectionClause = ID_COLUMN + "=?";
        String[] selectionArgs = {new Long(prayerId).toString()};
        Cursor cursor = pbDatabase.query(PRAYERS_TABLE, cols, selectionClause, selectionArgs, null, null, null);
        
        return cursor;
    }
}
