package ch.verein.whosintoday

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val canvas = Color(0xFFFAF9F2)
private val surface = Color(0xFFFFFDF8)
private val line = Color(0xFFD8DED3)
private val muted = Color(0xFF4F5D53)
private val accent = Color(0xFF204B3C)
private val soft = Color(0xFFE1ECE3)
private val ink = Color(0xFF20312B)
private val gutter = 19.dp
private val body = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 15.sp, lineHeight = 22.5.sp)

private enum class AppScreen(val label: String) {
    CheckIn("Check-in"), Events("Events"), People("People"), Export("Export"),
}

private const val sampleDate = "2026-11-18"
private val months = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)
private val weekdays = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

private data class EventDate(val year: Int, val month: Int, val day: Int) {
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

private fun parseDate(value: String): EventDate? {
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

private data class Member(val id: Int, val name: String, val active: Boolean = true)
private data class Event(val date: String, val attendance: Map<Int, Boolean>, val closed: Boolean = false) {
    val dateLabel = requireNotNull(parseDate(date))
    val present get() = attendance.values.count { it }
    val total get() = attendance.size
    val countLabel get() = "$present / $total present"
}

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = accent, background = canvas, surface = surface,
        onSurface = ink, onBackground = ink, onSurfaceVariant = muted, outline = line,
    )) {
        AttendancePrototype()
    }
}

