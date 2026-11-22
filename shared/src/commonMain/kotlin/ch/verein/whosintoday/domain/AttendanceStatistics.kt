package ch.verein.whosintoday.domain

import kotlin.math.roundToInt

internal fun attendancePercentage(present: Int, total: Int): Int? = if (total == 0) {
    null
} else {
    (100.0 * present / total).roundToInt()
}

internal class MemberAttendance(completedEvents: List<Event>, memberId: Long) {
    private val eligible = completedEvents.filter { memberId in it.attendance }
    val present = eligible.filter { it.attendance[memberId] == true }
    val absent = eligible.filter { it.attendance[memberId] == false }
    val percentage = attendancePercentage(present.size, eligible.size)
}
