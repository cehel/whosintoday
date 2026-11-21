package ch.verein.whosintoday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AppTopBar(newEvent: Boolean, onBackToEvents: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(surface)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .heightIn(min = 58.dp).padding(horizontal = gutter),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Label("Who’s In Today", size = 13, weight = 750)
        if (newEvent) {
            TextAction("← Events") { onBackToEvents() }
        }
    }
}
