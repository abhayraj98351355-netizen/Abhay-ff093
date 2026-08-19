package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

sealed class MarkdownElement {
    data class Paragraph(val text: String) : MarkdownElement()
    data class CodeBlock(val code: String, val language: String) : MarkdownElement()
    data class Heading(val text: String, val level: Int) : MarkdownElement()
    data class BulletItem(val text: String) : MarkdownElement()
    data class Blockquote(val text: String) : MarkdownElement()
}

@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier,
    isUser: Boolean = false
) {
    val elements = remember(content) { parseMarkdown(content) }

    Column(modifier = modifier.fillMaxWidth()) {
        elements.forEachIndexed { index, element ->
            when (element) {
                is MarkdownElement.CodeBlock -> {
                    CodeBlockView(
                        code = element.code,
                        language = element.language,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                is MarkdownElement.Heading -> {
                    val fontSize = when (element.level) {
                        1 -> 19.sp
                        2 -> 17.sp
                        else -> 15.sp
                    }
                    Text(
                        text = buildFormattedText(element.text, isUser),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) Color.White else NeonCyan,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                is MarkdownElement.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp, end = 8.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isUser) Color.White else NeonCyan)
                        )
                        Text(
                            text = buildFormattedText(element.text, isUser),
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = if (isUser) Color.White else TextPrimary
                        )
                    }
                }
                is MarkdownElement.Blockquote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .background(NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buildFormattedText(element.text, isUser),
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = TextSecondary
                        )
                    }
                }
                is MarkdownElement.Paragraph -> {
                    if (element.text.isNotBlank()) {
                        Text(
                            text = buildFormattedText(element.text, isUser),
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = if (isUser) Color.White else TextPrimary,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

fun parseMarkdown(text: String): List<MarkdownElement> {
    val elements = mutableListOf<MarkdownElement>()
    val lines = text.lines()
    var index = 0

    while (index < lines.size) {
        val line = lines[index]
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            val language = trimmed.removePrefix("```").trim()
            val codeBuilder = StringBuilder()
            index++
            while (index < lines.size && !lines[index].trim().startsWith("```")) {
                codeBuilder.append(lines[index]).append("\n")
                index++
            }
            elements.add(MarkdownElement.CodeBlock(codeBuilder.toString().trimEnd(), language))
            index++
            continue
        }

        if (trimmed.startsWith("### ")) {
            elements.add(MarkdownElement.Heading(trimmed.removePrefix("### "), 3))
            index++
            continue
        }
        if (trimmed.startsWith("## ")) {
            elements.add(MarkdownElement.Heading(trimmed.removePrefix("## "), 2))
            index++
            continue
        }
        if (trimmed.startsWith("# ")) {
            elements.add(MarkdownElement.Heading(trimmed.removePrefix("# "), 1))
            index++
            continue
        }

        if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            elements.add(MarkdownElement.BulletItem(trimmed.substring(2)))
            index++
            continue
        }

        if (trimmed.startsWith("> ")) {
            elements.add(MarkdownElement.Blockquote(trimmed.removePrefix("> ")))
            index++
            continue
        }

        if (trimmed.isNotBlank()) {
            elements.add(MarkdownElement.Paragraph(line))
        }
        index++
    }

    return elements
}

fun buildFormattedText(raw: String, isUser: Boolean): AnnotatedString {
    return buildAnnotatedString {
        var current = raw

        // Regex for bold **text** and inline code `code`
        val pattern = Regex("(\\*\\*.*?\\*\\*|`.*?`)")
        val matches = pattern.findAll(current).toList()

        var lastIdx = 0
        for (match in matches) {
            val matchRange = match.range
            if (matchRange.first > lastIdx) {
                append(current.substring(lastIdx, matchRange.first))
            }
            val matchValue = match.value
            if (matchValue.startsWith("**") && matchValue.endsWith("**")) {
                val boldText = matchValue.substring(2, matchValue.length - 2)
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = if (isUser) Color.White else TextPrimary))
                append(boldText)
                pop()
            } else if (matchValue.startsWith("`") && matchValue.endsWith("`")) {
                val codeText = matchValue.substring(1, matchValue.length - 1)
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0x334FACFE),
                        color = NeonCyan,
                        fontSize = 13.sp
                    )
                )
                append(" $codeText ")
                pop()
            }
            lastIdx = matchRange.last + 1
        }
        if (lastIdx < current.length) {
            append(current.substring(lastIdx))
        }
    }
}
