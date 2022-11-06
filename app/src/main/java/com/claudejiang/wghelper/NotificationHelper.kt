package com.claudejiang.wghelper

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object NotificationHelper {
    const val CHANNEL_ID = "normal"

    private val notificationIdGenerator = AtomicInteger(0)

    fun notify(title: String, content: String, context: Context, nm: NotificationManager) {
        val time = Date()
        SimpleDateFormat.getTimeInstance()
        val timeStr =
            SimpleDateFormat("HH:mm:ss", Locale.getDefault(Locale.Category.FORMAT)).format(time)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("$timeStr: $content")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        nm.notify(notificationIdGenerator.incrementAndGet(), notification)
    }
}