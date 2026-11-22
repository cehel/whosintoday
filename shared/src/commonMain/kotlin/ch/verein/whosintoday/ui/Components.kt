package ch.verein.whosintoday.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun Label(
    label: String,
    modifier: Modifier = Modifier,
    size: Int = 15,
    weight: Int = 400,
    color: Color = ink,
    align: TextAlign? = null,
) {
    Text(
        label,
        modifier,
        color = color,
        textAlign = align,
        style = body.copy(
            fontSize = size.sp,
            lineHeight = (size * 1.5).sp,
            fontWeight = FontWeight(weight),
        ),
    )
}

@Composable
internal fun Heading(label: String) {
    Text(
        label,
        color = ink,
        style = TextStyle(
            fontFamily = FontFamily.Serif,
            fontSize = 31.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-1).sp,
        ),
    )
}

@Composable
internal fun Rule() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(line))
}

@Composable
internal fun Badge(label: String) {
    Label(
        label,
        Modifier.background(soft, CircleShape).padding(horizontal = 9.dp, vertical = 4.dp),
        size = 12,
        weight = 750,
        color = accent,
    )
}

@Composable
internal fun <T> Rows(items: List<T>, modifier: Modifier = Modifier, item: @Composable (T) -> Unit) {
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
internal fun ListRow(modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().then(modifier).heightIn(min = 51.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
internal fun CheckMark(checked: Boolean) {
    Canvas(Modifier.size(20.dp)) {
        val stroke = 1.dp.toPx()
        val radius = CornerRadius(3.dp.toPx())
        if (checked) {
            drawRoundRect(accent, cornerRadius = radius)
            drawLine(
                Color.White,
                Offset(size.width * .25f, size.height * .51f),
                Offset(size.width * .43f, size.height * .69f),
                2.dp.toPx(),
                StrokeCap.Round,
            )
            drawLine(
                Color.White,
                Offset(size.width * .43f, size.height * .69f),
                Offset(size.width * .75f, size.height * .35f),
                2.dp.toPx(),
                StrokeCap.Round,
            )
        } else {
            drawRoundRect(surface, cornerRadius = radius)
            drawRoundRect(
                muted,
                Offset(stroke / 2, stroke / 2),
                Size(size.width - stroke, size.height - stroke),
                radius,
                style = Stroke(stroke),
            )
        }
    }
}

@Composable
internal fun Field(label: String, value: String, onChange: (String) -> Unit) {
    Label(label, Modifier.padding(top = 14.dp, bottom = 5.dp), weight = 650)
    val shape = RoundedCornerShape(9.dp)
    BasicTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).background(surface, shape)
            .border(1.dp, line, shape).padding(9.dp),
        textStyle = body.copy(color = ink),
        singleLine = true,
        cursorBrush = SolidColor(accent),
    )
}

@Composable
internal fun Primary(label: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = 44.dp).clip(RoundedCornerShape(10.dp))
            .background(accent).clickable(role = Role.Button, onClick = onClick).padding(9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Label(label, weight = 720, color = Color.White, align = TextAlign.Center)
    }
}

@Composable
internal fun Footer(label: String, hint: String = "", onClick: () -> Unit) {
    Rule()
    Column(Modifier.fillMaxWidth().background(surface).padding(horizontal = gutter, vertical = 10.dp)) {
        Primary(label, onClick)
        if (hint.isNotEmpty()) {
            Label(
                hint,
                Modifier.fillMaxWidth().padding(top = 4.dp),
                size = 12,
                color = muted,
                align = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun TextAction(label: String, onClick: () -> Unit) {
    Box(
        Modifier.heightIn(min = 44.dp).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        Label(label, weight = 700, color = accent)
    }
}

@Composable
internal fun Status(message: String) {
    Label(message, Modifier.padding(top = 8.dp), size = 13, color = accent)
}
