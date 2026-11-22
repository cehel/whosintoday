package ch.verein.whosintoday

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

internal fun Activity.shareAttendanceCsv(filename: String, bytes: ByteArray): Boolean {
    val cache = File(cacheDir, "attendance-exports")
    val expiry = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
    cache.listFiles()?.filter { it.lastModified() < expiry }?.forEach { it.deleteRecursively() }

    val directory = File(cache, UUID.randomUUID().toString())
    if (!directory.mkdirs()) return false
    val file = File(directory, filename)
    val launched = try {
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(this, "$packageName.csv", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, null))
        true
    } catch (_: IOException) {
        false
    } catch (_: ActivityNotFoundException) {
        false
    }
    if (!launched) directory.deleteRecursively()
    return launched
}
