package ch.verein.whosintoday.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL

internal const val DATABASE_NAME = "attendance.db"
internal const val SCHEMA_VERSION = 1

internal fun openAttendanceDatabase(path: String): SQLiteConnection {
    val connection = BundledSQLiteDriver().open(path)
    try {
        connection.execSQL("PRAGMA busy_timeout = 5000")
        connection.execSQL("PRAGMA foreign_keys = ON")
        migrate(connection)
        return connection
    } catch (failure: Throwable) {
        runCatching { connection.close() }.onFailure(failure::addSuppressed)
        throw failure
    }
}

private fun migrate(connection: SQLiteConnection) {
    // Lock before reading user_version so concurrent openers see the committed schema.
    connection.execSQL("BEGIN IMMEDIATE")
    try {
        val version = connection.prepare("PRAGMA user_version").use {
            check(it.step())
            it.getInt(0)
        }
        check(version in 0..SCHEMA_VERSION) {
            "Unsupported attendance database version: $version"
        }
        if (version == 0) {
            createAttendanceTables(connection)
            connection.execSQL("PRAGMA user_version = $SCHEMA_VERSION")
        }
        connection.execSQL("COMMIT")
    } catch (failure: Throwable) {
        runCatching { connection.execSQL("ROLLBACK") }.onFailure(failure::addSuppressed)
        throw failure
    }
}

private fun createAttendanceTables(connection: SQLiteConnection) {
    connection.execSQL(
        """
        CREATE TABLE members (
            id INTEGER PRIMARY KEY,
            name TEXT NOT NULL CHECK (length(trim(name)) > 0),
            active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1))
        )
        """.trimIndent(),
    )
    // The +0 days modifier normalizes impossible dates, and IS also rejects a NULL result.
    connection.execSQL(
        """
        CREATE TABLE events (
            event_date TEXT PRIMARY KEY NOT NULL CHECK (
                event_date GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]'
                AND substr(event_date, 1, 4) <> '0000'
                AND date(event_date, '+0 days') IS event_date
            ),
            closed INTEGER NOT NULL DEFAULT 0 CHECK (closed IN (0, 1))
        )
        """.trimIndent(),
    )
    connection.execSQL(
        """
        CREATE TABLE attendance (
            event_date TEXT NOT NULL REFERENCES events(event_date) ON DELETE CASCADE,
            member_id INTEGER NOT NULL REFERENCES members(id),
            present INTEGER NOT NULL DEFAULT 0 CHECK (present IN (0, 1)),
            PRIMARY KEY (event_date, member_id)
        )
        """.trimIndent(),
    )
    connection.execSQL("CREATE INDEX attendance_member_id ON attendance(member_id)")
}
