package com.sbeu.markdowneditor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.sbeu.markdowneditor.databinding.ActivityViewMarkdownBinding

class ViewMarkdownActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewMarkdownBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMarkdownBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()

        val content = intent.getStringExtra(EXTRA_CONTENT) ?: ""
        val elements = MarkdownParser.parse(content)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = MarkdownAdapter(elements)
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        private const val EXTRA_CONTENT = "EXTRA_CONTENT"
        fun newIntent(context: Context, content: String) =
            Intent(context, ViewMarkdownActivity::class.java).apply {
                putExtra(EXTRA_CONTENT, content)
            }
    }
}