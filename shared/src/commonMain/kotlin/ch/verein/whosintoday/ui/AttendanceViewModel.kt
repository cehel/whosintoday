package ch.verein.whosintoday.ui

import androidx.lifecycle.ViewModel
import androidx.sqlite.SQLiteException
import ch.verein.whosintoday.data.EventRepository
import ch.verein.whosintoday.domain.Event
import ch.verein.whosintoday.domain.Member
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class AttendanceViewState(
    val events: List<Event>,
    val dateInput: String,
    val screen: AppScreen = AppScreen.Events,
    val eventDate: String? = null,
    val newEvent: Boolean = false,
    val editing: Boolean = false,
    val message: String = "",
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
            message = "",
        )
    }

    fun showNewEvent() {
        attendance.value = attendance.value.copy(newEvent = true, dateInput = today(), message = "")
    }

    fun changeDate(date: String) {
        attendance.value = attendance.value.copy(dateInput = date.take(10), message = "")
    }

    fun startEvent() {
        val current = attendance.value
        val date = current.dateInput
        val event = try {
            events.start(date)
        } catch (failure: IllegalArgumentException) {
            attendance.value = current.copy(message = failure.message.orEmpty())
            return
        } catch (_: SQLiteException) {
            attendance.value = current.copy(message = "Could not start the event. Try again.")
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
            message = "",
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
            attendance.value = current.copy(editing = !current.editing, message = "")
        } else {
            writeEvent { events.finish(event.date) }
        }
    }

    fun csvCopied() {
        attendance.value = attendance.value.copy(message = "CSV copied.")
    }

    fun shareCsv(sortedMembers: List<Member>, share: (String, ByteArray) -> Boolean) {
        val current = attendance.value
        val bytes = attendanceCsv(sortedMembers, current.events).encodeToByteArray()
        val message = if (share("attendance.csv", bytes)) "" else "Could not open sharing. Try again."
        attendance.value = current.copy(message = message)
    }

    private fun writeEvent(write: () -> Event) {
        val current = attendance.value
        try {
            saveEvent(write())
        } catch (_: SQLiteException) {
            attendance.value = current.copy(message = "Could not save attendance. Try again.")
        } catch (failure: IllegalStateException) {
            attendance.value = current.copy(message = failure.message.orEmpty())
        }
    }

    private fun saveEvent(event: Event) {
        val current = attendance.value
        attendance.value = current.copy(
            events = current.events.filterNot { it.date == event.date } + event,
            message = "",
        )
    }
}
