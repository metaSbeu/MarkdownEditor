package com.sbeu.markdowneditor

import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownParserTest {

    @Test
    fun `parse handles H1 header`() {
        val markdown = "# Hello World"
        val expected = listOf(
            MarkdownElement.Header(1, listOf(InlineElement.Text("Hello World"))),
            MarkdownElement.Divider
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles H2 header`() {
        val markdown = "## Another Header"
        val expected = listOf(
            MarkdownElement.Header(2, listOf(InlineElement.Text("Another Header"))),
            MarkdownElement.Divider
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles H6 header`() {
        val markdown = "###### Small Header"
        val expected = listOf(
            MarkdownElement.Header(6, listOf(InlineElement.Text("Small Header")))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles header with leading trailing spaces`() {
        val markdown = "#    Header with Spaces   "
        val expected = listOf(
            MarkdownElement.Header(1, listOf(InlineElement.Text("Header with Spaces"))),
            MarkdownElement.Divider
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles H1 header with divider`() {
        val markdown = "# Title"
        val expected = listOf(
            MarkdownElement.Header(1, listOf(InlineElement.Text("Title"))),
            MarkdownElement.Divider
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles H2 header with divider`() {
        val markdown = "## Subtitle"
        val expected = listOf(
            MarkdownElement.Header(2, listOf(InlineElement.Text("Subtitle"))),
            MarkdownElement.Divider
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles H3 header without divider`() {
        val markdown = "### Section"
        val expected = listOf(
            MarkdownElement.Header(3, listOf(InlineElement.Text("Section")))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles a simple paragraph`() {
        val markdown = "This is a plain paragraph."
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(InlineElement.Text("This is a plain paragraph.")))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles bold text in a paragraph`() {
        val markdown = "This is **bold** text."
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(
                InlineElement.Text("This is "),
                InlineElement.Bold("bold"),
                InlineElement.Text(" text.")
            ))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles italic text in a paragraph`() {
        val markdown = "This is *italic* text."
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(
                InlineElement.Text("This is "),
                InlineElement.Italic("italic"),
                InlineElement.Text(" text.")
            ))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles strikethrough text in a paragraph`() {
        val markdown = "This is ~~strikethrough~~ text."
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(
                InlineElement.Text("This is "),
                InlineElement.Strikethrough("strikethrough"),
                InlineElement.Text(" text.")
            ))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles multiple inline formats in a paragraph`() {
        val markdown = "This has **bold**, *italic*, and ~~strikethrough~~."
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(
                InlineElement.Text("This has "),
                InlineElement.Bold("bold"),
                InlineElement.Text(", "),
                InlineElement.Italic("italic"),
                InlineElement.Text(", and "),
                InlineElement.Strikethrough("strikethrough"),
                InlineElement.Text(".")
            ))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles mixed inline formats with no spaces`() {
        val markdown = "**Bold***Italic*~~Strike~~"
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(
                InlineElement.Bold("Bold"),
                InlineElement.Italic("Italic"),
                InlineElement.Strikethrough("Strike")
            ))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles inline formats at start and end of paragraph`() {
        val markdown = "**Start** and *end*."
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(
                InlineElement.Bold("Start"),
                InlineElement.Text(" and "),
                InlineElement.Italic("end"),
                InlineElement.Text(".")
            ))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles bold in a header`() {
        val markdown = "# Header with **bold** text"
        val expected = listOf(
            MarkdownElement.Header(1, listOf(
                InlineElement.Text("Header with "),
                InlineElement.Bold("bold"),
                InlineElement.Text(" text")
            )),
            MarkdownElement.Divider
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles a simple image`() {
        val markdown = "![Alt text](http://example.com/image.png)"
        val expected = listOf(
            MarkdownElement.Image("http://example.com/image.png")
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles image with no alt text`() {
        val markdown = "![](http://example.com/noalt.jpg)"
        val expected = listOf(
            MarkdownElement.Image("http://example.com/noalt.jpg")
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles image with complex URL`() {
        val markdown = "![Complex Image](https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png?query=test)"
        val expected = listOf(
            MarkdownElement.Image("https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png?query=test")
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles a simple table`() {
        val markdown = """
            | Header1 | Header2 |
            |---------|---------|
            | Data1   | Data2   |
            | Data3   | Data4   |
        """.trimIndent()
        val expected = listOf(
            MarkdownElement.Table(
                headers = listOf("Header1", "Header2"),
                rows = listOf(
                    listOf("Data1", "Data2"),
                    listOf("Data3", "Data4")
                )
            )
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles table with leading and trailing pipes`() {
        val markdown = """
            | Header A | Header B | Header C |
            |:---|:---:|---:|
            | Cell 1A | Cell 1B | Cell 1C |
            | Cell 2A | Cell 2B | Cell 2C |
        """.trimIndent()
        val expected = listOf(
            MarkdownElement.Table(
                headers = listOf("Header A", "Header B", "Header C"),
                rows = listOf(
                    listOf("Cell 1A", "Cell 1B", "Cell 1C"),
                    listOf("Cell 2A", "Cell 2B", "Cell 2C")
                )
            )
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles table with empty cells`() {
        val markdown = """
            | H1 | H2 | H3 |
            |----|----|----|
            | A  |    | C  |
            |    | D  |    |
        """.trimIndent()
        val expected = listOf(
            MarkdownElement.Table(
                headers = listOf("H1", "H2", "H3"),
                rows = listOf(
                    listOf("A", "", "C"),
                    listOf("", "D", "")
                )
            )
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles table with uneven number of cells in rows`() {
        val markdown = """
            | A | B | C |
            |---|---|---|
            | 1 | 2 |
            | 4 | 5 | 6 | 7 |
        """.trimIndent()
        val expected = listOf(
            MarkdownElement.Table(
                headers = listOf("A", "B", "C"),
                rows = listOf(
                    listOf("1", "2"),
                    listOf("4", "5", "6", "7")
                )
            )
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles multiple elements in sequence`() {
        val markdown = """
            # Top Title
            This is a **paragraph** with *inline* text.
            ![Image](http://example.com/img.png)
            ## Sub-section
            | H1 | H2 |
            |----|----|
            | a  | b  |
            A final sentence.
        """.trimIndent()

        val expected = listOf(
            MarkdownElement.Header(1, listOf(InlineElement.Text("Top Title"))),
            MarkdownElement.Divider,
            MarkdownElement.Paragraph(listOf(
                InlineElement.Text("This is a "),
                InlineElement.Bold("paragraph"),
                InlineElement.Text(" with "),
                InlineElement.Italic("inline"),
                InlineElement.Text(" text.")
            )),
            MarkdownElement.Image("http://example.com/img.png"),
            MarkdownElement.Header(2, listOf(InlineElement.Text("Sub-section"))),
            MarkdownElement.Divider,
            MarkdownElement.Table(
                headers = listOf("H1", "H2"),
                rows = listOf(listOf("a", "b"))
            ),
            MarkdownElement.Paragraph(listOf(InlineElement.Text("A final sentence.")))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles empty string`() {
        val markdown = ""
        val expected = emptyList<MarkdownElement>()
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles string with only spaces`() {
        val markdown = "   \n   \n"
        val expected = emptyList<MarkdownElement>()
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles paragraph with only bold`() {
        val markdown = "**Important**"
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(InlineElement.Bold("Important")))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles multiple bold sections`() {
        val markdown = "**First** and **Second**"
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(
                InlineElement.Bold("First"),
                InlineElement.Text(" and "),
                InlineElement.Bold("Second")
            ))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles multiple italic sections`() {
        val markdown = "*One* and *Two*"
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(
                InlineElement.Italic("One"),
                InlineElement.Text(" and "),
                InlineElement.Italic("Two")
            ))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles multiple strikethrough sections`() {
        val markdown = "~~Gone~~ and ~~Done~~"
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(
                InlineElement.Strikethrough("Gone"),
                InlineElement.Text(" and "),
                InlineElement.Strikethrough("Done")
            ))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles inline formats within other inline formats (should not happen with current simple regex)`() {
        val markdown = "**bold *and* italic**"
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(
                InlineElement.Bold("bold *and* italic")
            ))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles raw text with markdown special characters but no closing tag`() {
        val markdown = "This has unmatched **bold or *italic"
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(
                InlineElement.Text("This has unmatched **bold or *italic")
            ))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }

    @Test
    fun `parse handles markdown-like text that is not actual markdown`() {
        val markdown = "Asterisks * not for italic. Double ** not for bold."
        val expected = listOf(
            MarkdownElement.Paragraph(listOf(
                InlineElement.Text("Asterisks * not for italic. Double ** not for bold.")
            ))
        )
        assertEquals(expected, MarkdownParser.parse(markdown))
    }
}