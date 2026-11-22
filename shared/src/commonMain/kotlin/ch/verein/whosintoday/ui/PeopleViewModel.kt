package ch.verein.whosintoday.ui

import androidx.lifecycle.ViewModel
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteException
import ch.verein.whosintoday.data.EmptyMemberName
import ch.verein.whosintoday.data.EventRepository
import ch.verein.whosintoday.data.InvalidMemberName
import ch.verein.whosintoday.data.LongMemberName
import ch.verein.whosintoday.data.MemberRepository
import ch.verein.whosintoday.data.openAttendanceDatabase
import ch.verein.whosintoday.domain.Member
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.StringResource
import whosintoday.shared.generated.resources.Res
import whosintoday.shared.generated.resources.could_not_save_the_member
import whosintoday.shared.generated.resources.empty_member_name
import whosintoday.shared.generated.resources.invalid_member_name
import whosintoday.shared.generated.resources.long_member_name
import whosintoday.shared.generated.resources.member_archived
import whosintoday.shared.generated.resources.member_restored
import whosintoday.shared.generated.resources.member_saved

internal data class PeopleViewState(
    val members: List<Member>,
    val selectedMember: Member? = null,
    val name: String = "",
    val message: StringResource? = null,
)

internal class PeopleViewModel private constructor(
    private val database: SQLiteConnection,
    private val repository: MemberRepository,
    val events: EventRepository,
) : ViewModel() {
    private val state = MutableStateFlow(PeopleViewState(repository.list()))
    val viewState = state.asStateFlow()

    fun selectMember(member: Member) {
        state.value = state.value.copy(selectedMember = member, name = member.name, message = null)
    }

    fun changeName(name: String) {
        state.value = state.value.copy(name = name)
    }

    fun saveMember() {
        val current = state.value
        save(Res.string.member_saved) {
            val member = current.selectedMember
            if (member == null) {
                repository.add(current.name)
            } else {
                repository.rename(member.id, current.name)
            }
        }
    }

    fun toggleSelectedMemberActive() {
        val member = state.value.selectedMember ?: return
        val message = if (member.active) Res.string.member_archived else Res.string.member_restored
        save(message) { repository.setActive(member.id, !member.active) }
    }

    fun resetEditor() {
        state.value = PeopleViewState(state.value.members)
    }

    private fun save(message: StringResource, write: () -> Member) {
        val current = state.value
        val saved = try {
            write()
        } catch (_: EmptyMemberName) {
            state.value = current.copy(message = Res.string.empty_member_name)
            return
        } catch (_: LongMemberName) {
            state.value = current.copy(message = Res.string.long_member_name)
            return
        } catch (_: InvalidMemberName) {
            state.value = current.copy(message = Res.string.invalid_member_name)
            return
        } catch (_: SQLiteException) {
            state.value = current.copy(message = Res.string.could_not_save_the_member)
            return
        }
        val members = current.members.toMutableList()
        val index = members.indexOfFirst { it.id == saved.id }
        if (index == -1) members.add(saved) else members[index] = saved
        state.value = PeopleViewState(members, message = message)
    }

    override fun onCleared() {
        database.close()
    }

    companion object {
        fun open(databasePath: String, sample: Boolean): PeopleViewModel {
            val database = openAttendanceDatabase(databasePath)
            try {
                val repository = MemberRepository(database)
                val events = EventRepository(database)
                if (sample) seedSample(repository, events)
                return PeopleViewModel(database, repository, events)
            } catch (failure: Throwable) {
                runCatching { database.close() }.onFailure(failure::addSuppressed)
                throw failure
            }
        }

        private fun seedSample(members: MemberRepository, events: EventRepository) {
            val roster = listOf("Amélie Rey", "Jonas Berger", "Lea Imhof", "Luca Morel", "Mia Arnold", "Noé Favre")
                .map { members.add(it) }

            fun seed(date: String, vararg present: Int, closed: Boolean = true) {
                val event = events.start(date)
                present.forEach { events.setPresent(event, roster[it].id, true) }
                if (closed) events.finish(date)
            }

            seed(SAMPLE_DATE, 0, 2, closed = false)
            seed("2026-11-04", 0, 1, 3, 5)
            seed("2026-10-21", 0, 1, 2, 4, 5)
        }
    }
}
