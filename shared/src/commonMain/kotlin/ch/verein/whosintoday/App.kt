package ch.verein.whosintoday

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.verein.whosintoday.ui.AttendancePrototype
import ch.verein.whosintoday.ui.AttendanceViewModel
import ch.verein.whosintoday.ui.PeopleViewModel
import ch.verein.whosintoday.ui.accent
import ch.verein.whosintoday.ui.canvas
import ch.verein.whosintoday.ui.ink
import ch.verein.whosintoday.ui.line
import ch.verein.whosintoday.ui.muted
import ch.verein.whosintoday.ui.surface

@Composable
@Preview
fun App() = AttendanceApp(":memory:", sample = true, shareCsv = null)

@Composable
fun App(databasePath: String, shareCsv: (String, ByteArray) -> Boolean) = AttendanceApp(
    databasePath,
    sample = false,
    shareCsv = shareCsv,
)

@Composable
private fun AttendanceApp(databasePath: String, sample: Boolean, shareCsv: ((String, ByteArray) -> Boolean)?) {
    val people: PeopleViewModel = viewModel(key = "people:$databasePath") {
        PeopleViewModel.open(databasePath, sample)
    }
    // Both ViewModels live in the same store and share the connection owned by PeopleViewModel.
    val attendance: AttendanceViewModel = viewModel(key = "attendance:$databasePath") {
        AttendanceViewModel(people.events, sample)
    }
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = accent,
            background = canvas,
            surface = surface,
            onSurface = ink,
            onBackground = ink,
            onSurfaceVariant = muted,
            outline = line,
        ),
    ) {
        AttendancePrototype(people, attendance, shareCsv)
    }
}
