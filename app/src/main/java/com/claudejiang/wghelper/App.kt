package com.claudejiang.wghelper

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

class App : Application() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate() {
        super.onCreate()

        var nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(NotificationHelper.CHANNEL_ID, "Normal", NotificationManager.IMPORTANCE_DEFAULT)
        nm.createNotificationChannel(channel)

        try {
            startService(Intent(applicationContext, AutoSwitchWireguardService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}