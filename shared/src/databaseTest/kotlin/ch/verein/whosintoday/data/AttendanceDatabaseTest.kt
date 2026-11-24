package ch.verein.whosintoday.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteException
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AttendanceDatabaseTest {
    @Test
    fun freshDatabaseEnforcesAttendanceReferencesAndUniqueness() {
        openAttendanceDatabase(":memory:").use { database ->
            assertEquals(SCHEMA_VERSION, database.intValue("PRAGMA user_version"))
            database.execSQL("INSERT INTO members (id, name) VALUES (1, 'Amélie')")
            assertEquals(1, database.intValue("SELECT active FROM members WHERE id = 1"))
            assertFailsWith<SQLiteException> {
                database.execSQL("UPDATE members SET active = 2 WHERE id = 1")
            }
            database.execSQL("INSERT INTO events (event_date) VALUES ('2026-11-18')")
            assertEquals(1, database.intValue("SELECT count(*) FROM events WHERE notes = ''"))
            database.execSQL("INSERT INTO attendance (event_date, member_id) VALUES ('2026-11-18', 1)")
            assertEquals(0, database.intValue("SELECT present FROM attendance"))
            assertFailsWith<SQLiteException> {
                database.execSQL("INSERT INTO events (event_date) VALUES ('2026-11-18')")
            }
            assertFailsWith<SQLiteException> {
                database.execSQL("INSERT INTO attendance (event_date, member_id) VALUES ('2026-11-19', 1)")
            }
            assertFailsWith<SQLiteException> {
                database.execSQL("INSERT INTO attendance (event_date, member_id) VALUES ('2026-11-18', 2)")
            }
            assertFailsWith<SQLiteException> {
                database.execSQL("INSERT INTO attendance (event_date, member_id) VALUES ('2026-11-18', 1)")
            }
        }
    }

    @Test
    fun acceptsOnlyCanonicalCalendarDates() {
        openAttendanceDatabase(":memory:").use { database ->
            database.execSQL("INSERT INTO events (event_date) VALUES ('2024-02-29'), ('2026-03-29')")
            val invalidDates = listOf(
                "0000-01-01",
                "1900-02-29",
                "2026-04-31",
                "2026-13-01",
                "2026-03-00",
                "2026-3-29",
                "2026-03-29T00:00:00+01:00",
            )
            for (date in invalidDates) {
                assertFailsWith<SQLiteException>(date) {
                    database.prepare("INSERT INTO events (event_date) VALUES (?)").use {
                        it.bindText(1, date)
                        it.step()
                    }
                }
            }
        }
    }

    @Test
    fun upgradingFromVersionOnePreservesExistingAttendance() = withTemporaryDatabase { path ->
        BundledSQLiteDriver().open(path).use { database ->
            database.execSQL("CREATE TABLE members (id INTEGER PRIMARY KEY, name TEXT NOT NULL, active INTEGER NOT NULL DEFAULT 1)")
            database.execSQL("CREATE TABLE events (event_date TEXT PRIMARY KEY NOT NULL, closed INTEGER NOT NULL DEFAULT 0)")
            database.execSQL(
                """
                CREATE TABLE attendance (
                    event_date TEXT NOT NULL REFERENCES events(event_date) ON DELETE CASCADE,
                    member_id INTEGER NOT NULL REFERENCES members(id),
                    present INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (event_date, member_id)
                )
                """.trimIndent(),
            )
            database.execSQL("CREATE INDEX attendance_member_id ON attendance(member_id)")
            database.execSQL("INSERT INTO members (id, name) VALUES (7, 'Noé')")
            database.execSQL("INSERT INTO events (event_date, closed) VALUES ('2026-11-18', 1), ('2026-11-20', 0)")
            database.execSQL("INSERT INTO attendance VALUES ('2026-11-18', 7, 1)")
            database.execSQL("INSERT INTO attendance VALUES ('2026-11-20', 7, 0)")
            database.execSQL("UPDATE members SET active = 0 WHERE id = 7")
            database.execSQL("PRAGMA user_version = 1")
        }
        openAttendanceDatabase(path).use { database ->
            assertEquals(SCHEMA_VERSION, database.intValue("PRAGMA user_version"))
            assertEquals(1, database.intValue("SELECT count(*) FROM members WHERE id = 7 AND name = 'Noé' AND active = 0"))
            assertEquals(2, database.intValue("SELECT count(*) FROM events WHERE notes = ''"))
            assertEquals(2, database.intValue("SELECT count(*) FROM attendance WHERE member_id = 7"))
            assertEquals(1, database.intValue("SELECT present FROM attendance WHERE event_date = '2026-11-18' AND member_id = 7"))
            assertEquals(0, database.intValue("SELECT present FROM attendance WHERE event_date = '2026-11-20' AND member_id = 7"))
            assertEquals(1, database.intValue("SELECT closed FROM events WHERE event_date = '2026-11-18'"))
            assertEquals(0, database.intValue("SELECT closed FROM events WHERE event_date = '2026-11-20'"))
            database.execSQL("UPDATE events SET notes = 'Bring the spare key' WHERE event_date = '2026-11-18'")
        }
        openAttendanceDatabase(path).use { database ->
            assertEquals(1, database.intValue("SELECT count(*) FROM events WHERE notes = 'Bring the spare key'"))
            database.execSQL("DELETE FROM events WHERE event_date = '2026-11-18'")
            assertEquals(0, database.intValue("SELECT count(*) FROM attendance WHERE event_date = '2026-11-18'"))
            assertEquals(1, database.intValue("SELECT count(*) FROM attendance WHERE event_date = '2026-11-20'"))
        }
    }

    @Test
    fun failedInitializationRollsBackSchemaAndVersion() = withTemporaryDatabase { path ->
        BundledSQLiteDriver().open(path).use { database ->
            // Conflict after tables have been created.
            database.execSQL("CREATE TABLE attendance_member_id (value INTEGER)")
        }
        assertFailsWith<SQLiteException> { openAttendanceDatabase(path) }
        BundledSQLiteDriver().open(path).use { database ->
            assertEquals(0, database.intValue("PRAGMA user_version"))
            assertEquals(0, database.intValue("SELECT count(*) FROM sqlite_master WHERE name = 'members'"))
            assertEquals(1, database.intValue("SELECT count(*) FROM sqlite_master WHERE name = 'attendance_member_id'"))
        }
    }

    @Test
    fun rejectsNewerSchemaWithoutDowngrading() = withTemporaryDatabase { path ->
        BundledSQLiteDriver().open(path).use {
            it.execSQL("PRAGMA user_version = 999")
        }
        assertFailsWith<IllegalStateException> { openAttendanceDatabase(path) }
        BundledSQLiteDriver().open(path).use {
            assertEquals(999, it.intValue("PRAGMA user_version"))
        }
    }
}

private fun SQLiteConnection.intValue(sql: String): Int = prepare(sql).use {
    check(it.step())
    it.getInt(0)
}

internal expect fun withTemporaryDatabase(test: (String) -> Unit)
