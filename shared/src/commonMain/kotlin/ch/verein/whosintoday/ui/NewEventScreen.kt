package ch.verein.whosintoday.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun NewEventScreen(
    dateInput: String,
    message: String,
    onDateChange: (String) -> Unit,
    onStart: () -> Unit,
) {
    Heading("New event")
    Field("Event date", dateInput, onDateChange)
    Label(
        "Each date has one attendance record.",
        size = 13,
        color = muted,
        modifier = Modifier.padding(bottom = 10.dp),
    )
    Primary("Start check-in", onStart)
    Status(message)
}
