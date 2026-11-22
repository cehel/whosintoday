package ch.verein.whosintoday.ui

internal enum class AppScreen(val label: String) {
    CheckIn("Check-in"),
    Events("Events"),
    People("People"),
    Export("Export"),
}

internal const val SAMPLE_DATE = "2026-11-18"
private val months = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)
private val weekdays = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

internal data class EventDate(val year: Int, val month: Int, val day: Int) {
    val shortLabel get() = "$day ${months[month - 1]}"
    val fullLabel: String
        get() {
            // Gregorian weekday calculation, with January and February counted in the prior year.
            val adjustedYear = if (month < 3) year - 1 else year
            val offsets = listOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
            val weekday = adjustedYear + adjustedYear / 4 - adjustedYear / 100 +
                adjustedYear / 400 + offsets[month - 1] + day
            return "${weekdays[weekday % 7]}, $shortLabel"
        }
}

internal fun parseDate(value: String): EventDate? {
    if (!Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(value)) return null
    val (year, month, day) = value.split('-').map { it.toInt() }
    if (year == 0 || month !in 1..12) return null
    val leapYear = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    val days = when (month) {
        2 -> if (leapYear) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return EventDate(year, month, day).takeIf { day in 1..days }
}

internal data class Event(val date: String, val attendance: Map<Long, Boolean>, val closed: Boolean = false) {
    val dateLabel = requireNotNull(parseDate(date))
    val present get() = attendance.values.count { it }
    val total get() = attendance.size
    val countLabel get() = "$present / $total present"
}
