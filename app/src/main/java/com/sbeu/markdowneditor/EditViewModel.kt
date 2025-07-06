package com.sbeu.markdowneditor

import android.app.Application
import android.content.ContentResolver
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditViewModel(application: Application) : AndroidViewModel(application) {

    val markdownContent = MarkdownRepository.markdownContent
    val currentFileUri = MarkdownRepository.currentFileUri

    private val contentResolver: ContentResolver = application.contentResolver

    fun updateMarkdownContent(text: String) {
        markdownContent.value = text
    }

    fun saveMarkdownToCurrentFile() {
        val uri = currentFileUri.value ?: return
        val content = markdownContent.value ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            contentResolver.openOutputStream(uri, "wt")?.use { it.write(content.toByteArray()) }
        }
    }
}