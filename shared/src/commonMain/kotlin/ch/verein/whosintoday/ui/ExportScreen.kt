package ch.verein.whosintoday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ExportScreen(csv: String, message: String) {
    Heading("Export attendance")
    Label(
        "Completed events · 1 present · 0 absent · blank outside the event roster",
        color = muted,
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
    )
    val shape = RoundedCornerShape(10.dp)
    Text(
        csv,
        Modifier.fillMaxWidth().background(surface, shape).border(1.dp, line, shape)
            .padding(12.dp),
        color = ink,
        style = body.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp),
    )
    Status(message)
}
