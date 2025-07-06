package com.sbeu.markdowneditor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sbeu.markdowneditor.databinding.ActivityUploadBinding


class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private lateinit var viewModel: UploadViewModel

    private val REQUEST_WRITE_EXTERNAL_STORAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()

        viewModel = ViewModelProvider(this)[UploadViewModel::class.java]

        val fileLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    viewModel.loadMarkdownFromUri(it)
                }
            }

        binding.buttonLoadFromStorage.setOnClickListener {
            fileLauncher.launch(arrayOf("text/markdown", "text/plain"))
        }

        binding.buttonLoadInternet.setOnClickListener {
            checkAndRequestPermissionsForDownload()
        }

        binding.buttonView.setOnClickListener {
            startActivity(
                ViewMarkdownActivity.newIntent(
                    this, viewModel.markdownContent.value ?: ""
                )
            )
        }

        binding.buttonEdit.setOnClickListener {
            startActivity(EditActivity.newIntent(this, viewModel.markdownContent.value ?: ""))
        }

        viewModel.markdownContent.observe(this) {
            binding.textViewContent.text = it
            binding.rvPreview.layoutManager = LinearLayoutManager(this)
            binding.rvPreview.adapter = MarkdownAdapter(MarkdownParser.parse(it))
        }

        setupToggleGroup()
    }

    private fun checkAndRequestPermissionsForDownload() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_EXTERNAL_STORAGE
                )
            } else {
                viewModel.downloadMarkdownFileToDownloads(this, binding.editTextUrl.text.toString())
            }
        } else {
            viewModel.downloadMarkdownFileToDownloads(this, binding.editTextUrl.text.toString())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено, можно начинать загрузку
                viewModel.downloadMarkdownFileToDownloads(this, binding.editTextUrl.text.toString())
            } else {
                 Toast.makeText(this, "Для сохранения файла требуется разрешение на хранилище", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupToggleGroup() {
        val toggleGroup = binding.toggleGroup

        toggleGroup.check(R.id.toggleButtonPreview)
        binding.rvPreview.visibility = View.VISIBLE
        binding.textViewContent.visibility = View.GONE

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.toggleButtonPreview -> {
                        binding.rvPreview.visibility = View.VISIBLE
                        binding.textViewContent.visibility = View.GONE
                    }

                    R.id.toggleButtonEdit -> {
                        binding.rvPreview.visibility = View.GONE
                        binding.textViewContent.visibility = View.VISIBLE
                    }
                }
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