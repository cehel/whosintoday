package ch.verein.whosintoday

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ch.verein.whosintoday.ui.AttendancePrototype
import ch.verein.whosintoday.ui.accent
import ch.verein.whosintoday.ui.canvas
import ch.verein.whosintoday.ui.ink
import ch.verein.whosintoday.ui.line
import ch.verein.whosintoday.ui.muted
import ch.verein.whosintoday.ui.surface

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = accent, background = canvas, surface = surface,
        onSurface = ink, onBackground = ink, onSurfaceVariant = muted, outline = line,
    )) {
        AttendancePrototype()
    }
}
