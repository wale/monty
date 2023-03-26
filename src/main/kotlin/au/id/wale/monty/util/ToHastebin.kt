package au.id.wale.monty.util

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

private val client = OkHttpClient()
fun String.uploadToHastebin(): String {
    val request = Request.Builder()
        .url("https://haste.erisa.uk/documents")
        .post(this.toRequestBody("text/plain; charset=utf8".toMediaType()))
        .build()
    val response = client.newCall(request).execute()
    val json = JSONObject(response.body!!.string())

    return json.getString("key")
}