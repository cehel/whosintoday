package ch.verein.whosintoday.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import whosintoday.shared.generated.resources.Res
import whosintoday.shared.generated.resources.each_date_has_one_attendance_record
import whosintoday.shared.generated.resources.event_date
import whosintoday.shared.generated.resources.new_event
import whosintoday.shared.generated.resources.start_check_in

@Composable
internal fun NewEventScreen(
    dateInput: String,
    message: StringResource?,
    onDateChange: (String) -> Unit,
    onStart: () -> Unit,
) {
    Heading(stringResource(Res.string.new_event))
    Field(stringResource(Res.string.event_date), dateInput, onDateChange)
    Label(
        stringResource(Res.string.each_date_has_one_attendance_record),
        size = 13,
        color = muted,
        modifier = Modifier.padding(bottom = 10.dp),
    )
    Primary(stringResource(Res.string.start_check_in), onStart)
    Status(message)
}
