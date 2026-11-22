package ch.verein.whosintoday.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

@OptIn(ExperimentalForeignApi::class)
internal actual fun withTemporaryDatabase(test: (String) -> Unit) {
    val path = NSTemporaryDirectory() + "attendance-" + NSUUID().UUIDString + ".db"
    try {
        test(path)
    } finally {
        NSFileManager.defaultManager.removeItemAtPath(path, error = null)
    }
}
