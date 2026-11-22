package ch.verein.whosintoday.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ch.verein.whosintoday.domain.Member
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import whosintoday.shared.generated.resources.Res
import whosintoday.shared.generated.resources.add_a_member
import whosintoday.shared.generated.resources.add_member
import whosintoday.shared.generated.resources.archive_member
import whosintoday.shared.generated.resources.edit
import whosintoday.shared.generated.resources.edit_archived_member
import whosintoday.shared.generated.resources.edit_name
import whosintoday.shared.generated.resources.people
import whosintoday.shared.generated.resources.restore_member
import whosintoday.shared.generated.resources.save_name

@Composable
internal fun PeopleScreen(
    member: Member?,
    sortedMembers: List<Member>,
    name: String,
    message: StringResource?,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onToggleActive: () -> Unit,
    onSelect: (Member) -> Unit,
) {
    Heading(stringResource(Res.string.people))
    Field(if (member == null) stringResource(Res.string.add_a_member) else stringResource(Res.string.edit_name), name, onNameChange)
    Primary(if (member == null) stringResource(Res.string.add_member) else stringResource(Res.string.save_name), onSave)
    Status(message)
    if (member != null) {
        TextAction(if (member.active) stringResource(Res.string.archive_member) else stringResource(Res.string.restore_member), onToggleActive)
    }
    Rows(sortedMembers, Modifier.padding(top = 12.dp)) { person ->
        ListRow(Modifier.clickable(role = Role.Button) { onSelect(person) }) {
            Label(person.name, weight = 620, modifier = Modifier.weight(1f))
            Label(if (person.active) stringResource(Res.string.edit) else stringResource(Res.string.edit_archived_member), size = 12, color = muted)
        }
    }
}
