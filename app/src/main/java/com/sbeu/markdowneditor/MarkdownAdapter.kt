package com.sbeu.markdowneditor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors // Для выполнения задач в фоновом потоке
import android.os.Handler // Для обновления UI в основном потоке
import android.os.Looper // Для получения Looper'а основного потока

class MarkdownAdapter(private val elements: List<MarkdownElement>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_PARAGRAPH = 1
        const val VIEW_TYPE_IMAGE = 2
        const val VIEW_TYPE_TABLE = 3
        const val VIEW_TYPE_DIVIDER = 4
    }

    override fun getItemViewType(position: Int): Int = when (elements[position]) {
        is MarkdownElement.Header -> VIEW_TYPE_HEADER
        is MarkdownElement.Paragraph -> VIEW_TYPE_PARAGRAPH
        is MarkdownElement.Image -> VIEW_TYPE_IMAGE
        is MarkdownElement.Table -> VIEW_TYPE_TABLE
        MarkdownElement.Divider -> VIEW_TYPE_DIVIDER
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val paddingLarge = context.resources.getDimensionPixelSize(R.dimen.padding_large)
        val paddingMedium = context.resources.getDimensionPixelSize(R.dimen.padding_medium)
        val paddingSmall = context.resources.getDimensionPixelSize(R.dimen.padding_small)

        return when (viewType) {
            VIEW_TYPE_HEADER -> TextViewHolder(TextView(context).apply {
                setPadding(paddingLarge, paddingMedium, paddingLarge, paddingSmall)
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            })

            VIEW_TYPE_PARAGRAPH -> TextViewHolder(TextView(context).apply {
                setPadding(paddingLarge, paddingSmall, paddingLarge, paddingSmall)
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            })

            VIEW_TYPE_IMAGE -> ImageViewHolder(ImageView(context).apply {
                setPadding(paddingLarge, paddingMedium, paddingLarge, paddingMedium)
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
            })

            VIEW_TYPE_TABLE -> TableViewHolder(TableLayout(context).apply {
                setPadding(paddingLarge, paddingMedium, paddingLarge, paddingMedium)
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                isStretchAllColumns = true
            })

            VIEW_TYPE_DIVIDER -> DividerViewHolder(View(context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(context, 1f)
                )
                setBackgroundColor(ContextCompat.getColor(context, R.color.black))
                setPadding(paddingLarge, paddingMedium, paddingLarge, paddingMedium)
            })

            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val element = elements[position]
        when (holder) {
            is TextViewHolder -> {
                val tv = holder.textView
                tv.setTypeface(null, Typeface.NORMAL)
                tv.paintFlags = tv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tv.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    tv.context.resources.getDimension(R.dimen.text_size_paragraph)
                )

                when (element) {
                    is MarkdownElement.Header -> {
                        tv.setTextSize(
                            TypedValue.COMPLEX_UNIT_PX, tv.context.resources.getDimension(
                                when (element.level) {
                                    1 -> R.dimen.text_size_h1
                                    2 -> R.dimen.text_size_h2
                                    3 -> R.dimen.text_size_h3
                                    4 -> R.dimen.text_size_h4
                                    5 -> R.dimen.text_size_h5
                                    6 -> R.dimen.text_size_h6
                                    else -> R.dimen.text_size_h1
                                }
                            )
                        )
                        tv.text = formatInlineContent(element.content, tv.context)
                        tv.setTypeface(null, Typeface.BOLD)
                    }

                    is MarkdownElement.Paragraph -> {
                        tv.text = formatInlineContent(element.content, tv.context)
                    }

                    is MarkdownElement.Image,
                    is MarkdownElement.Table,
                    MarkdownElement.Divider,
                        -> {
                    }
                }
            }

            is ImageViewHolder -> {
                if (element is MarkdownElement.Image) {
                    holder.imageView.setImageDrawable(null)
                    LoadImageHelper.load(holder.imageView, element.url)
                }
            }

            is TableViewHolder -> {
                if (element is MarkdownElement.Table) {
                    holder.bind(element)
                }
            }
        }
    }

    private fun formatInlineContent(content: List<InlineElement>, context: Context): CharSequence {
        val spannableStringBuilder = SpannableStringBuilder()
        content.forEach { inlineElement ->
            when (inlineElement) {
                is InlineElement.Text -> {
                    spannableStringBuilder.append(inlineElement.text)
                }

                is InlineElement.Bold -> {
                    val start = spannableStringBuilder.length
                    spannableStringBuilder.append(inlineElement.text)
                    spannableStringBuilder.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        spannableStringBuilder.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                is InlineElement.Italic -> {
                    val start = spannableStringBuilder.length
                    spannableStringBuilder.append(inlineElement.text)
                    spannableStringBuilder.setSpan(
                        StyleSpan(Typeface.ITALIC),
                        start,
                        spannableStringBuilder.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                is InlineElement.Strikethrough -> {
                    val start = spannableStringBuilder.length
                    spannableStringBuilder.append(inlineElement.text)
                    spannableStringBuilder.setSpan(
                        StrikethroughSpan(),
                        start,
                        spannableStringBuilder.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        return spannableStringBuilder
    }

    override fun getItemCount(): Int = elements.size

    class TextViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
    class TableViewHolder(val table: TableLayout) : RecyclerView.ViewHolder(table) {
        fun bind(tableElement: MarkdownElement.Table) {
            table.removeAllViews()
            val ctx = table.context
            val paddingSmall = ctx.resources.getDimensionPixelSize(R.dimen.padding_small)
            val paddingMedium = ctx.resources.getDimensionPixelSize(R.dimen.padding_medium)

            // Header Row
            val headerRow = TableRow(ctx).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT
                )
            }
            tableElement.headers.forEach { headerText ->
                val tv = TextView(ctx).apply {
                    text = headerText
                    setTypeface(null, Typeface.BOLD)
                    setPadding(paddingMedium, paddingSmall, paddingMedium, paddingSmall)
                    setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        ctx.resources.getDimension(R.dimen.text_size_paragraph)
                    )
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f
                    )
                }
                headerRow.addView(tv)
            }
            table.addView(headerRow)

            // Data Rows
            tableElement.rows.forEach { row ->
                val tr = TableRow(ctx).apply {
                    layoutParams = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                row.forEach { cellText ->
                    val tv = TextView(ctx).apply {
                        text = cellText
                        setPadding(paddingMedium, paddingSmall, paddingMedium, paddingSmall)
                        setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            ctx.resources.getDimension(R.dimen.text_size_paragraph)
                        )
                        layoutParams = TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT,
                            1.0f
                        )
                    }
                    tr.addView(tv)
                }
                table.addView(tr)
            }
        }
    }

    class DividerViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    private fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics
        ).toInt()
    }
}