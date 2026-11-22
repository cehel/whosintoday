package ch.verein.whosintoday.data

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.writeToFile

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
internal actual fun writeAttendanceCsvFile(path: String, bytes: ByteArray): Boolean = bytes.usePinned {
    NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()).writeToFile(path, atomically = true)
}
