package ch.verein.whosintoday.data

internal const val ATTENDANCE_CSV_NAME = "attendance.csv"

internal expect fun writeAttendanceCsvFile(path: String, bytes: ByteArray): Boolean
