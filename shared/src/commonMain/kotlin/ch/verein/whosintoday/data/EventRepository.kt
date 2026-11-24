package ch.verein.whosintoday.data

import androidx.sqlite.SQLiteConnection
import ch.verein.whosintoday.domain.Event
import ch.verein.whosintoday.domain.parseDate

internal class EventRepository(private val database: SQLiteConnection) {
    fun today(): String = database.prepare("SELECT date('now', 'localtime')").use {
        check(it.step())
        it.getText(0)
    }

    fun list(): List<Event> = readEvents()

    fun start(date: String): Event {
        if (parseDate(date) == null) throw InvalidEventDate()
        return database.writeTransaction {
            val existing = readEvents(date).singleOrNull()
            if (existing != null) return@writeTransaction existing

            database.prepare("INSERT INTO events (event_date) VALUES (?)").use {
                it.bindText(1, date)
                it.step()
            }
            database.prepare(
                "INSERT INTO attendance (event_date, member_id) SELECT ?, id FROM members WHERE active = 1",
            ).use {
                it.bindText(1, date)
                it.step()
            }

            val event = readEvents(date).single()
            if (event.attendance.isEmpty()) throw EmptyEventRoster()
            event
        }
    }

    fun setPresent(event: Event, memberId: Long, present: Boolean): Event = database.writeTransaction {
        database.prepare(
            """
            UPDATE attendance SET present = ?
            WHERE event_date = ? AND member_id = ?
                AND EXISTS (SELECT 1 FROM events WHERE event_date = ? AND closed = ?)
            RETURNING member_id
            """.trimIndent(),
        ).use {
            it.bindLong(1, if (present) 1L else 0L)
            it.bindText(2, event.date)
            it.bindLong(3, memberId)
            it.bindText(4, event.date)
            it.bindLong(5, if (event.closed) 1L else 0L)
            if (!it.step()) throw EventChecklistChanged()
            check(!it.step())
        }
        readEvents(event.date).single()
    }

    fun finish(date: String): Event = database.writeTransaction {
        database.prepare("UPDATE events SET closed = 1 WHERE event_date = ?").use {
            it.bindText(1, date)
            it.step()
        }
        readEvents(date).single()
    }

    fun saveNotes(date: String, notes: String): Event? = database.writeTransaction {
        database.prepare("UPDATE events SET notes = ? WHERE event_date = ?").use {
            it.bindText(1, notes)
            it.bindText(2, date)
            it.step()
        }
        readEvents(date).singleOrNull()
    }

    fun delete(date: String) {
        database.prepare("DELETE FROM events WHERE event_date = ?").use {
            it.bindText(1, date)
            it.step()
        }
    }

    private fun readEvents(date: String? = null): List<Event> = database.prepare(
        """
        SELECT events.event_date, events.closed, attendance.member_id, attendance.present, events.notes
        FROM events LEFT JOIN attendance ON attendance.event_date = events.event_date
        WHERE ? IS NULL OR events.event_date = ?
        ORDER BY events.event_date DESC
        """.trimIndent(),
    ).use { statement ->
        if (date == null) {
            statement.bindNull(1)
            statement.bindNull(2)
        } else {
            statement.bindText(1, date)
            statement.bindText(2, date)
        }
        val events = mutableListOf<Event>()
        var current: Event? = null
        var attendance = mutableMapOf<Long, Boolean>()
        while (statement.step()) {
            val eventDate = statement.getText(0)
            if (current?.date != eventDate) {
                attendance = mutableMapOf()
                current = Event(eventDate, attendance, closed = statement.getLong(1) != 0L, notes = statement.getText(4))
                events.add(current)
            }
            if (!statement.isNull(2)) {
                attendance[statement.getLong(2)] = statement.getLong(3) != 0L
            }
        }
        events
    }
}

internal class InvalidEventDate : IllegalArgumentException()

internal class EmptyEventRoster : IllegalArgumentException()

internal class EventChecklistChanged : IllegalStateException()
