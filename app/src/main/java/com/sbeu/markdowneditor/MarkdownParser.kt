package com.sbeu.markdowneditor

object MarkdownParser {

    fun parse(markdown: String): List<MarkdownElement> {
        val lines = markdown.lines()
        val elements = mutableListOf<MarkdownElement>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            when {
                // Headers
                line.startsWith("#") -> {
                    val level = line.takeWhile { it == '#' }.length
                    val text = line.drop(level).trim()
                    elements.add(MarkdownElement.Header(level, parseInline(text)))
                    if (level == 1 || level == 2) {
                        elements.add(MarkdownElement.Divider)
                    }
                }
                // Images
                line.startsWith("![") && line.contains("](") && line.endsWith(")") -> {
                    val url = line.substringAfter("](").substringBeforeLast(")")
                    elements.add(MarkdownElement.Image(url))
                }
                // Tables
                "|" in line && line.startsWith("|") -> {
                    val rawHeaders = line.split("|").map { it.trim() }
                    val headers = if (rawHeaders.size > 1 && rawHeaders.first().isEmpty() && rawHeaders.last().isEmpty()) {
                        rawHeaders.subList(1, rawHeaders.size - 1)
                    } else if (rawHeaders.first().isEmpty()) {
                        rawHeaders.subList(1, rawHeaders.size)
                    } else if (rawHeaders.last().isEmpty()) {
                        rawHeaders.subList(0, rawHeaders.size - 1)
                    } else {
                        rawHeaders
                    }

                    val rows = mutableListOf<List<String>>()
                    i += 2
                    while (i < lines.size && lines[i].trim().startsWith("|")) {
                        val rawRow = lines[i].split("|").map { it.trim() }
                        val row = if (rawRow.size > 1 && rawRow.first().isEmpty() && rawRow.last().isEmpty()) {
                            rawRow.subList(1, rawRow.size - 1)
                        } else if (rawRow.first().isEmpty()) {
                            rawRow.subList(1, rawRow.size)
                        } else if (rawRow.last().isEmpty()) {
                            rawRow.subList(0, rawRow.size - 1)
                        } else {
                            rawRow
                        }
                        rows.add(row)
                        i++
                    }
                    elements.add(MarkdownElement.Table(headers, rows))
                    continue
                }
                // Paragraphs
                line.isNotBlank() -> {
                    elements.add(MarkdownElement.Paragraph(parseInline(line)))
                }
            }
            i++
        }
        return elements
    }

    private fun parseInline(text: String): List<InlineElement> {
        val inlineElements = mutableListOf<InlineElement>()
        var currentIndex = 0

        val regex = Regex(
            "(\\*\\*([^\\s*](?:.*?[^\\s*])?)\\*\\*)" +
                    "|(\\*((?!\\*)[^\\s*](?:.*?[^\\s*])?)\\*)" +
                    "|(~~(.*?)~~)"
        )

        regex.findAll(text).forEach { matchResult ->
            val matchRange = matchResult.range
            val matchedText = matchResult.value

            // Добавляем обычный текст перед найденным markdown-элементом
            if (matchRange.first > currentIndex) {
                inlineElements.add(InlineElement.Text(text.substring(currentIndex, matchRange.first)))
            }

            // Определяем тип найденного markdown-элемента
            when {
                // Жирный текст: Проверяем, что первая группа (весь матч жирного) не null
                matchResult.groups[1] != null -> {
                    val content = matchResult.groups[2]!!.value // Содержимое находится во второй группе
                    inlineElements.add(InlineElement.Bold(content))
                }
                // Курсив: Проверяем, что третья группа (весь матч курсива) не null
                matchResult.groups[3] != null -> {
                    val content = matchResult.groups[4]!!.value // Содержимое находится в четвертой группе
                    inlineElements.add(InlineElement.Italic(content))
                }
                // Зачеркнутый текст: Проверяем, что пятая группа (весь матч зачеркнутого) не null
                matchResult.groups[5] != null -> {
                    val content = matchResult.groups[6]!!.value // Содержимое находится в шестой группе
                    inlineElements.add(InlineElement.Strikethrough(content))
                }
            }
            currentIndex = matchRange.last + 1
        }

        // Добавляем оставшийся обычный текст после последнего найденного markdown-элемента
        if (currentIndex < text.length) {
            inlineElements.add(InlineElement.Text(text.substring(currentIndex)))
        }

        // Если не найдено никаких markdown-элементов, весь текст - это обычный текст
        if (inlineElements.isEmpty() && text.isNotEmpty()) {
            inlineElements.add(InlineElement.Text(text))
        }

        return inlineElements
    }
}