@Suppress("DEPRECATION") // The text clipboard API is available on both Compose targets.
@Composable
private fun AttendancePrototype() {
    var tabName by rememberSaveable { mutableStateOf(AppScreen.CheckIn.name) }
    val tab = AppScreen.valueOf(tabName)
    val members = remember {
        mutableStateListOf("Amélie Rey", "Jonas Berger", "Lea Imhof", "Luca Morel", "Mia Arnold", "Noé Favre")
            .mapIndexed(::Member).toMutableStateList()
    }
    val events = remember {
        fun sample(date: String, vararg present: Int, closed: Boolean = true) =
            Event(date, members.associate { it.id to (it.id in present) }, closed)
        mutableStateListOf(
            sample(sampleDate, 0, 2, closed = false),
            sample("2026-11-04", 0, 1, 3, 5),
            sample("2026-10-21", 0, 1, 2, 4, 5),
        )
    }
    var eventDate by remember { mutableStateOf<String?>(sampleDate) }
    var editing by remember { mutableStateOf(false) }
    var newEvent by remember { mutableStateOf(false) }
    var dateInput by remember { mutableStateOf(sampleDate) }
    var memberId by remember { mutableStateOf<Int?>(null) }
    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val sortedMembers = members.sortedBy { it.name.lowercase() }
    val current = events.find { it.date == eventDate }
    val canMark = current != null && (!current.closed || editing)
    val clipboard = LocalClipboardManager.current
    val csv = if (tab == AppScreen.Export) attendanceCsv(sortedMembers, events) else ""

    fun open(date: String) {
        eventDate = date
        tabName = if (date == sampleDate) AppScreen.CheckIn.name else AppScreen.Events.name
        editing = false
        newEvent = false
        message = ""
    }

    fun navigate(screen: AppScreen) {
        tabName = screen.name
        eventDate = sampleDate.takeIf { screen == AppScreen.CheckIn }
        editing = false
        newEvent = false
        memberId = null
        name = ""
        message = ""
    }

    Column(
        Modifier.fillMaxSize().background(canvas)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        Row(
            Modifier.fillMaxWidth().background(surface)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .heightIn(min = 58.dp).padding(horizontal = gutter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Label("Who’s In Today", size = 13, weight = 750)
            if (newEvent) {
                TextAction("← Events") { navigate(AppScreen.Events) }
            }
        }
        Rule()

        key(tab, current?.date, newEvent) {
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(gutter),
            ) {
                when {
                    current != null -> {
                        Heading(current.dateLabel.shortLabel)
                        Row(
                            Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Label(when {
                                editing -> "Editing attendance"
                                current.closed -> "Completed event"
                                else -> "Check in attendees"
                            }, color = muted, modifier = Modifier.weight(1f).padding(end = 10.dp))
                            Badge(current.countLabel)
                        }
                        Box(
                            Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 14.dp)
                                .height(5.dp).clip(CircleShape).background(line),
                        ) {
                            val fraction = if (current.total == 0) 0f else current.present.toFloat() / current.total
                            Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(accent, CircleShape))
                        }
                        val roster = sortedMembers.filter { it.id in current.attendance }
                        Rows(roster) { member ->
                            val checked = current.attendance[member.id] == true
                            val interaction = if (canMark) {
                                Modifier.background(if (checked) soft else surface)
                                    .toggleable(checked, role = Role.Checkbox) { isPresent ->
                                        events[events.indexOf(current)] = current.copy(
                                            attendance = current.attendance + (member.id to isPresent),
                                        )
                                    }
                            } else Modifier
                            ListRow(interaction) {
                                Row(
                                    Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        Modifier.size(30.dp).background(
                                            if (canMark && checked) surface else soft, CircleShape,
                                        ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        val initials = member.name.split(Regex("\\s+")).take(2)
                                            .joinToString("") { it.take(1) }
                                        Label(initials, size = 10, weight = 800, color = accent)
                                    }
                                    Label(member.name, weight = 620)
                                }
                                if (canMark) CheckMark(checked)
                                else Label(if (checked) "Present" else "Absent", size = 12, color = muted)
                            }
                        }
                    }
                    newEvent -> {
                        Heading("New event")
                        Field("Event date", dateInput, { dateInput = it.take(10) })
                        Label("Each date has one attendance record.", size = 13, color = muted,
                            modifier = Modifier.padding(bottom = 10.dp))
                        Primary("Start check-in") {
                            when {
                                parseDate(dateInput) == null -> message = "Enter a date as YYYY-MM-DD."
                                events.any { it.date == dateInput } -> open(dateInput)
                                members.none { it.active } -> message = "Add an active member before starting an event."
                                else -> {
                                    events += Event(dateInput, members.filter { it.active }.associate { it.id to false })
                                    open(dateInput)
                                }
                            }
                        }
                        Status(message)
                    }
                    tab == AppScreen.Events -> {
                        Heading("Events")
                        val (past, ongoing) = events.sortedByDescending { it.date }.partition { it.closed }
                        if (ongoing.isNotEmpty()) EventRows("In progress", ongoing, ::open)
                        EventRows("Past events", past, ::open)
                    }
                    tab == AppScreen.People -> {
                        Heading("People")
                        val member = members.find { it.id == memberId }
                        Field(if (member == null) "Add a member" else "Edit name", name, { name = it.take(100) })
                        Primary(if (member == null) "Add member" else "Save name") {
                            val trimmed = name.trim()
                            if (trimmed.isEmpty()) {
                                message = "Enter a name."
                            } else {
                                if (member == null) members += Member(members.size, trimmed)
                                else members[members.indexOf(member)] = member.copy(name = trimmed)
                                memberId = null
                                name = ""
                                message = "Member saved."
                            }
                        }
                        Status(message)
                        if (member != null) {
                            TextAction(if (member.active) "Archive member" else "Restore member") {
                                members[members.indexOf(member)] = member.copy(active = !member.active)
                                memberId = null
                                name = ""
                                message = if (member.active) "Member archived." else "Member restored."
                            }
                        }
                        Rows(sortedMembers, Modifier.padding(top = 12.dp)) { person ->
                            ListRow(Modifier.clickable(role = Role.Button) {
                                memberId = person.id
                                name = person.name
                                message = ""
                            }) {
                                Label(person.name, weight = 620, modifier = Modifier.weight(1f))
                                Label(if (person.active) "Edit" else "Archived · Edit", size = 12, color = muted)
                            }
                        }
                    }
                    tab == AppScreen.Export -> {
                        Heading("Export attendance")
                        Label("Completed events · 1 present · 0 absent · blank outside the event roster",
                            color = muted, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
                        val shape = RoundedCornerShape(10.dp)
                        Text(csv, Modifier.fillMaxWidth().background(surface, shape).border(1.dp, line, shape)
                            .padding(12.dp), color = ink,
                            style = body.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp))
                        Status(message)
                    }
                }
            }
        }

        when {
            current != null -> Footer(
                label = when {
                    editing -> "Done editing"
                    current.closed -> "Edit attendance"
                    else -> "Finish check-in"
                },
                hint = when {
                    editing -> "Changes apply immediately."
                    current.closed -> ""
                    else -> "Unchecked members count as absent when you finish."
                },
            ) {
                if (canMark) {
                    events[events.indexOf(current)] = current.copy(closed = true)
                    editing = false
                } else editing = true
            }
            tab == AppScreen.Events && !newEvent -> Footer("+ New event") {
                newEvent = true
                dateInput = sampleDate
                message = ""
            }
            tab == AppScreen.Export -> Footer("Copy sample CSV") {
                clipboard.setText(AnnotatedString(csv))
                message = "Sample CSV copied."
            }
        }

        Rule()
        Row(
            Modifier.fillMaxWidth().background(surface)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .selectableGroup().padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            AppScreen.entries.forEach { screen ->
                val selected = screen == tab && !newEvent
                Box(
                    Modifier.heightIn(min = 44.dp).clip(RoundedCornerShape(8.dp))
                        .background(if (selected) soft else surface)
                        .selectable(selected, role = Role.Tab, onClick = { navigate(screen) })
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Label(screen.label, size = 13, weight = if (selected) 750 else 400,
                        color = if (selected) accent else muted)
                }
            }
        }
    }
}

@Composable
private fun Label(
    label: String,
    modifier: Modifier = Modifier,
    size: Int = 15,
    weight: Int = 400,
    color: Color = ink,
    align: TextAlign? = null,
) {
    Text(label, modifier, color = color, textAlign = align, style = body.copy(
        fontSize = size.sp, lineHeight = (size * 1.5).sp, fontWeight = FontWeight(weight),
    ))
}

