package com.sbeu.markdowneditor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.sbeu.markdowneditor.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private lateinit var viewModel: EditViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()

        viewModel = ViewModelProvider(this)[EditViewModel::class.java]
        binding.markdownEditField.setText(viewModel.markdownContent.value ?: "")

        binding.buttonSave.setOnClickListener {
            viewModel.updateMarkdownContent(binding.markdownEditField.text.toString())
            viewModel.saveMarkdownToCurrentFile()
            startActivity(
                ViewMarkdownActivity.newIntent(
                    this, binding.markdownEditField.text.toString()
                )
            )
            finish()
        }
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
            Intent(context, EditActivity::class.java).apply {
                putExtra(EXTRA_CONTENT, content)
            }
    }
}