package com.example.androidllmapp

import android.os.AsyncTask
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject

class OpenAITask(
    private val prompt: String,
    private val callback: (String) -> Unit
) : AsyncTask<Void, Void, String>() {

    private val client = OkHttpClient()
    private val apiKey = "YOUR_API_KEY"

    override fun doInBackground(vararg params: Void?): String {
        val url = "https://api.openai.com/v1/chat/completions"

        val json = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val responseJson = JSONObject(responseBody)
                    val choices = responseJson.getJSONArray("choices")
                    choices.getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()
                } else {
                    "Error: ${response.code} ${response.message}"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Exception: ${e.message}"
        }
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        callback(result)
    }
}