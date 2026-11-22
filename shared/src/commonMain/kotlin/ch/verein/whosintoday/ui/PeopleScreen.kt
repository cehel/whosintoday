package ch.verein.whosintoday.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ch.verein.whosintoday.domain.Member

@Composable
internal fun PeopleScreen(
    member: Member?,
    sortedMembers: List<Member>,
    name: String,
    message: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onToggleActive: () -> Unit,
    onSelect: (Member) -> Unit,
) {
    Heading("People")
    Field(if (member == null) "Add a member" else "Edit name", name, onNameChange)
    Primary(if (member == null) "Add member" else "Save name", onSave)
    Status(message)
    if (member != null) {
        TextAction(if (member.active) "Archive member" else "Restore member", onToggleActive)
    }
    Rows(sortedMembers, Modifier.padding(top = 12.dp)) { person ->
        ListRow(Modifier.clickable(role = Role.Button) { onSelect(person) }) {
            Label(person.name, weight = 620, modifier = Modifier.weight(1f))
            Label(if (person.active) "Edit" else "Archived · Edit", size = 12, color = muted)
        }
    }
}
