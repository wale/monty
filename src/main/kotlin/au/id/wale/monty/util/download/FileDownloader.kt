package au.id.wale.monty.util.download

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.util.*

class FileDownloader(
    private val client: OkHttpClient,
    private val writer: FileWriter
) : AutoCloseable {
    fun download(url: String): Long {
        val request: Request = Request.Builder().url(url).build()
        val response: Response = client.newCall(request).execute()
        val responseBody: ResponseBody =
            response.body ?: throw IllegalStateException("Response doesn't contain a file")
        val length = response.header("Content-Length", "1")!!.toDouble()
        return writer.write(responseBody.byteStream(), length)
    }

    override fun close() {
        writer.close()
    }
}