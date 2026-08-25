package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSuccess
import com.example.ui.theme.SmaliClass
import com.example.ui.theme.SmaliComment
import com.example.ui.theme.SmaliField
import com.example.ui.theme.SmaliKeyword
import com.example.ui.theme.SmaliLineNumber
import com.example.ui.theme.SmaliRegister
import com.example.ui.theme.SmaliValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SmaliCodeCard(
    title: String,
    subtitle: String,
    code: String,
    isPatched: Boolean = false,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (isPatched) CyberSuccess.copy(alpha = 0.5f) else CyberBorder, RoundedCornerShape(16.dp)),
        color = CyberCardBg,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isPatched) CyberSuccess else CyberPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        copied = true
                        Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            delay(2000)
                            copied = false
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = if (copied) CyberSuccess else CyberPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Code container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070A12))
                    .border(1.dp, Color(0xFF1E2638), RoundedCornerShape(12.dp))
                    .padding(14.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = formatSmaliSyntax(code),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

fun formatSmaliSyntax(rawCode: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = rawCode.lines()
        lines.forEachIndexed { index, line ->
            // Line numbers
            withStyle(SpanStyle(color = SmaliLineNumber)) {
                append(String.format("%2d  ", index + 1))
            }

            val trimmed = line.trimStart()
            when {
                trimmed.startsWith("#") -> {
                    withStyle(SpanStyle(color = SmaliComment)) {
                        append(line)
                    }
                }
                trimmed.startsWith(".method") || trimmed.startsWith(".end method") ||
                trimmed.startsWith(".registers") || trimmed.startsWith(".line") ||
                trimmed.startsWith("return-void") || trimmed.startsWith("const") -> {
                    withStyle(SpanStyle(color = SmaliKeyword, fontWeight = FontWeight.SemiBold)) {
                        append(line)
                    }
                }
                trimmed.startsWith("sget") || trimmed.startsWith("sput") || trimmed.startsWith("xor") -> {
                    val parts = line.split(" ")
                    if (parts.isNotEmpty()) {
                        withStyle(SpanStyle(color = SmaliKeyword, fontWeight = FontWeight.Bold)) {
                            append(parts[0])
                        }
                        append(" ")
                        val remainder = parts.drop(1).joinToString(" ")
                        formatLineTokens(remainder, this)
                    } else {
                        append(line)
                    }
                }
                else -> {
                    formatLineTokens(line, this)
                }
            }
            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}

private fun formatLineTokens(text: String, builder: AnnotatedString.Builder) {
    var cursor = 0
    val len = text.length

    while (cursor < len) {
        val nextComma = text.indexOf(',', cursor)
        val nextSemi = text.indexOf(';', cursor)
        val nextArrow = text.indexOf("->", cursor)

        when {
            text.startsWith("v0", cursor) || text.startsWith("v1", cursor) || text.startsWith("p0", cursor) -> {
                builder.withStyle(SpanStyle(color = SmaliRegister, fontWeight = FontWeight.Bold)) {
                    append(text.substring(cursor, cursor + 2))
                }
                cursor += 2
            }
            text.startsWith("Lcom/miui/", cursor) -> {
                val end = if (nextSemi != -1) nextSemi + 1 else len
                builder.withStyle(SpanStyle(color = SmaliClass)) {
                    append(text.substring(cursor, end))
                }
                cursor = end
            }
            text.startsWith("USE_WHITE_LISTS:Z", cursor) -> {
                builder.withStyle(SpanStyle(color = CyberSuccess, fontWeight = FontWeight.Bold)) {
                    append("USE_WHITE_LISTS:Z")
                }
                cursor += "USE_WHITE_LISTS:Z".length
            }
            text.startsWith("IS_INTERNATIONAL_BUILD:Z", cursor) -> {
                builder.withStyle(SpanStyle(color = SmaliField)) {
                    append("IS_INTERNATIONAL_BUILD:Z")
                }
                cursor += "IS_INTERNATIONAL_BUILD:Z".length
            }
            text.startsWith("0x1", cursor) || text.startsWith("0x0", cursor) -> {
                builder.withStyle(SpanStyle(color = SmaliValue, fontWeight = FontWeight.Bold)) {
                    append(text.substring(cursor, cursor + 3))
                }
                cursor += 3
            }
            else -> {
                builder.append(text[cursor])
                cursor++
            }
        }
    }
}
