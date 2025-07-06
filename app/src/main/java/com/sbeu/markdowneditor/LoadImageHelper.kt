package com.sbeu.markdowneditor

import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import java.net.URL
import java.util.concurrent.Executors

object LoadImageHelper {
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    fun load(imageView: ImageView, urlString: String) {
        executor.execute {
            try {
                val url = URL(urlString)
                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                handler.post {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                handler.post {
                    imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            }
        }
    }
}