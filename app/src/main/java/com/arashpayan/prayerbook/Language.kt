/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.util.Locale

enum class Language(@JvmField val code: String, @JvmField val humanName: Int, @JvmField val rightToLeft: Boolean) : Parcelable {
    Czech("cs", R.string.czech, false),
    German("de", R.string.german, false),
    English("en", R.string.english, false),
    Spanish("es", R.string.spanish, false),
    Persian("fa", R.string.persian, true),
    Fijian("fj", R.string.fijian, false),
    French("fr", R.string.french, false),
    Icelandic("is", R.string.icelandic, false),
    Dutch("nl", R.string.dutch, false),
    Slovak("sk", R.string.slovak, false);

    val locale: Locale

    init {
        this.locale = Locale.forLanguageTag(code)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(code)
    }

    companion object {
        fun get(code: String?): Language {
            for (l in entries) {
                if (l.code == code) {
                    return l
                }
            }

            return Language.English
        }

        val CREATOR: Creator<Language?> = object : Creator<Language?> {
            override fun createFromParcel(p: Parcel): Language {
                val code = p.readString()
                return get(code)
            }

            override fun newArray(size: Int): Array<Language?> {
                return arrayOfNulls<Language>(size)
            }
        }
    }
}
