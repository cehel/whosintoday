package ch.verein.whosintoday.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal class StatisticsViewModel(
    attendance: StateFlow<AttendanceViewState>,
    people: StateFlow<PeopleViewState>,
) : ViewModel() {
    private val selection = MutableStateFlow(StatisticsSelection())
    private val events = attendance.map { it.events }.distinctUntilChanged()
    private val sortedMembers = people.map { it.members }.distinctUntilChanged()
        .map { members -> members.sortedBy { it.name.lowercase() } }

    val viewState = combine(selection, events, sortedMembers) { selection, events, members ->
        selection.view(events, members)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        selection.value.view(attendance.value.events, people.value.members.sortedBy { it.name.lowercase() }),
    )

    fun showPanel(panel: StatisticsPanel) {
        selection.update { it.copy(panel = panel) }
    }

    fun searchMember(query: String) {
        selection.update { it.copy(query = query) }
    }

    fun selectMember(memberId: Long) {
        selection.update { it.copy(memberId = memberId, query = "") }
    }
}
