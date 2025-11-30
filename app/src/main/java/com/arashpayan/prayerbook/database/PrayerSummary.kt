package com.arashpayan.prayerbook.database

import com.arashpayan.prayerbook.Language

class PrayerSummary {
    var prayerId: Long = 0
    @JvmField
    var openingWords: String? = null
    @JvmField
    var category: String? = null
    @JvmField
    var author: String? = null
    var language: Language? = null
    @JvmField
    var wordCount: Int = 0
}
