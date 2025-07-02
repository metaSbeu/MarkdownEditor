package com.sbeu.markdowneditor

import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.sbeu.markdowneditor.databinding.ActivityUploadBinding

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private lateinit var viewModel: UploadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()

        viewModel = ViewModelProvider(this)[UploadViewModel::class.java]

        val pickMarkdownFileLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            uri?.let {
                viewModel.loadMarkdownFromUri(it)
            }
        }

        binding.buttonLoadFromStorage.setOnClickListener {
            pickMarkdownFileLauncher.launch(arrayOf("text/markdown", "text/plain"))
        }

        binding.buttonLoadInternet.setOnClickListener {
            val url = binding.editTextUrl.text.toString()
            viewModel.loadMarkdownFromUrl(url)
        }

        viewModel.markdownContent.observe(this) { text ->
            binding.textViewContent.text = text
        }

        binding.buttonEdit.setOnClickListener {
            viewModel.markdownContent.value?.let {
                val intent = EditActivity.newIntent(this, it)
                startActivity(intent)
            }
        }

        binding.buttonView.setOnClickListener {
            viewModel.markdownContent.value?.let {
                val intent = ViewMarkdownActivity.newIntent(this, it)
                startActivity(intent)
            }
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
