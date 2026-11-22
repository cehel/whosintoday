package ch.verein.whosintoday.ui

import ch.verein.whosintoday.domain.Event
import ch.verein.whosintoday.domain.Member

internal fun attendanceCsv(members: List<Member>, events: List<Event>): String {
    val completed = events.filter { it.closed }.sortedBy { it.date }
    val roster = members.filter { member -> member.active || completed.any { member.id in it.attendance } }
    return buildList {
        add(listOf("Name") + completed.map { it.date })
        roster.forEach { member ->
            add(
                listOf(member.name) + completed.map { event ->
                    when (event.attendance[member.id]) {
                        true -> "1"
                        false -> "0"
                        null -> ""
                    }
                },
            )
        }
    }.joinToString("\r\n") { row -> row.joinToString(",") { csvCell(it) } }
}

private fun csvCell(value: String): String {
    val safe = if (value.firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r', '\n')) "'$value" else value
    return if (safe.any { it in ",\"\r\n" }) "\"${safe.replace("\"", "\"\"")}\"" else safe
}
