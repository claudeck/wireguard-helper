package com.claudejiang.wghelper

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object NotificationHelper {
    val CHANNEL_ID = "normal"

    val notificationIdGenerator = AtomicInteger(0)

    fun notify(title : String, content : String, context: Context, nm : NotificationManager) {
        val time = Date()
        val timeStr = SimpleDateFormat("HH:mm:ss").format(time)
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("${timeStr}: ${content}")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        nm.notify(notificationIdGenerator.incrementAndGet(), notification)
    }
}