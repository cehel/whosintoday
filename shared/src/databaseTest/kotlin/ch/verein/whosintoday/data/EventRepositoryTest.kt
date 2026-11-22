package ch.verein.whosintoday.data

import androidx.sqlite.SQLiteException
import androidx.sqlite.execSQL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EventRepositoryTest {
    @Test
    fun reopeningKeepsTheOriginalRosterAndRestoresTicksAndUnticks() = withTemporaryDatabase { path ->
        val date = "2024-02-29"
        val originalRoster = openAttendanceDatabase(path).use { database ->
            val members = MemberRepository(database)
            val events = EventRepository(database)
            val first = members.add("Amélie")
            val second = members.add("Noé")
            val archived = members.add("Lea")
            members.setActive(archived.id, false)

            val event = events.start(date)
            assertEquals(mapOf(first.id to false, second.id to false), event.attendance)
            events.setPresent(event, first.id, true)
            events.setPresent(event, second.id, true)
            events.setPresent(event, second.id, false)

            members.setActive(first.id, false)
            members.setActive(second.id, false)
            members.setActive(archived.id, true)
            val added = members.add("Mia")
            assertEquals(mapOf(archived.id to false, added.id to false), events.start("2024-03-01").attendance)
            members.setActive(archived.id, false)
            members.setActive(added.id, false)
            mapOf(first.id to true, second.id to false)
        }

        openAttendanceDatabase(path).use { database ->
            val events = EventRepository(database)
            val reopened = events.start(date)
            assertEquals(originalRoster, reopened.attendance)
            assertEquals(reopened, events.list().single { it.date == date })
            assertEquals(false, reopened.closed)
            val completed = events.finish(date)
            assertTrue(completed.closed)
            assertEquals(completed, events.start(date))

            val edited = events.setPresent(completed, originalRoster.keys.last(), true)
            assertTrue(edited.closed)
            assertEquals(originalRoster.keys.associateWith { true }, edited.attendance)
        }

        openAttendanceDatabase(path).use { database ->
            val saved = EventRepository(database).list().single { it.date == date }
            assertTrue(saved.closed)
            assertEquals(originalRoster.keys.associateWith { true }, saved.attendance)
        }
    }

    @Test
    fun deletingAnEventClearsItsAttendanceAndAllowsRecreatingIt() = withTemporaryDatabase { path ->
        val date = "2026-11-18"
        val otherDate = "2026-11-19"
        val member = openAttendanceDatabase(path).use { database ->
            val member = MemberRepository(database).add("Amélie")
            val events = EventRepository(database)
            events.setPresent(events.start(date), member.id, true)
            events.finish(date)
            events.setPresent(events.start(otherDate), member.id, true)
            events.delete(date)
            member
        }

        openAttendanceDatabase(path).use { database ->
            val events = EventRepository(database)
            val remaining = events.list().single()
            assertEquals(otherDate, remaining.date)
            assertEquals(mapOf(member.id to true), remaining.attendance)

            val recreated = events.start(date)
            assertEquals(mapOf(member.id to false), recreated.attendance)
            assertEquals(false, recreated.closed)
        }
    }

    @Test
    fun failedSnapshotLeavesNoEventAndCanBeRetried() {
        openAttendanceDatabase(":memory:").use { database ->
            val events = EventRepository(database)
            val members = MemberRepository(database)
            val date = "2026-11-18"
            assertFailsWith<IllegalArgumentException> { events.start(date) }
            assertTrue(events.list().isEmpty())

            val first = members.add("Amélie")
            val second = members.add("Noé")
            database.execSQL(
                """
                CREATE TRIGGER fail_snapshot BEFORE INSERT ON attendance
                WHEN NEW.member_id = ${second.id}
                BEGIN SELECT RAISE(FAIL, 'Snapshot failed'); END
                """.trimIndent(),
            )
            assertFailsWith<SQLiteException> { events.start(date) }
            assertTrue(events.list().isEmpty())
            database.execSQL("DROP TRIGGER fail_snapshot")
            assertEquals(mapOf(first.id to false, second.id to false), events.start(date).attendance)
        }
    }
}
