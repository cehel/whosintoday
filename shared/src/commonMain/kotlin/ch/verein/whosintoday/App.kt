package ch.verein.whosintoday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private enum class AppScreen(val label: String) {
    CheckIn("Check-in"),
    Events("Events"),
    People("People"),
    Export("Export"),
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var selectedScreenName by rememberSaveable { mutableStateOf(AppScreen.CheckIn.name) }
        val selectedScreen = AppScreen.values().firstOrNull { it.name == selectedScreenName }
            ?: AppScreen.CheckIn

        Scaffold(
            modifier = Modifier.safeContentPadding(),
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 1.dp,
                ) {
                    Text(
                        text = "Who's In Today",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        AppScreen.values().forEach { screen ->
                            val isSelected = screen == selectedScreen
                            TextButton(
                                onClick = { selectedScreenName = screen.name },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { selected = isSelected },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                    contentColor = if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                ),
                            ) {
                                Text(screen.label)
                            }
                        }
                    }
                }
            },
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            ) {
                when (selectedScreen) {
                    AppScreen.CheckIn -> CheckInScreen()
                    AppScreen.Events -> EventsScreen()
                    AppScreen.People -> PeopleScreen()
                    AppScreen.Export -> ExportScreen()
                }
            }
        }
    }
}

@Composable
private fun CheckInScreen() {
    EmptyScreen(
        title = "No check-in started",
        message = "Start today's event to begin checking people in.",
    )
}

@Composable
private fun EventsScreen() {
    EmptyScreen(
        title = "No events yet",
        message = "Events you start will appear here.",
    )
}

@Composable
private fun PeopleScreen() {
    EmptyScreen(
        title = "No people yet",
        message = "Add members to set up your attendance roster.",
    )
}

@Composable
private fun ExportScreen() {
    EmptyScreen(
        title = "No completed events",
        message = "Finish an event to see attendance ready to export.",
    )
}

@Composable
private fun EmptyScreen(
    title: String,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
