package eu.kanade.tachiyomi.ui.player

import android.os.Handler
import android.os.Looper
import android.util.Log
import eu.kanade.tachiyomi.data.JPDBKey
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

fun sendRequest(text: String, onResult: (String) -> Unit = {}) {
    val apiToken = JPDBKey.apiKey

    if (apiToken.isBlank()) {
        Log.e("API_REQUEST", "JPDB API key is not set")
        Handler(Looper.getMainLooper()).post {
            onResult("ERROR: JPDB API key not configured. Please set it in settings.")
        }
        return
    }

    val client = OkHttpClient()

    val json = """
        {
            "text": "$text",
            "token_fields": ["vocabulary_index", "furigana"],
            "vocabulary_fields": ["reading", "spelling", "meanings"]
        }
    """.trimIndent()

    val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

    val request = Request.Builder()
        .url("https://jpdb.io/api/v1/parse")
        .addHeader("Authorization", "Bearer $apiToken")
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("API_REQUEST", "Request failed", e)
            Handler(Looper.getMainLooper()).post {
                onResult("ERROR: Failed to connect to JPDB API. Check your internet connection.")
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body.string()

            if (response.isSuccessful) {
                Log.d("API_REQUEST", "Success: $responseBody")
                Handler(Looper.getMainLooper()).post {
                    onResult(responseBody)
                }
            } else {
                val errorMsg = when (response.code) {
                    401 -> "ERROR: Invalid JPDB API key. Check your configuration."
                    403 -> "ERROR: Access forbidden. Check your JPDB account."
                    429 -> "ERROR: Rate limit exceeded. Try again later."
                    else -> "ERROR: API returned error ${response.code}. Response: $responseBody"
                }
                Log.e("API_REQUEST", errorMsg)
                Handler(Looper.getMainLooper()).post {
                    onResult(errorMsg)
                }
            }
            response.close()
        }
    })
}

fun parseJPDBResponse(jsonString: String): String {
    if (jsonString.isBlank()) {
        return "No response from API"
    }

    if (jsonString.startsWith("ERROR:")) {
        return jsonString
    }

    return try {
        val json = JSONObject(jsonString)

        if (!json.has("tokens") || !json.has("vocabulary")) {
            Log.e("JPDB_PARSE", "Missing required fields in JPDB response")
            return "Invalid response format from JPDB API"
        }

        val tokens = json.getJSONArray("tokens")
        val vocabulary = json.getJSONArray("vocabulary")

        val result = StringBuilder()

        for (i in 0 until tokens.length()) {
            val tokenArray = tokens.getJSONArray(i)

            val vocabIndex = tokenArray.optInt(0, -1)

            val furigana = tokenArray.optJSONArray(1)?.let { furArray ->
                StringBuilder().apply {
                    for (k in 0 until furArray.length()) {
                        val item = furArray.get(k)
                        if (item is JSONArray && item.length() > 1) {
                            append(item.getString(1))
                        } else if (item is String) {
                            append(item)
                        }
                    }
                }.toString()
            } ?: ""

            if (vocabIndex >= 0 && vocabIndex < vocabulary.length()) {
                val vocabArray = vocabulary.getJSONArray(vocabIndex)

                val reading = vocabArray.optString(0, "")
                val spelling = vocabArray.optString(1, "")
                val meaningsArray = vocabArray.optJSONArray(2)

                result.append("$spelling")
                if (furigana.isNotEmpty()) {
                    result.append(" ($furigana)")
                }
                result.append("\n")

                if (meaningsArray != null && meaningsArray.length() > 0) {
                    result.append("Definitions: ")
                    val definitionsList = mutableListOf<String>()

                    for (j in 0 until minOf(meaningsArray.length(), 2)) {
                        try {
                            val meaning = meaningsArray.getString(j)
                            if (meaning.isNotEmpty()) {
                                definitionsList.add(meaning)
                            }
                        } catch (e: Exception) {
                            Log.d("JPDB_PARSE", "Could not parse meaning at index $j: ${e.message}")
                        }
                    }

                    result.append(definitionsList.joinToString(", "))
                    result.append("\n")
                }
                result.append("---\n")
            }
        }

        if (result.isEmpty()) "No vocabulary found" else result.toString().trimEnd()
    } catch (e: Exception) {
        Log.e("JPDB_PARSE", "Error parsing JPDB response: ${e.message}", e)
        "Error parsing API response: ${e.message}"
    }
}

