package ch.verein.whosintoday.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ch.verein.whosintoday.domain.Event
import org.jetbrains.compose.resources.stringResource
import whosintoday.shared.generated.resources.Res
import whosintoday.shared.generated.resources.events
import whosintoday.shared.generated.resources.in_progress
import whosintoday.shared.generated.resources.past_events

@Composable
internal fun EventsScreen(events: List<Event>, onOpen: (String) -> Unit) {
    Heading(stringResource(Res.string.events))
    val (past, ongoing) = events.sortedByDescending { it.date }.partition { it.closed }
    if (ongoing.isNotEmpty()) EventRows(stringResource(Res.string.in_progress), ongoing, onOpen)
    EventRows(stringResource(Res.string.past_events), past, onOpen)
}

@Composable
private fun EventRows(title: String, events: List<Event>, open: (String) -> Unit) {
    Label(title, Modifier.padding(top = 20.dp, bottom = 12.dp), size = 14, weight = 720)
    Rows(events) { event ->
        ListRow(Modifier.clickable(role = Role.Button) { open(event.date) }) {
            Column(Modifier.weight(1f)) {
                Label(event.dateLabel.fullLabel(), weight = 700)
                Label(event.countLabel(), size = 12, color = muted)
            }
            if (event.closed) Label("›", size = 22, color = accent) else Badge(stringResource(Res.string.in_progress))
        }
    }
}
