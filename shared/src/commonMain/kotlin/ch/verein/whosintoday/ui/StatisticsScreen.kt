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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import whosintoday.shared.generated.resources.Res
import whosintoday.shared.generated.resources.absent
import whosintoday.shared.generated.resources.archived
import whosintoday.shared.generated.resources.archived_member_name
import whosintoday.shared.generated.resources.attendance_by_event
import whosintoday.shared.generated.resources.completed_events
import whosintoday.shared.generated.resources.event
import whosintoday.shared.generated.resources.group_count
import whosintoday.shared.generated.resources.member_attendance
import whosintoday.shared.generated.resources.member_completed_events
import whosintoday.shared.generated.resources.no_completed_events_included_this_member
import whosintoday.shared.generated.resources.no_completed_events_yet
import whosintoday.shared.generated.resources.no_matching_members
import whosintoday.shared.generated.resources.overall_attendance
import whosintoday.shared.generated.resources.people_present
import whosintoday.shared.generated.resources.people_present_count
import whosintoday.shared.generated.resources.percentage
import whosintoday.shared.generated.resources.percentage_present
import whosintoday.shared.generated.resources.present
import whosintoday.shared.generated.resources.search_by_name
import whosintoday.shared.generated.resources.search_for_a_member_to_see_their_attendance
import whosintoday.shared.generated.resources.statistics
import whosintoday.shared.generated.resources.statistics_calculation

@Composable
internal fun StatisticsScreen(
    state: StatisticsViewState,
    onPanelChange: (StatisticsPanel) -> Unit,
    onSearch: (String) -> Unit,
    onMemberSelect: (Long) -> Unit,
) {
    Heading(stringResource(Res.string.statistics))
    Label(stringResource(Res.string.completed_events), Modifier.padding(top = 4.dp), size = 13, color = muted)
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
                Label(stringResource(panel.label), weight = if (selected) 750 else 400, color = accent)
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
        Label(stringResource(Res.string.overall_attendance), Modifier.weight(1f), size = 13, color = accent)
        Label(stringResource(Res.string.percentage, percentage), size = 28, weight = 750, color = accent)
    }
}

@Composable
private fun MemberStatistics(state: StatisticsViewState, onSearch: (String) -> Unit, onMemberSelect: (Long) -> Unit) {
    val focus = LocalFocusManager.current
    StatisticsHeading(stringResource(Res.string.member_attendance))
    Field(stringResource(Res.string.search_by_name), state.query, onSearch)
    if (state.query.isNotBlank() && state.matches.isEmpty()) {
        Label(stringResource(Res.string.no_matching_members), size = 13, color = muted)
    }
    Rows(state.matches) { member ->
        ListRow(
            Modifier.clickable(role = Role.Button) {
                focus.clearFocus()
                onMemberSelect(member.id)
            },
        ) {
            Label(member.name, Modifier.weight(1f), weight = 620)
            if (!member.active) Label(stringResource(Res.string.archived), size = 12, color = muted)
        }
    }

    val member = state.member
    val history = state.history
    if (member == null || history == null) {
        if (state.query.isBlank()) Label(stringResource(Res.string.search_for_a_member_to_see_their_attendance), size = 13, color = muted)
        return
    }
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier.padding(top = 12.dp).fillMaxWidth().background(surface, shape)
            .border(1.dp, line, shape).padding(12.dp),
    ) {
        Label(if (member.active) member.name else stringResource(Res.string.archived_member_name, member.name), weight = 750)
        val rate = history.percentage
        if (rate == null) {
            Label(stringResource(Res.string.no_completed_events_included_this_member), size = 13, color = muted)
        } else {
            Label(stringResource(Res.string.percentage_present, stringResource(Res.string.percentage, rate)), size = 23, weight = 750, color = accent)
            val total = history.absent.size + history.present.size
            Label(pluralStringResource(Res.plurals.member_completed_events, total, total), size = 13, color = muted)
            StatisticsHistoryGroup(stringResource(Res.string.absent), history.absent)
            StatisticsHistoryGroup(stringResource(Res.string.present), history.present)
        }
    }
}

@Composable
private fun StatisticsHistoryGroup(status: String, events: List<Event>) {
    StatisticsHeading(stringResource(Res.string.group_count, status, events.size))
    Rows(events) { event ->
        ListRow(Modifier) {
            Label(event.dateLabel.fullLabel(includeYear = true), size = 13)
        }
    }
}

@Composable
private fun EventChart(events: List<EventStatistic>, axisMax: Int) {
    StatisticsHeading(stringResource(Res.string.attendance_by_event))
    if (events.isEmpty()) {
        Label(stringResource(Res.string.no_completed_events_yet), size = 13, color = muted)
        return
    }
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier.padding(top = 8.dp).fillMaxWidth().background(surface, shape)
            .border(1.dp, line, shape).padding(10.dp),
    ) {
        Label(stringResource(Res.string.event), size = 11, color = muted)
        events.forEach { event ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 34.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Label(event.date, Modifier.width(80.dp), size = 11, color = muted)
                val presentDescription = pluralStringResource(Res.plurals.people_present_count, event.present, event.present)
                Box(
                    Modifier.weight(1f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(soft)
                        .semantics { contentDescription = presentDescription },
                ) {
                    val fraction = event.present.toFloat() / axisMax.coerceAtLeast(1)
                    Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(accent))
                }
                Label(
                    event.percentage?.let { stringResource(Res.string.percentage, it) } ?: "—",
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
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                Label("0", Modifier.align(Alignment.CenterStart), size = 11, color = muted)
                val middleTick = axisMax / 2
                if (middleTick > 0) {
                    val middleOffset = maxWidth * (middleTick.toFloat() / axisMax - .5f)
                    Label(middleTick.toString(), Modifier.align(Alignment.Center).offset(x = middleOffset), size = 11, color = muted)
                }
                if (axisMax > 0) {
                    Label(axisMax.toString(), Modifier.align(Alignment.CenterEnd), size = 11, color = muted)
                }
            }
            Label(stringResource(Res.string.people_present), Modifier.fillMaxWidth(), size = 11, color = muted, align = TextAlign.Center)
        }
    }
    Label(
        stringResource(Res.string.statistics_calculation),
        Modifier.padding(top = 8.dp),
        size = 13,
        color = muted,
    )
}

@Composable
private fun StatisticsHeading(title: String) {
    Label(title, Modifier.padding(top = 16.dp, bottom = 4.dp).semantics { heading() }, weight = 750)
}
