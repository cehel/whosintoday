package ch.verein.whosintoday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ch.verein.whosintoday.domain.Event
import ch.verein.whosintoday.domain.Member
import org.jetbrains.compose.resources.stringResource
import whosintoday.shared.generated.resources.Res
import whosintoday.shared.generated.resources.absent
import whosintoday.shared.generated.resources.add_notes
import whosintoday.shared.generated.resources.cancel
import whosintoday.shared.generated.resources.check_in_attendees
import whosintoday.shared.generated.resources.completed_event
import whosintoday.shared.generated.resources.could_not_save_notes
import whosintoday.shared.generated.resources.delete
import whosintoday.shared.generated.resources.delete_event
import whosintoday.shared.generated.resources.delete_event_confirmation
import whosintoday.shared.generated.resources.delete_event_title
import whosintoday.shared.generated.resources.edit_notes
import whosintoday.shared.generated.resources.editing_attendance
import whosintoday.shared.generated.resources.event_notes
import whosintoday.shared.generated.resources.group_count
import whosintoday.shared.generated.resources.no_notes_yet
import whosintoday.shared.generated.resources.present
import whosintoday.shared.generated.resources.save_notes

@Composable
internal fun EventAttendanceScreen(
    current: Event,
    editing: Boolean,
    canMark: Boolean,
    sortedMembers: List<Member>,
    onAttendanceChange: (Long, Boolean) -> Unit,
    notesCard: @Composable () -> Unit,
) {
    Heading(current.dateLabel.shortLabel())
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Label(
            when {
                editing -> stringResource(Res.string.editing_attendance)
                current.closed -> stringResource(Res.string.completed_event)
                else -> stringResource(Res.string.check_in_attendees)
            },
            color = muted,
            modifier = Modifier.weight(1f).padding(end = 10.dp),
        )
        Badge(current.countLabel())
    }
    Box(
        Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 14.dp)
            .height(5.dp).clip(CircleShape).background(line),
    ) {
        val fraction = if (current.total == 0) 0f else current.present.toFloat() / current.total
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(accent, CircleShape))
    }
    notesCard()
    val roster = sortedMembers.filter { it.id in current.attendance }
    if (canMark) {
        Rows(roster) { member ->
            val checked = current.attendance[member.id] == true
            ListRow(
                Modifier.background(if (checked) soft else surface)
                    .toggleable(checked, role = Role.Checkbox) { isPresent ->
                        onAttendanceChange(member.id, isPresent)
                    },
            ) {
                AttendanceMemberName(member, Modifier.weight(1f), if (checked) surface else soft)
                CheckMark(checked)
            }
        }
    } else {
        val (present, absent) = roster.partition { current.attendance[it.id] == true }
        AttendanceGroup(stringResource(Res.string.absent), absent)
        AttendanceGroup(stringResource(Res.string.present), present)
    }
}

@Composable
internal fun EventNotesCard(
    notes: String,
    draft: String?,
    saveFailed: Boolean,
    onEdit: () -> Unit,
    onChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier.fillMaxWidth().padding(bottom = 14.dp)
            .background(surface, shape).border(1.dp, line, shape).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val title = stringResource(Res.string.event_notes)
        if (draft == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Label(title, Modifier.weight(1f).semantics { heading() }, weight = 720)
                TextButton(onClick = onEdit) {
                    Text(stringResource(if (notes.isEmpty()) Res.string.add_notes else Res.string.edit_notes), color = accent)
                }
            }
            if (notes.isEmpty()) {
                Label(stringResource(Res.string.no_notes_yet), color = muted)
            } else {
                Label(notes)
            }
        } else {
            OutlinedTextField(
                value = draft,
                onValueChange = onChange,
                label = { Text(title) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                isError = saveFailed,
                supportingText = if (saveFailed) {
                    { Text(stringResource(Res.string.could_not_save_notes)) }
                } else {
                    null
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = accent)) {
                    Text(stringResource(Res.string.save_notes))
                }
                TextButton(onClick = onCancel) {
                    Text(stringResource(Res.string.cancel), color = accent)
                }
            }
        }
    }
}

@Composable
internal fun DeleteEventAction(
    event: Event,
    confirming: Boolean,
    onRequest: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    TextButton(onClick = onRequest, modifier = Modifier.padding(top = 12.dp)) {
        Text(stringResource(Res.string.delete_event), color = MaterialTheme.colorScheme.error)
    }
    if (confirming) {
        AlertDialog(
            onDismissRequest = onCancel,
            title = { Text(stringResource(Res.string.delete_event_title)) },
            text = { Text(stringResource(Res.string.delete_event_confirmation, event.dateLabel.fullLabel())) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(Res.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = onCancel) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

@Composable
private fun AttendanceGroup(title: String, members: List<Member>) {
    Label(
        stringResource(Res.string.group_count, title, members.size),
        Modifier.padding(top = 12.dp, bottom = 5.dp).semantics { heading() },
        size = 14,
        weight = 720,
    )
    Rows(members) { member ->
        ListRow(Modifier) {
            AttendanceMemberName(member, Modifier.weight(1f), soft)
        }
    }
}

@Composable
private fun AttendanceMemberName(member: Member, modifier: Modifier, initialsBackground: Color) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(30.dp).background(initialsBackground, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val initials = member.name.split(Regex("\\s+")).take(2)
                .joinToString("") { it.take(1) }
            Label(initials, size = 10, weight = 800, color = accent)
        }
        Label(member.name, weight = 620)
    }
}
