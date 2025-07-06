package com.sbeu.markdowneditor

import android.net.Uri
import androidx.lifecycle.MutableLiveData

object MarkdownRepository {
    val markdownContent = MutableLiveData<String>()
    val currentFileUri = MutableLiveData<Uri?>()
}
