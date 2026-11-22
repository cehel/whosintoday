package ch.verein.whosintoday

import ch.verein.whosintoday.domain.Event
import ch.verein.whosintoday.domain.Member
import ch.verein.whosintoday.ui.EventStatistic
import ch.verein.whosintoday.ui.StatisticsSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class StatisticsTest {
    @Test
    fun historyExcludesOpenEventsAndEventsOutsideTheMemberRoster() {
        val member = Member(1, "Amélie Rey", active = false)
        val other = Member(2, "Jonas Berger")
        val present = Event("2026-10-21", mapOf(member.id to true, other.id to true), closed = true)
        val absent = Event("2026-11-04", mapOf(member.id to false), closed = true)
        val outsideRoster = Event("2026-11-11", mapOf(other.id to true), closed = true)
        val open = Event("2026-11-18", mapOf(member.id to true))
        val selection = StatisticsSelection(query = " AMÉ ", memberId = member.id)

        val state = selection.view(listOf(present, open, outsideRoster, absent), listOf(member, other))

        assertEquals(listOf(member), state.matches)
        val history = assertNotNull(state.history)
        assertEquals(listOf(present), history.present)
        assertEquals(listOf(absent), history.absent)
        assertEquals(50, history.percentage)
        assertEquals(
            listOf(
                EventStatistic(outsideRoster.date, 1, 100),
                EventStatistic(absent.date, 0, 0),
                EventStatistic(present.date, 2, 100),
            ),
            state.events,
        )
    }

    @Test
    fun chartUsesEachEventsRosterAndLeavesUndefinedRatesBlank() {
        val member = Member(1, "Amélie Rey")
        val empty = Event("2026-10-21", emptyMap(), closed = true)
        val attended = Event("2026-11-04", mapOf(2L to true, 3L to true, 4L to false), closed = true)

        val state = StatisticsSelection(memberId = member.id).view(listOf(empty, attended), listOf(member))

        assertEquals(4, state.axisMax)
        assertEquals(listOf(EventStatistic(attended.date, 2, 67), EventStatistic(empty.date, 0, null)), state.events)
        assertNull(assertNotNull(state.history).percentage)
    }
}
