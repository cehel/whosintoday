package ch.verein.whosintoday.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal fun initializeAttendanceDatabase() {
    val directory = checkNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            NSApplicationSupportDirectory,
            NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        ),
    ) { "Cannot create the application support directory" }
    val path = checkNotNull(directory.URLByAppendingPathComponent(databaseName)?.path)
    openAttendanceDatabase(path).use { }
}
