package ch.verein.whosintoday.ui

import ch.verein.whosintoday.domain.CalendarDate
import ch.verein.whosintoday.domain.Event
import ch.verein.whosintoday.domain.parseDate

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

internal val CalendarDate.shortLabel get() = "$day ${months[month - 1]}"

internal val CalendarDate.fullLabel: String
    get() {
        // Gregorian weekday calculation, with January and February counted in the prior year.
        val adjustedYear = if (month < 3) year - 1 else year
        val offsets = listOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
        val weekday = adjustedYear + adjustedYear / 4 - adjustedYear / 100 +
            adjustedYear / 400 + offsets[month - 1] + day
        return "${weekdays[weekday % 7]}, $shortLabel"
    }

internal val Event.dateLabel get() = requireNotNull(parseDate(date))
internal val Event.countLabel get() = "$present / $total present"
