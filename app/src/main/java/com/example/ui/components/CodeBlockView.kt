package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RadiantAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CodeBlockView(
    code: String,
    language: String = "kotlin",
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    val cleanLanguage = language.ifBlank { "code" }.lowercase()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF070B14))
    ) {
        // Header bar with language badge and Copy button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF101726))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cleanLanguage.uppercase(),
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(code))
                    isCopied = true
                    Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                    scope.launch {
                        delay(2000)
                        isCopied = false
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = if (isCopied) NeonEmerald else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Code content with line numbers and syntax styling
        val lines = remember(code) { code.lines() }
        val horizontalScrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp)
        ) {
            // Line numbers column
            Column(
                modifier = Modifier.padding(end = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                lines.indices.forEach { index ->
                    Text(
                        text = "${index + 1}",
                        color = TextMuted.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp
                    )
                }
            }

            // Code text
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                Text(
                    text = highlightCode(code, cleanLanguage),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

fun highlightCode(rawCode: String, language: String): AnnotatedString {
    val keywords = setOf(
        "val", "var", "fun", "class", "interface", "object", "return", "if", "else", "when",
        "for", "while", "import", "package", "override", "suspend", "data", "sealed", "private",
        "public", "protected", "internal", "const", "def", "async", "await", "function", "let",
        "const", "type", "struct", "impl", "fn", "match", "mut", "pub", "SELECT", "FROM", "WHERE",
        "INSERT", "UPDATE", "DELETE", "JOIN"
    )

    return buildAnnotatedString {
        val lines = rawCode.lines()
        lines.forEachIndexed { index, line ->
            if (line.trim().startsWith("//") || line.trim().startsWith("#")) {
                pushStyle(SpanStyle(color = TextMuted.copy(alpha = 0.7f)))
                append(line)
                pop()
            } else {
                val tokens = line.split(Regex("(?<=\\s)|(?=\\s)|(?<=[(),;{}])|(?=[(),;{}])"))
                tokens.forEach { token ->
                    when {
                        token in keywords -> {
                            pushStyle(SpanStyle(color = NeonCyan, fontWeight = FontWeight.Bold))
                            append(token)
                            pop()
                        }
                        token.startsWith("\"") || token.endsWith("\"") || token.startsWith("'") -> {
                            pushStyle(SpanStyle(color = NeonEmerald))
                            append(token)
                            pop()
                        }
                        token.toIntOrNull() != null || token.toDoubleOrNull() != null -> {
                            pushStyle(SpanStyle(color = RadiantAmber))
                            append(token)
                            pop()
                        }
                        token.startsWith("@") -> {
                            pushStyle(SpanStyle(color = Color(0xFFFF758C)))
                            append(token)
                            pop()
                        }
                        else -> {
                            pushStyle(SpanStyle(color = TextPrimary))
                            append(token)
                            pop()
                        }
                    }
                }
            }
            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}
