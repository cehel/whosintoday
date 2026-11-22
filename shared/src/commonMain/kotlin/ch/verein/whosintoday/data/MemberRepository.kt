package ch.verein.whosintoday.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import ch.verein.whosintoday.domain.Member

internal class MemberRepository(private val database: SQLiteConnection) {
    fun list(): List<Member> = database.prepare(
        "SELECT id, name, active FROM members ORDER BY id",
    ).use { statement ->
        buildList {
            while (statement.step()) add(statement.readMember())
        }
    }

    fun add(name: String): Member = database.prepare(
        "INSERT INTO members (name) VALUES (?) RETURNING id, name, active",
    ).use { statement ->
        statement.bindText(1, validatedName(name))
        statement.changedMember()
    }

    fun rename(id: Long, name: String): Member = database.prepare(
        "UPDATE members SET name = ? WHERE id = ? RETURNING id, name, active",
    ).use { statement ->
        statement.bindText(1, validatedName(name))
        statement.bindLong(2, id)
        statement.changedMember()
    }

    fun setActive(id: Long, active: Boolean): Member = database.prepare(
        "UPDATE members SET active = ? WHERE id = ? RETURNING id, name, active",
    ).use { statement ->
        statement.bindLong(1, if (active) 1L else 0L)
        statement.bindLong(2, id)
        statement.changedMember()
    }

    private fun validatedName(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) throw EmptyMemberName()
        if (trimmed.length > 100) throw LongMemberName()
        if (trimmed.any { it.isISOControl() }) throw InvalidMemberName()
        return trimmed
    }

    private fun SQLiteStatement.changedMember(): Member {
        check(step()) { "Member no longer exists." }
        val member = readMember()
        // Complete the statement before reporting that the write succeeded.
        check(!step())
        return member
    }

    private fun SQLiteStatement.readMember() = Member(getLong(0), getText(1), getLong(2) != 0L)
}

internal class EmptyMemberName : IllegalArgumentException()

internal class LongMemberName : IllegalArgumentException()

internal class InvalidMemberName : IllegalArgumentException()
