package ch.verein.whosintoday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Suppress("DEPRECATION") // The text clipboard API is available on both Compose targets.
@Composable
internal fun AttendancePrototype(people: PeopleViewModel, sample: Boolean) {
    var tabName by rememberSaveable { mutableStateOf(AppScreen.CheckIn.name) }
    val tab = AppScreen.valueOf(tabName)
    val peopleState by people.viewState.collectAsStateWithLifecycle()
    val members = peopleState.members
    val events = remember(people) {
        if (!sample) return@remember mutableStateListOf<Event>()
        fun sample(date: String, vararg present: Int, closed: Boolean = true) = Event(date, members.mapIndexed { index, member -> member.id to (index in present) }.toMap(), closed)
        mutableStateListOf(
            sample(SAMPLE_DATE, 0, 2, closed = false),
            sample("2026-11-04", 0, 1, 3, 5),
            sample("2026-10-21", 0, 1, 2, 4, 5),
        )
    }
    var eventDate by remember { mutableStateOf(events.find { it.date == SAMPLE_DATE }?.date) }
    var editing by remember { mutableStateOf(false) }
    var newEvent by remember { mutableStateOf(false) }
    var dateInput by remember { mutableStateOf(SAMPLE_DATE) }
    var message by remember { mutableStateOf("") }
    val sortedMembers = members.sortedBy { it.name.lowercase() }
    val current = events.find { it.date == eventDate }
    val canMark = current != null && (!current.closed || editing)
    val clipboard = LocalClipboardManager.current
    val csv = if (tab == AppScreen.Export) attendanceCsv(sortedMembers, events) else ""

    fun open(date: String) {
        eventDate = date
        tabName = if (date == SAMPLE_DATE) AppScreen.CheckIn.name else AppScreen.Events.name
        editing = false
        newEvent = false
        message = ""
    }
    fun navigate(screen: AppScreen) {
        tabName = screen.name
        eventDate = SAMPLE_DATE.takeIf { screen == AppScreen.CheckIn }
        editing = false
        newEvent = false
        people.resetEditor()
        message = ""
    }
    Column(
        Modifier.fillMaxSize().background(canvas)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        AppTopBar(newEvent) { navigate(AppScreen.Events) }
        Rule()

        key(tab, current?.date, newEvent) {
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(gutter),
            ) {
                when {
                    current != null -> CheckInScreen(current, editing, canMark, sortedMembers) { id, isPresent ->
                        events[events.indexOf(current)] = current.copy(
                            attendance = current.attendance + (id to isPresent),
                        )
                    }

                    newEvent -> NewEventScreen(dateInput, message, { dateInput = it.take(10) }) {
                        when {
                            parseDate(dateInput) == null -> message = "Enter a date as YYYY-MM-DD."

                            events.any { it.date == dateInput } -> open(dateInput)

                            members.none { it.active } -> message = "Add an active member before starting an event."

                            else -> {
                                events += Event(dateInput, members.filter { it.active }.associate { it.id to false })
                                open(dateInput)
                            }
                        }
                    }

                    tab == AppScreen.Events -> EventsScreen(events, ::open)

                    tab == AppScreen.CheckIn -> {
                        Heading("Check-in")
                        Status("No event is open.")
                        TextAction("Go to Events") { navigate(AppScreen.Events) }
                    }

                    tab == AppScreen.People -> PeopleScreen(
                        member = peopleState.selectedMember,
                        sortedMembers = sortedMembers,
                        name = peopleState.name,
                        message = peopleState.message,
                        onNameChange = people::changeName,
                        onSave = people::saveMember,
                        onToggleActive = people::toggleSelectedMemberActive,
                        onSelect = people::selectMember,
                    )

                    tab == AppScreen.Export -> ExportScreen(csv, message)
                }
            }
        }

        when {
            current != null -> Footer(
                label = when {
                    editing -> "Done editing"
                    current.closed -> "Edit attendance"
                    else -> "Finish check-in"
                },
                hint = when {
                    editing -> "Changes apply immediately."
                    current.closed -> ""
                    else -> "Unchecked members count as absent when you finish."
                },
            ) {
                if (canMark) {
                    events[events.indexOf(current)] = current.copy(closed = true)
                    editing = false
                } else {
                    editing = true
                }
            }

            tab == AppScreen.Events && !newEvent -> Footer("+ New event") {
                newEvent = true
                dateInput = SAMPLE_DATE
                message = ""
            }

            tab == AppScreen.Export -> Footer("Copy sample CSV") {
                clipboard.setText(AnnotatedString(csv))
                message = "Sample CSV copied."
            }
        }

        Rule()
        AppBottomBar(tab, newEvent, ::navigate)
    }
}
