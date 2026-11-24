package ch.verein.whosintoday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AppBottomBar(selectedScreen: AppScreen, newEvent: Boolean, onNavigate: (AppScreen) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(surface)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .selectableGroup().padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        AppScreen.entries.forEach { screen ->
            val selected = screen == selectedScreen && !newEvent
            Box(
                Modifier.heightIn(min = 44.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (selected) soft else surface)
                    .selectable(selected, role = Role.Tab, onClick = { onNavigate(screen) })
                    .padding(horizontal = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Label(
                    stringResource(screen.label),
                    size = 13,
                    weight = if (selected) 750 else 400,
                    color = if (selected) accent else muted,
                )
            }
        }
    }
}
