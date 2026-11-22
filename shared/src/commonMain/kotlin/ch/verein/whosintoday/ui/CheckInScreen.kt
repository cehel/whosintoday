package ch.verein.whosintoday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ch.verein.whosintoday.domain.Event
import ch.verein.whosintoday.domain.Member

@Composable
internal fun CheckInScreen(
    current: Event,
    editing: Boolean,
    canMark: Boolean,
    sortedMembers: List<Member>,
    onAttendanceChange: (Long, Boolean) -> Unit,
) {
    Heading(current.dateLabel.shortLabel)
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Label(
            when {
                editing -> "Editing attendance"
                current.closed -> "Completed event"
                else -> "Check in attendees"
            },
            color = muted,
            modifier = Modifier.weight(1f).padding(end = 10.dp),
        )
        Badge(current.countLabel)
    }
    Box(
        Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 14.dp)
            .height(5.dp).clip(CircleShape).background(line),
    ) {
        val fraction = if (current.total == 0) 0f else current.present.toFloat() / current.total
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(accent, CircleShape))
    }
    val roster = sortedMembers.filter { it.id in current.attendance }
    Rows(roster) { member ->
        val checked = current.attendance[member.id] == true
        val interaction = if (canMark) {
            Modifier.background(if (checked) soft else surface)
                .toggleable(checked, role = Role.Checkbox) { isPresent ->
                    onAttendanceChange(member.id, isPresent)
                }
        } else {
            Modifier
        }
        ListRow(interaction) {
            Row(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(30.dp).background(
                        if (canMark && checked) surface else soft,
                        CircleShape,
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    val initials = member.name.split(Regex("\\s+")).take(2)
                        .joinToString("") { it.take(1) }
                    Label(initials, size = 10, weight = 800, color = accent)
                }
                Label(member.name, weight = 620)
            }
            if (canMark) {
                CheckMark(checked)
            } else {
                Label(if (checked) "Present" else "Absent", size = 12, color = muted)
            }
        }
    }
}
