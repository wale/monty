package au.id.wale.monty.util.download

interface DownloadProgressCallback {
    fun onProgress(progress: Double)
}