@Composable
private fun Heading(label: String) {
    Text(label, color = ink, style = TextStyle(
        fontFamily = FontFamily.Serif, fontSize = 31.sp, lineHeight = 34.sp,
        fontWeight = FontWeight.Medium, letterSpacing = (-1).sp,
    ))
}

@Composable
private fun Rule() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(line))
}

@Composable
private fun Badge(label: String) {
    Label(label, Modifier.background(soft, CircleShape).padding(horizontal = 9.dp, vertical = 4.dp),
        size = 12, weight = 750, color = accent)
}

@Composable
private fun <T> Rows(items: List<T>, modifier: Modifier = Modifier, item: @Composable (T) -> Unit) {
    if (items.isEmpty()) return
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier.fillMaxWidth().clip(shape).background(surface).border(1.dp, line, shape)
            .padding(1.dp),
    ) {
        items.forEachIndexed { index, value ->
            if (index > 0) Rule()
            item(value)
        }
    }
}

@Composable
private fun ListRow(modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().then(modifier).heightIn(min = 51.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun EventRows(title: String, events: List<Event>, open: (String) -> Unit) {
    Label(title, Modifier.padding(top = 20.dp, bottom = 12.dp), size = 14, weight = 720)
    Rows(events) { event ->
        ListRow(Modifier.clickable(role = Role.Button) { open(event.date) }) {
            Column(Modifier.weight(1f)) {
                Label(event.dateLabel.fullLabel, weight = 700)
                Label(event.countLabel, size = 12, color = muted)
            }
            if (event.closed) Label("›", size = 22, color = accent) else Badge("In progress")
        }
    }
}

@Composable
private fun CheckMark(checked: Boolean) {
    Canvas(Modifier.size(20.dp)) {
        val stroke = 1.dp.toPx()
        val radius = CornerRadius(3.dp.toPx())
        if (checked) {
            drawRoundRect(accent, cornerRadius = radius)
            drawLine(Color.White, Offset(size.width * .25f, size.height * .51f),
                Offset(size.width * .43f, size.height * .69f), 2.dp.toPx(), StrokeCap.Round)
            drawLine(Color.White, Offset(size.width * .43f, size.height * .69f),
                Offset(size.width * .75f, size.height * .35f), 2.dp.toPx(), StrokeCap.Round)
        } else {
            drawRoundRect(surface, cornerRadius = radius)
            drawRoundRect(muted, Offset(stroke / 2, stroke / 2),
                Size(size.width - stroke, size.height - stroke), radius, style = Stroke(stroke))
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    Label(label, Modifier.padding(top = 14.dp, bottom = 5.dp), weight = 650)
    val shape = RoundedCornerShape(9.dp)
    BasicTextField(
        value = value, onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).background(surface, shape)
            .border(1.dp, line, shape).padding(9.dp),
        textStyle = body.copy(color = ink), singleLine = true, cursorBrush = SolidColor(accent),
    )
}

@Composable
private fun Primary(label: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = 44.dp).clip(RoundedCornerShape(10.dp))
            .background(accent).clickable(role = Role.Button, onClick = onClick).padding(9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Label(label, weight = 720, color = Color.White, align = TextAlign.Center)
    }
}

@Composable
private fun Footer(label: String, hint: String = "", onClick: () -> Unit) {
    Rule()
    Column(Modifier.fillMaxWidth().background(surface).padding(horizontal = gutter, vertical = 10.dp)) {
        Primary(label, onClick)
        if (hint.isNotEmpty()) {
            Label(hint, Modifier.fillMaxWidth().padding(top = 4.dp), size = 12,
                color = muted, align = TextAlign.Center)
        }
    }
}

@Composable
private fun TextAction(label: String, onClick: () -> Unit) {
    Box(Modifier.heightIn(min = 44.dp).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.CenterStart) {
        Label(label, weight = 700, color = accent)
    }
}

@Composable
private fun Status(message: String) {
    Label(message, Modifier.padding(top = 8.dp), size = 13, color = accent)
}

private fun attendanceCsv(members: List<Member>, events: List<Event>): String {
    val completed = events.filter { it.closed }.sortedBy { it.date }
    val roster = members.filter { member -> member.active || completed.any { member.id in it.attendance } }
    return buildList {
        add(listOf("Name") + completed.map { it.date })
        roster.forEach { member ->
            add(listOf(member.name) + completed.map { event ->
                when (event.attendance[member.id]) {
                    true -> "1"
                    false -> "0"
                    null -> ""
                }
            })
        }
    }.joinToString("\r\n") { row -> row.joinToString(",") { csvCell(it) } }
}

private fun csvCell(value: String): String {
    val safe = if (value.firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r', '\n')) "'$value" else value
    return if (safe.any { it in ",\"\r\n" }) "\"${safe.replace("\"", "\"\"")}\"" else safe
}
