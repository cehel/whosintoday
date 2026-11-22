package ch.verein.whosintoday.data

import android.content.Context

fun attendanceDatabasePath(context: Context): String {
    val file = context.getDatabasePath(DATABASE_NAME)
    val directory = checkNotNull(file.parentFile)
    check(directory.mkdirs() || directory.isDirectory) { "Cannot create the database directory" }
    return file.absolutePath
}
