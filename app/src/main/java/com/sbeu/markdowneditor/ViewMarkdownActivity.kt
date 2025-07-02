package com.sbeu.markdowneditor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sbeu.markdowneditor.databinding.ActivityViewMarkdownBinding

class ViewMarkdownActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewMarkdownBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewMarkdownBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()


    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}