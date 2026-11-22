package ch.verein.whosintoday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Suppress("DEPRECATION") // The text clipboard API is available on both Compose targets.
@Composable
internal fun AttendancePrototype(
    peopleViewModel: PeopleViewModel,
    attendanceViewModel: AttendanceViewModel,
    shareCsv: ((String, ByteArray) -> Boolean)?,
) {
    val state by attendanceViewModel.viewState.collectAsStateWithLifecycle()
    val people by peopleViewModel.viewState.collectAsStateWithLifecycle()
    val sortedMembers = people.members.sortedBy { it.name.lowercase() }
    val current = state.currentEvent
    val clipboard = LocalClipboardManager.current

    fun navigate(screen: AppScreen) {
        attendanceViewModel.navigate(screen)
        peopleViewModel.resetEditor()
    }

    Column(
        Modifier.fillMaxSize().background(canvas)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        AppTopBar(state.newEvent) { navigate(AppScreen.Events) }
        Rule()

        key(state.screen, current?.date, state.newEvent) {
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(gutter),
            ) {
                when {
                    current != null -> {
                        EventAttendanceScreen(current, state.editing, state.canMark, sortedMembers, attendanceViewModel::changeAttendance)
                        Status(state.message)
                    }

                    state.newEvent -> NewEventScreen(
                        state.dateInput,
                        state.message,
                        attendanceViewModel::changeDate,
                        attendanceViewModel::startEvent,
                    )

                    state.screen == AppScreen.Events -> EventsScreen(state.events, attendanceViewModel::openEvent)

                    state.screen == AppScreen.People -> PeopleScreen(
                        member = people.selectedMember,
                        sortedMembers = sortedMembers,
                        name = people.name,
                        message = people.message,
                        onNameChange = peopleViewModel::changeName,
                        onSave = peopleViewModel::saveMember,
                        onToggleActive = peopleViewModel::toggleSelectedMemberActive,
                        onSelect = peopleViewModel::selectMember,
                    )

                    state.screen == AppScreen.Export -> {
                        val csv = attendanceCsv(sortedMembers, state.events)
                        ExportScreen(csv, state.message)
                        TextAction("Copy CSV") {
                            clipboard.setText(AnnotatedString(csv))
                            attendanceViewModel.csvCopied()
                        }
                    }
                }
            }
        }

        when {
            current != null -> Footer(
                label = when {
                    state.editing -> "Done editing"
                    current.closed -> "Edit attendance"
                    else -> "Finish check-in"
                },
                hint = when {
                    state.editing -> "Changes apply immediately."
                    current.closed -> ""
                    else -> "Unchecked members count as absent when you finish."
                },
                onClick = attendanceViewModel::finishOrEdit,
            )

            state.screen == AppScreen.Events && !state.newEvent -> Footer("+ New event", onClick = attendanceViewModel::showNewEvent)

            state.screen == AppScreen.Export && shareCsv != null -> Footer("Share CSV") {
                attendanceViewModel.shareCsv(sortedMembers, shareCsv)
            }
        }

        Rule()
        AppBottomBar(state.screen, state.newEvent, ::navigate)
    }
}
