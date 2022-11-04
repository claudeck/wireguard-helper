package com.claudejiang.wghelper

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class AutoSwitchWireguardService : Service() {

    val TAG = AutoSwitchWireguardService::class.java.simpleName

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()

        val cm = getSystemService(ConnectivityManager::class.java)
        var nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Thread {
            while (true) {
                try {
                    Thread.sleep(5 * 60 * 1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val cap = cm.getNetworkCapabilities(cm.activeNetwork)

                if (cap != null) {
                    if (cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        val result = turnOnWireguard()
                        if (result) {
                            NotificationHelper.notify("打开Wireguard", "检查到当前使用手机网络，定时任务帮你开启Wireguard.", this, nm)
                        }
                    } else if (cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        val result = turnOffWiregaurd()
                        if (result) {
                            NotificationHelper.notify("关闭Wireguard", "检查到当前使用Wifi，定时任务帮你关闭Wireguard.", this, nm)
                        }
                    }
                }
            }
        }.start()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val context = this
            cm.registerDefaultNetworkCallback(
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        val cap = cm.getNetworkCapabilities(network)
                        if (cap != null) {
                            if (cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                Log.e(TAG, "WIFI is available")
                                val result = turnOffWiregaurd()
                                if (result) {
                                    NotificationHelper.notify("关闭Wireguard", "检查到当前使用Wifi，立刻帮你关闭Wireguard.", context, nm)
                                }
                            } else if(cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                                Log.e(TAG, "WIFI is lost")
                                val result = turnOnWireguard()
                                if (result) {
                                    NotificationHelper.notify("打开Wireguard", "检查到当前使用手机网络，立刻帮你开启Wireguard.", context, nm)
                                }
                            }
                        }
                    }
                })

        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun turnOffWiregaurd() : Boolean {
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val wgEnable = prefs.getBoolean("wgEnable", false)
        val wgName = prefs.getString("wgName", "")

        if (! wgEnable || wgName == "") {
            return false
        }

        val ssids = prefs.getString("ssids", "")
        var ssidSet = ssids?.split(",")?.toSet()

        val ssid = getWifiSsid()
        Log.e(TAG, "ssid $ssid")
        if (ssidSet == null || ssidSet.isEmpty() || ssidSet.contains(ssid)) {
            for (ni in NetworkInterface.getNetworkInterfaces()) {
                if (ni.name == "tun0") {
                    Log.e(TAG, "turn off the wireguard")
                    val sendIntent: Intent = Intent().apply {
                        `package` = "com.wireguard.android"
                        action = "com.wireguard.android.action.SET_TUNNEL_DOWN"
                        putExtra("tunnel", wgName)
                    }
                    sendBroadcast(sendIntent)
                    return true
                }
            }
        }
        return false
    }

    private fun getWifiSsid(): String {
        val wm = getSystemService(WifiManager::class.java)
        val ci = wm.connectionInfo
        val ssid = ci.ssid.removeSurrounding("\"")
        return ssid
    }

    private fun turnOnWireguard() : Boolean {
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val wgEnable = prefs.getBoolean("wgEnable", false)
        val wgName = prefs.getString("wgName", "")

        if (! wgEnable || wgName == "") {
            return false
        }

        var hasTun0 = false
        for (ni in NetworkInterface.getNetworkInterfaces()) {
            if (ni.name == "tun0") {
                hasTun0 = true
                break
            }
        }
        if (!hasTun0) {
            Log.e(TAG, "turn on the wireguard")
            val sendIntent: Intent = Intent().apply {
                `package` = "com.wireguard.android"
                action = "com.wireguard.android.action.SET_TUNNEL_UP"
                putExtra("tunnel", wgName)
            }
            sendBroadcast(sendIntent)
            return true
        } else {
            Log.e(TAG, "VPN is alive.")
        }
        return false
    }

}