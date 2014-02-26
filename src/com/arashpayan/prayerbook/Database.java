/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.File;
import java.util.HashMap;

/**
 *
 * @author arash
 */
public class Database {

    public enum Language implements Parcelable {

        Dutch("nl", R.string.nederlands, false),
        English("en", R.string.english, false),
        French("fr", R.string.francais, false),
        Persian("fa", R.string.farsi, true),
        Spanish("es", R.string.espanol, false);

        public final String code;
        public final int humanName;
        public final boolean rightToLeft;

        Language(String code, int humanName, boolean rightToLeft) {
            this.code = code;
            this.humanName = humanName;
            this.rightToLeft = rightToLeft;
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
        
        prayerCountCache = new HashMap<String, Integer>();
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
            prayerCountCache.put(language+category, Integer.valueOf(count));
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

            whereClause.append(" searchText LIKE %");
            whereClause.append(kw);
            whereClause.append("%");
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

        Cursor cursor = pbDatabase.query(PRAYERS_TABLE, cols, whereClause.toString(), null, null, null, LANGUAGE_COLUMN);

        return cursor;
    }
    
    public Cursor getPrayer(long prayerId) {
        String[] cols = {PRAYERTEXT_COLUMN, AUTHOR_COLUMN, CITATION_COLUMN, SEARCHTEXT_COLUMN, LANGUAGE_COLUMN};
        String selectionClause = ID_COLUMN + "=?";
        String[] selectionArgs = {Long.valueOf(prayerId).toString()};
        Cursor cursor = pbDatabase.query(PRAYERS_TABLE, cols, selectionClause, selectionArgs, null, null, null);
        
        return cursor;
    }
}
