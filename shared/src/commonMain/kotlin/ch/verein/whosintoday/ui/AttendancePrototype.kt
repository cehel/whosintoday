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
import org.jetbrains.compose.resources.stringResource
import whosintoday.shared.generated.resources.Res
import whosintoday.shared.generated.resources.changes_apply_immediately
import whosintoday.shared.generated.resources.copy_csv
import whosintoday.shared.generated.resources.done_editing
import whosintoday.shared.generated.resources.edit_attendance
import whosintoday.shared.generated.resources.finish_check_in
import whosintoday.shared.generated.resources.new_event_action
import whosintoday.shared.generated.resources.share_csv
import whosintoday.shared.generated.resources.unchecked_members_count_as_absent_when_you_finish

@Suppress("DEPRECATION") // The text clipboard API is available on both Compose targets.
@Composable
internal fun AttendancePrototype(
    peopleViewModel: PeopleViewModel,
    attendanceViewModel: AttendanceViewModel,
    statisticsViewModel: StatisticsViewModel,
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
        AppTopBar(newEvent = state.newEvent, eventAttendance = current != null) { navigate(AppScreen.Events) }
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
                        DeleteEventAction(
                            current,
                            state.confirmingDelete,
                            attendanceViewModel::requestDelete,
                            attendanceViewModel::cancelDelete,
                            attendanceViewModel::confirmDelete,
                        )
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

                    state.screen == AppScreen.Statistics -> {
                        val statistics by statisticsViewModel.viewState.collectAsStateWithLifecycle()
                        StatisticsScreen(
                            statistics,
                            statisticsViewModel::showPanel,
                            statisticsViewModel::searchMember,
                            statisticsViewModel::selectMember,
                        )
                    }

                    state.screen == AppScreen.Export -> {
                        val csv = attendanceCsv(sortedMembers, state.events)
                        ExportScreen(csv, state.message)
                        TextAction(stringResource(Res.string.copy_csv)) {
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
                    state.editing -> stringResource(Res.string.done_editing)
                    current.closed -> stringResource(Res.string.edit_attendance)
                    else -> stringResource(Res.string.finish_check_in)
                },
                hint = when {
                    state.editing -> stringResource(Res.string.changes_apply_immediately)
                    current.closed -> ""
                    else -> stringResource(Res.string.unchecked_members_count_as_absent_when_you_finish)
                },
                onClick = attendanceViewModel::finishOrEdit,
            )

            state.screen == AppScreen.Events && !state.newEvent -> Footer(stringResource(Res.string.new_event_action), onClick = attendanceViewModel::showNewEvent)

            state.screen == AppScreen.Export && shareCsv != null -> Footer(stringResource(Res.string.share_csv)) {
                attendanceViewModel.shareCsv(sortedMembers, shareCsv)
            }
        }

        Rule()
        AppBottomBar(state.screen, state.newEvent, ::navigate)
    }
}
