package com.claudejiang.wghelper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StartServiceOnBootReceiver : BroadcastReceiver() {

    private val tag = StartServiceOnBootReceiver::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        val receiveAction: String? = intent.action
        Log.d(tag, "onReceive intent $receiveAction")
        if (receiveAction == "android.intent.action.BOOT_COMPLETED") {
            try {
                context.startService(Intent(context, AutoSwitchWireguardService::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}