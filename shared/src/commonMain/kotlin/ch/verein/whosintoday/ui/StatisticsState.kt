package ch.verein.whosintoday.ui

import ch.verein.whosintoday.domain.Event
import ch.verein.whosintoday.domain.Member
import ch.verein.whosintoday.domain.MemberAttendance
import ch.verein.whosintoday.domain.attendancePercentage

internal enum class StatisticsPanel(val label: String) {
    Events("Events"),
    People("People"),
}

internal data class StatisticsSelection(
    val panel: StatisticsPanel = StatisticsPanel.Events,
    val query: String = "",
    val memberId: Long? = null,
) {
    fun view(events: List<Event>, sortedMembers: List<Member>): StatisticsViewState {
        val completed = events.filter(Event::closed).sortedByDescending(Event::date)
        val member = sortedMembers.find { it.id == memberId }
        val name = query.trim()
        val matches = if (name.isEmpty()) emptyList() else sortedMembers.filter { it.name.contains(name, ignoreCase = true) }
        val largestRoster = (completed.maxOfOrNull(Event::total) ?: 0).coerceAtLeast(2)
        return StatisticsViewState(
            panel = panel,
            query = query,
            matches = matches,
            member = member,
            history = member?.let { MemberAttendance(completed, it.id) },
            events = completed.map { EventStatistic(it.date, it.present, attendancePercentage(it.present, it.total)) },
            axisMax = largestRoster + largestRoster % 2,
        )
    }
}

internal data class StatisticsViewState(
    val panel: StatisticsPanel,
    val query: String,
    val matches: List<Member>,
    val member: Member?,
    val history: MemberAttendance?,
    val events: List<EventStatistic>,
    val axisMax: Int,
)

internal data class EventStatistic(val date: String, val present: Int, val percentage: Int?)
