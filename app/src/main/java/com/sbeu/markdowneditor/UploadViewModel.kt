package com.sbeu.markdowneditor

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UploadViewModel(application: Application) : AndroidViewModel(application) {

    private val _markdownContent = MutableLiveData<String>()
    val markdownContent: LiveData<String> = _markdownContent

    private val contentResolver: ContentResolver = application.contentResolver

    fun loadMarkdownFromUri(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            val text = contentResolver.openInputStream(uri)?.use {
                it.bufferedReader().use { reader -> reader.readText() }
            } ?: ""
            _markdownContent.postValue(text)
        }
    }

    fun loadMarkdownFromUrl(urlString: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val content = reader.use { it.readText() }
                    _markdownContent.postValue(content)
                } else {
                    _markdownContent.postValue("Ошибка загрузки: HTTP $responseCode")
                }
            } catch (e: Exception) {
                _markdownContent.postValue("Ошибка загрузки: ${e.message}")
            }
        }
    }
}