package com.claudejiang.wghelper

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            NotificationHelper.CHANNEL_ID,
            "Normal",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(channel)

        try {
            startService(Intent(applicationContext, AutoSwitchWireguardService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}