package ch.verein.whosintoday.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun AppTopBar(newEvent: Boolean, eventAttendance: Boolean, onBackToEvents: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(surface)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .heightIn(min = 58.dp).padding(horizontal = gutter),
        horizontalArrangement = if (eventAttendance) Arrangement.spacedBy(14.dp) else Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (eventAttendance) {
            Box(
                Modifier.size(44.dp)
                    .clickable(role = Role.Button, onClick = onBackToEvents)
                    .semantics { contentDescription = "Back to Events" },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(20.dp)) {
                    val tip = Offset(size.width * .35f, size.height * .5f)
                    val right = size.width * .65f
                    val stroke = 2.dp.toPx()
                    drawLine(accent, Offset(right, size.height * .2f), tip, stroke, StrokeCap.Round)
                    drawLine(accent, tip, Offset(right, size.height * .8f), stroke, StrokeCap.Round)
                }
            }
        }
        Label("Who’s In Today", size = 13, weight = 750)
        if (newEvent && !eventAttendance) {
            TextAction("← Events", onClick = onBackToEvents)
        }
    }
}
