/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * @author Arash
 */
public enum Language implements Parcelable {

    Czech("cs", R.string.czech, false),
    English("en", R.string.english, false),
    Spanish("es", R.string.spanish, false),
    Persian("fa", R.string.persian, true),
    Fijian("fj", R.string.fijian, false),
    French("fr", R.string.french, false),
    Dutch("nl", R.string.dutch, false),
    Slovak("sk", R.string.slovak, false);

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

    public static final Parcelable.Creator<Language> CREATOR = new Parcelable.Creator<Language>() {
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
