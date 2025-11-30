package com.arashpayan.prayerbook.database

import com.arashpayan.prayerbook.Language

class Prayer {
    @JvmField
    var prayerId: Long = 0
    @JvmField
    var text: String? = null
    @JvmField
    var citation: String? = null
    @JvmField
    var searchText: String? = null
    @JvmField
    var author: String? = null
    @JvmField
    var language: Language? = null
}
