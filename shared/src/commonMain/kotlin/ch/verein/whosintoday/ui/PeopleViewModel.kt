package ch.verein.whosintoday.ui

import androidx.lifecycle.ViewModel
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteException
import ch.verein.whosintoday.data.MemberRepository
import ch.verein.whosintoday.data.openAttendanceDatabase
import ch.verein.whosintoday.domain.Member
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class PeopleViewState(
    val members: List<Member>,
    val selectedMember: Member? = null,
    val name: String = "",
    val message: String = "",
)

internal class PeopleViewModel private constructor(
    private val database: SQLiteConnection,
    private val repository: MemberRepository,
    members: List<Member>,
) : ViewModel() {
    private val state = MutableStateFlow(PeopleViewState(members))
    val viewState = state.asStateFlow()

    fun selectMember(member: Member) {
        state.value = state.value.copy(selectedMember = member, name = member.name, message = "")
    }

    fun changeName(name: String) {
        state.value = state.value.copy(name = name)
    }

    fun saveMember() {
        val current = state.value
        save("Member saved.") {
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
        val message = if (member.active) "Member archived." else "Member restored."
        save(message) { repository.setActive(member.id, !member.active) }
    }

    fun resetEditor() {
        state.value = PeopleViewState(state.value.members)
    }

    private fun save(message: String, write: () -> Member) {
        val current = state.value
        val saved = try {
            write()
        } catch (failure: IllegalArgumentException) {
            state.value = current.copy(message = failure.message.orEmpty())
            return
        } catch (_: SQLiteException) {
            state.value = current.copy(message = "Could not save the member. Try again.")
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
                if (sample) {
                    listOf("Amélie Rey", "Jonas Berger", "Lea Imhof", "Luca Morel", "Mia Arnold", "Noé Favre")
                        .forEach { repository.add(it) }
                }
                return PeopleViewModel(database, repository, repository.list())
            } catch (failure: Throwable) {
                runCatching { database.close() }.onFailure(failure::addSuppressed)
                throw failure
            }
        }
    }
}
