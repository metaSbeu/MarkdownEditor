package com.sbeu.markdowneditor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.sbeu.markdowneditor.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private lateinit var viewModel: UploadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()

        viewModel = ViewModelProvider(this)[UploadViewModel::class.java]

        binding.markdownEditField.setText(intent.getStringExtra(EXTRA_MARKDOWN_CONTENT))

        binding.buttonSave.setOnClickListener {
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
        const val EXTRA_MARKDOWN_CONTENT = "markdown_content"

        fun newIntent(context: Context, markdownContent: String): Intent {
            return Intent(context, EditActivity::class.java).apply {
                putExtra(EXTRA_MARKDOWN_CONTENT, markdownContent)
            }
        }
    }
}