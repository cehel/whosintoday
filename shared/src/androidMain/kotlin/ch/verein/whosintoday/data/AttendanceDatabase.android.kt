package ch.verein.whosintoday.data

import android.content.Context

fun initializeAttendanceDatabase(context: Context) {
    val file = context.getDatabasePath(databaseName)
    val directory = checkNotNull(file.parentFile)
    check(directory.mkdirs() || directory.isDirectory) { "Cannot create the database directory" }
    openAttendanceDatabase(file.absolutePath).use { }
}
