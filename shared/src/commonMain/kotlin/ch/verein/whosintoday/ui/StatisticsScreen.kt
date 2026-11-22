package ch.verein.whosintoday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.verein.whosintoday.domain.Event

@Composable
internal fun StatisticsScreen(
    state: StatisticsViewState,
    onPanelChange: (StatisticsPanel) -> Unit,
    onSearch: (String) -> Unit,
    onMemberSelect: (Long) -> Unit,
) {
    Heading("Statistics")
    Label("Completed events", Modifier.padding(top = 4.dp), size = 13, color = muted)
    Row(
        Modifier.padding(top = 14.dp).fillMaxWidth().selectableGroup()
            .background(soft, RoundedCornerShape(10.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StatisticsPanel.entries.forEach { panel ->
            val selected = panel == state.panel
            Box(
                Modifier.weight(1f).heightIn(min = 44.dp).clip(RoundedCornerShape(7.dp))
                    .background(if (selected) surface else soft)
                    .selectable(selected, role = Role.Tab, onClick = { onPanelChange(panel) }),
                contentAlignment = Alignment.Center,
            ) {
                Label(panel.label, weight = if (selected) 750 else 400, color = accent)
            }
        }
    }
    when (state.panel) {
        StatisticsPanel.Events -> {
            state.overallPercentage?.let { OverallAttendance(it) }
            EventChart(state.events, state.axisMax)
        }

        StatisticsPanel.People -> MemberStatistics(state, onSearch, onMemberSelect)
    }
}

@Composable
private fun OverallAttendance(percentage: Int) {
    Row(
        Modifier.padding(top = 14.dp).fillMaxWidth().background(soft, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Label("Overall attendance", Modifier.weight(1f), size = 13, color = accent)
        Label("$percentage%", size = 28, weight = 750, color = accent)
    }
}

@Composable
private fun MemberStatistics(state: StatisticsViewState, onSearch: (String) -> Unit, onMemberSelect: (Long) -> Unit) {
    val focus = LocalFocusManager.current
    StatisticsHeading("Member attendance")
    Field("Search by name", state.query, onSearch)
    if (state.query.isNotBlank() && state.matches.isEmpty()) {
        Label("No matching members.", size = 13, color = muted)
    }
    Rows(state.matches) { member ->
        ListRow(
            Modifier.clickable(role = Role.Button) {
                focus.clearFocus()
                onMemberSelect(member.id)
            },
        ) {
            Label(member.name, Modifier.weight(1f), weight = 620)
            if (!member.active) Label("Archived", size = 12, color = muted)
        }
    }

    val member = state.member
    val history = state.history
    if (member == null || history == null) {
        if (state.query.isBlank()) Label("Search for a member to see their attendance.", size = 13, color = muted)
        return
    }
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier.padding(top = 12.dp).fillMaxWidth().background(surface, shape)
            .border(1.dp, line, shape).padding(12.dp),
    ) {
        Label(if (member.active) member.name else "${member.name} · Archived", weight = 750)
        val rate = history.percentage
        if (rate == null) {
            Label("No completed events included this member.", size = 13, color = muted)
        } else {
            Label("$rate% present", size = 23, weight = 750, color = accent)
            val total = history.absent.size + history.present.size
            val eventLabel = if (total == 1) "event" else "events"
            Label("Based on $total completed $eventLabel that included this member.", size = 13, color = muted)
            StatisticsHistoryGroup("Absent", history.absent)
            StatisticsHistoryGroup("Present", history.present)
        }
    }
}

@Composable
private fun StatisticsHistoryGroup(status: String, events: List<Event>) {
    StatisticsHeading("$status · ${events.size}")
    Rows(events) { event ->
        ListRow(Modifier) {
            Label("${event.dateLabel.fullLabel} ${event.dateLabel.year}", size = 13)
        }
    }
}

@Composable
private fun EventChart(events: List<EventStatistic>, axisMax: Int) {
    StatisticsHeading("Attendance by event")
    if (events.isEmpty()) {
        Label("No completed events yet.", size = 13, color = muted)
        return
    }
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier.padding(top = 8.dp).fillMaxWidth().background(surface, shape)
            .border(1.dp, line, shape).padding(10.dp),
    ) {
        Label("Event", size = 11, color = muted)
        events.forEach { event ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 34.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Label(event.date, Modifier.width(80.dp), size = 11, color = muted)
                Box(
                    Modifier.weight(1f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(soft)
                        .semantics { contentDescription = "${event.present} people present" },
                ) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(event.present.toFloat() / axisMax).background(accent))
                }
                Label(
                    event.percentage?.let { "$it%" } ?: "—",
                    Modifier.width(48.dp).clip(RoundedCornerShape(99.dp)).background(soft).padding(vertical = 3.dp),
                    size = 12,
                    weight = 750,
                    color = accent,
                    align = TextAlign.Center,
                )
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 86.dp, end = 54.dp)) {
            Rule()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(0, axisMax / 2, axisMax).forEach { tick ->
                    Label(tick.toString(), size = 11, color = muted)
                }
            }
            Label("People present", Modifier.fillMaxWidth(), size = 11, color = muted, align = TextAlign.Center)
        }
    }
    Label(
        "Each percentage uses its event’s roster. Overall attendance uses all roster spots across completed events.",
        Modifier.padding(top = 8.dp),
        size = 13,
        color = muted,
    )
}

@Composable
private fun StatisticsHeading(title: String) {
    Label(title, Modifier.padding(top = 16.dp, bottom = 4.dp).semantics { heading() }, weight = 750)
}
