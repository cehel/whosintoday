package ch.verein.whosintoday.ui

import androidx.lifecycle.ViewModel
import androidx.sqlite.SQLiteException
import ch.verein.whosintoday.data.EmptyEventRoster
import ch.verein.whosintoday.data.EventChecklistChanged
import ch.verein.whosintoday.data.EventRepository
import ch.verein.whosintoday.data.InvalidEventDate
import ch.verein.whosintoday.domain.Event
import ch.verein.whosintoday.domain.Member
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.StringResource
import whosintoday.shared.generated.resources.Res
import whosintoday.shared.generated.resources.could_not_delete_the_event
import whosintoday.shared.generated.resources.could_not_open_sharing
import whosintoday.shared.generated.resources.could_not_save_attendance
import whosintoday.shared.generated.resources.could_not_start_the_event
import whosintoday.shared.generated.resources.csv_copied
import whosintoday.shared.generated.resources.empty_event_roster
import whosintoday.shared.generated.resources.event_checklist_changed
import whosintoday.shared.generated.resources.invalid_event_date

internal data class AttendanceViewState(
    val events: List<Event>,
    val dateInput: String,
    val screen: AppScreen = AppScreen.Events,
    val eventDate: String? = null,
    val newEvent: Boolean = false,
    val editing: Boolean = false,
    val notesDraft: String? = null,
    val notesSaveFailed: Boolean = false,
    val confirmingDelete: Boolean = false,
    val message: StringResource? = null,
) {
    val currentEvent get() = events.find { it.date == eventDate }
    val canMark get() = currentEvent?.let { !it.closed || editing } == true
}

internal class AttendanceViewModel(
    private val events: EventRepository,
    private val sample: Boolean,
) : ViewModel() {
    private val attendance = MutableStateFlow(
        AttendanceViewState(events.list(), today()),
    )
    val viewState = attendance.asStateFlow()

    private fun today() = if (sample) SAMPLE_DATE else events.today()

    fun navigate(screen: AppScreen) {
        val current = attendance.value
        attendance.value = current.copy(
            screen = screen,
            eventDate = null,
            newEvent = false,
            editing = false,
            notesDraft = null,
            notesSaveFailed = false,
            confirmingDelete = false,
            message = null,
        )
    }

    fun showNewEvent() {
        attendance.value = attendance.value.copy(newEvent = true, dateInput = today(), message = null)
    }

    fun changeDate(date: String) {
        attendance.value = attendance.value.copy(dateInput = date.take(10), message = null)
    }

    fun startEvent() {
        val current = attendance.value
        val date = current.dateInput
        val event = try {
            events.start(date)
        } catch (_: InvalidEventDate) {
            attendance.value = current.copy(message = Res.string.invalid_event_date)
            return
        } catch (_: EmptyEventRoster) {
            attendance.value = current.copy(message = Res.string.empty_event_roster)
            return
        } catch (_: SQLiteException) {
            attendance.value = current.copy(message = Res.string.could_not_start_the_event)
            return
        }
        saveEvent(event)
        openEvent(date)
    }

    fun openEvent(date: String) {
        attendance.value = attendance.value.copy(
            screen = AppScreen.Events,
            eventDate = date,
            newEvent = false,
            editing = false,
            notesDraft = null,
            notesSaveFailed = false,
            confirmingDelete = false,
            message = null,
        )
    }

    fun editNotes() {
        val current = attendance.value
        val event = current.currentEvent ?: return
        attendance.value = current.copy(notesDraft = event.notes, notesSaveFailed = false)
    }

    fun changeNotes(notes: String) {
        val current = attendance.value
        if (current.notesDraft == null) return
        attendance.value = current.copy(notesDraft = notes, notesSaveFailed = false)
    }

    fun cancelNotes() {
        attendance.value = attendance.value.copy(notesDraft = null, notesSaveFailed = false)
    }

    fun saveNotes() {
        val current = attendance.value
        val event = current.currentEvent ?: return
        val draft = current.notesDraft ?: return
        val updated = try {
            events.saveNotes(event.date, draft.trim())
        } catch (_: SQLiteException) {
            null
        }
        if (updated == null) {
            attendance.value = current.copy(notesSaveFailed = true)
            return
        }
        attendance.value = current.copy(
            events = current.events.map { if (it.date == updated.date) updated else it },
            notesDraft = null,
            notesSaveFailed = false,
        )
    }

    fun requestDelete() {
        val current = attendance.value
        if (current.currentEvent == null) return
        attendance.value = current.copy(confirmingDelete = true, message = null)
    }

    fun cancelDelete() {
        attendance.value = attendance.value.copy(confirmingDelete = false)
    }

    fun confirmDelete() {
        val current = attendance.value
        if (!current.confirmingDelete) return
        val event = current.currentEvent ?: return
        try {
            events.delete(event.date)
        } catch (_: SQLiteException) {
            attendance.value = current.copy(confirmingDelete = false, message = Res.string.could_not_delete_the_event)
            return
        }
        attendance.value = current.copy(
            events = current.events.filterNot { it.date == event.date },
            eventDate = null,
            editing = false,
            notesDraft = null,
            notesSaveFailed = false,
            confirmingDelete = false,
            message = null,
        )
    }

    fun changeAttendance(memberId: Long, present: Boolean) {
        val current = attendance.value
        val event = current.currentEvent ?: return
        if (!current.canMark || memberId !in event.attendance) return
        writeEvent { events.setPresent(event, memberId, present) }
    }

    fun finishOrEdit() {
        val current = attendance.value
        val event = current.currentEvent ?: return
        if (event.closed) {
            attendance.value = current.copy(editing = !current.editing, message = null)
        } else {
            writeEvent { events.finish(event.date) }
        }
    }

    fun csvCopied() {
        attendance.value = attendance.value.copy(message = Res.string.csv_copied)
    }

    fun shareCsv(sortedMembers: List<Member>, share: (String, ByteArray) -> Boolean) {
        val current = attendance.value
        val bytes = attendanceCsv(sortedMembers, current.events).encodeToByteArray()
        val message = if (share("attendance.csv", bytes)) null else Res.string.could_not_open_sharing
        attendance.value = current.copy(message = message)
    }

    private fun writeEvent(write: () -> Event) {
        val current = attendance.value
        try {
            saveEvent(write())
        } catch (_: SQLiteException) {
            attendance.value = current.copy(message = Res.string.could_not_save_attendance)
        } catch (_: EventChecklistChanged) {
            attendance.value = current.copy(message = Res.string.event_checklist_changed)
        }
    }

    private fun saveEvent(event: Event) {
        val current = attendance.value
        attendance.value = current.copy(
            events = current.events.filterNot { it.date == event.date } + event,
            message = null,
        )
    }
}
