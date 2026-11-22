package ch.verein.whosintoday

import ch.verein.whosintoday.domain.Event
import ch.verein.whosintoday.domain.Member
import ch.verein.whosintoday.ui.attendanceCsv
import kotlin.test.Test
import kotlin.test.assertContentEquals

class AttendanceCsvTest {
    @Test
    fun exportsCompletedEventsInDateOrderWithRosterMembershipAndUtf8() {
        val members = listOf(
            Member(1, "Amélie, \"A\"", active = false),
            Member(2, "Noé"),
            Member(3, "Archived", active = false),
        )
        val events = listOf(
            Event("2026-11-20", mapOf(2L to true), closed = true),
            Event("2026-11-21", mapOf(3L to true)),
            Event("2026-11-19", mapOf(1L to true, 2L to false), closed = true),
        )
        val expected = "Name,2026-11-19,2026-11-20\r\n\"Amélie, \"\"A\"\"\",1,\r\nNoé,0,1"
        assertContentEquals(expected.encodeToByteArray(), attendanceCsv(members, events).encodeToByteArray())
    }
}
