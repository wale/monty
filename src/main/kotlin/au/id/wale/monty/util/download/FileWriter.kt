package au.id.wale.monty.util.download

import java.io.BufferedInputStream
import java.io.InputStream
import java.io.OutputStream

class FileWriter(
    private val outputStream: OutputStream,
    private val progressCallback: DownloadProgressCallback
) : AutoCloseable {

    private val CHUNK_SIZE = 1024
    override fun close() {
        outputStream.close()
    }

    fun write(stream: InputStream, length: Double): Long {
        BufferedInputStream(stream).use { input ->
            val dataBuffer = ByteArray(CHUNK_SIZE)
            var readBytes: Int
            var totalBytes: Long = 0
            while (input.read(dataBuffer).also { readBytes = it } != -1) {
                totalBytes += readBytes.toLong()
                outputStream.write(dataBuffer, 0, readBytes)
                progressCallback.onProgress(totalBytes / length * 100.0)
            }
            return totalBytes
        }
    }
}