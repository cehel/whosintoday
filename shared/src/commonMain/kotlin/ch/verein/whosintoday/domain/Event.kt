package ch.verein.whosintoday.domain

internal data class CalendarDate(val year: Int, val month: Int, val day: Int)

internal fun parseDate(value: String): CalendarDate? {
    if (!Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(value)) return null
    val (year, month, day) = value.split('-').map { it.toInt() }
    if (year == 0 || month !in 1..12) return null
    val leapYear = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    val days = when (month) {
        2 -> if (leapYear) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return CalendarDate(year, month, day).takeIf { day in 1..days }
}

internal data class Event(val date: String, val attendance: Map<Long, Boolean>, val closed: Boolean = false) {
    val present get() = attendance.values.count { it }
    val total get() = attendance.size
}
