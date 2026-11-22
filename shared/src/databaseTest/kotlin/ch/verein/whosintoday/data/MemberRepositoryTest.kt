package ch.verein.whosintoday.data

import androidx.sqlite.execSQL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemberRepositoryTest {
    @Test
    fun rosterChangesSurviveReopeningWithoutChangingAttendance() = withTemporaryDatabase { path ->
        val id = openAttendanceDatabase(path).use { database ->
            val members = MemberRepository(database)
            val member = members.add("  Amélie Rey  ")
            assertEquals("Amélie Rey", member.name)
            assertTrue(member.active)
            database.execSQL("INSERT INTO events (event_date) VALUES ('2026-11-18')")
            database.prepare(
                "INSERT INTO attendance (event_date, member_id, present) VALUES ('2026-11-18', ?, 1)",
            ).use {
                it.bindLong(1, member.id)
                it.step()
            }

            assertEquals(member.id, members.rename(member.id, "  Amélie O’Rey ").id)
            assertFalse(members.setActive(member.id, false).active)
            member.id
        }

        openAttendanceDatabase(path).use { database ->
            val members = MemberRepository(database)
            val archived = members.list().single()
            assertEquals(id, archived.id)
            assertEquals("Amélie O’Rey", archived.name)
            assertFalse(archived.active)
            assertTrue(members.setActive(id, true).active)
        }

        openAttendanceDatabase(path).use { database ->
            assertTrue(MemberRepository(database).list().single().active)
            database.prepare("SELECT event_date, member_id, present FROM attendance").use {
                assertTrue(it.step())
                assertEquals("2026-11-18", it.getText(0))
                assertEquals(id, it.getLong(1))
                assertEquals(1L, it.getLong(2))
                assertFalse(it.step())
            }
        }
    }

    @Test
    fun invalidNamesLeaveTheRosterUnchanged() {
        openAttendanceDatabase(":memory:").use { database ->
            val members = MemberRepository(database)
            val existing = members.add("Noé")
            for (invalid in listOf(" \t\u00a0 ", "a".repeat(101), "Noé\u0000Favre")) {
                assertFailsWith<IllegalArgumentException> { members.add(invalid) }
                assertFailsWith<IllegalArgumentException> { members.rename(existing.id, invalid) }
            }
            assertEquals(listOf(existing), members.list())
        }
    }
}
