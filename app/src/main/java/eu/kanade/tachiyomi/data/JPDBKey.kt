package eu.kanade.tachiyomi.data

import android.app.Application
import android.content.Context
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object JPDBKey {
    private const val PREFS_NAME = "jpdb_prefs"
    private const val KEY_API_KEY = "jpdb_api_key"

    private val context: Application = Injekt.get()
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_API_KEY, value).apply()
        }
}
