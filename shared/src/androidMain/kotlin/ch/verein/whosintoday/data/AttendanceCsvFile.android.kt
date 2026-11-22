package ch.verein.whosintoday.data

import java.io.File
import java.io.IOException

internal actual fun writeAttendanceCsvFile(path: String, bytes: ByteArray): Boolean {
    val file = File(path)
    return try {
        val temporary = File.createTempFile("attendance-", ".tmp", file.absoluteFile.parentFile)
        try {
            temporary.writeBytes(bytes)
            temporary.renameTo(file)
        } finally {
            temporary.delete()
        }
    } catch (_: IOException) {
        false
    }
}
