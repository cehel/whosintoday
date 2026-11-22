package ch.verein.whosintoday.ui

import androidx.compose.runtime.Composable
import ch.verein.whosintoday.domain.CalendarDate
import ch.verein.whosintoday.domain.Event
import ch.verein.whosintoday.domain.parseDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import whosintoday.shared.generated.resources.Res
import whosintoday.shared.generated.resources.attendance_count
import whosintoday.shared.generated.resources.date_day_month
import whosintoday.shared.generated.resources.date_weekday
import whosintoday.shared.generated.resources.date_weekday_year
import whosintoday.shared.generated.resources.events
import whosintoday.shared.generated.resources.export
import whosintoday.shared.generated.resources.month_names
import whosintoday.shared.generated.resources.people
import whosintoday.shared.generated.resources.statistics
import whosintoday.shared.generated.resources.weekday_names

internal enum class AppScreen(val label: StringResource) {
    Events(Res.string.events),
    People(Res.string.people),
    Statistics(Res.string.statistics),
    Export(Res.string.export),
}

internal const val SAMPLE_DATE = "2026-11-18"
private val weekdayOffsets = listOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)

@Composable
internal fun CalendarDate.shortLabel(): String {
    val months = stringArrayResource(Res.array.month_names)
    return stringResource(Res.string.date_day_month, day, months[month - 1])
}

@Composable
internal fun CalendarDate.fullLabel(includeYear: Boolean = false): String {
    // Gregorian weekday calculation, with January and February counted in the prior year.
    val adjustedYear = if (month < 3) year - 1 else year
    val weekday = adjustedYear + adjustedYear / 4 - adjustedYear / 100 +
        adjustedYear / 400 + weekdayOffsets[month - 1] + day
    val weekdays = stringArrayResource(Res.array.weekday_names)
    val weekdayName = weekdays[weekday % 7]
    val dayAndMonth = shortLabel()
    return if (includeYear) {
        stringResource(Res.string.date_weekday_year, weekdayName, dayAndMonth, year)
    } else {
        stringResource(Res.string.date_weekday, weekdayName, dayAndMonth)
    }
}

internal val Event.dateLabel get() = requireNotNull(parseDate(date))

@Composable
internal fun Event.countLabel() = stringResource(Res.string.attendance_count, present, total)
