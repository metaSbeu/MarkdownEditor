package com.sbeu.markdowneditor

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UploadViewModel(application: Application) : AndroidViewModel(application) {
    val markdownContent = MarkdownRepository.markdownContent
    val currentFileUri = MarkdownRepository.currentFileUri

    private val contentResolver: ContentResolver = application.contentResolver

    fun loadMarkdownFromUri(uri: Uri) {
        currentFileUri.value = uri
        CoroutineScope(Dispatchers.IO).launch {
            val text =
                contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
            markdownContent.postValue(text)
        }
    }

    fun downloadMarkdownFileToDownloads(context: Context, urlString: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connect()

                val inputStream = conn.inputStream

                val fileName = url.path.substringAfterLast('/').ifEmpty { "downloaded.md" }

                var outputStream: FileOutputStream? = null
                var fileUri: Uri? = null

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "text/markdown")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }

                        fileUri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                        fileUri?.let { uri ->
                            outputStream = contentResolver.openOutputStream(uri) as FileOutputStream?
                        }

                        if (outputStream == null) {
                            throw Exception("Не удалось получить OutputStream для MediaStore URI.")
                        }

                    } else {
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        if (!downloadsDir.exists()) downloadsDir.mkdirs()
                        val file = File(downloadsDir, fileName)
                        outputStream = FileOutputStream(file)
                        fileUri = Uri.fromFile(file)
                    }

                    outputStream.use { output ->
                        inputStream.copyTo(output)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Файл сохранён в: ${fileUri?.path}", Toast.LENGTH_LONG).show()
                    }

                } finally {
                    inputStream.close()
                    outputStream?.close()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка загрузки: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}