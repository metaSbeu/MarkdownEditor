package com.sbeu.markdowneditor

sealed class MarkdownElement {
    data class Header(val level: Int, val content: List<InlineElement>) : MarkdownElement()
    data class Paragraph(val content: List<InlineElement>) : MarkdownElement()
    data class Image(val url: String) : MarkdownElement()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownElement()
    object Divider : MarkdownElement()
}

sealed class InlineElement {
    data class Text(val text: String) : InlineElement()
    data class Bold(val text: String) : InlineElement()
    data class Italic(val text: String) : InlineElement()
    data class Strikethrough(val text: String) : InlineElement()
}