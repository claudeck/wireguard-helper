package com.claudejiang.wghelper

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import java.net.NetworkInterface

class AutoSwitchWireguardService : Service() {

    private val tag = AutoSwitchWireguardService::class.java.simpleName

    override fun onCreate() {
        super.onCreate()

        val cm = getSystemService(ConnectivityManager::class.java)

//        Thread {
//            while (true) {
//                try {
//                    Thread.sleep(5 * 60 * 1000)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//
//                val activeNetwork = cm.activeNetwork
//                if (activeNetwork != null) {
//                    handleWireGuardByNetworkStatus(activeNetwork, SCHEDULED_MODE)
//                }
//            }
//        }.start()

        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                handleWireGuardByNetworkStatus(network)
            }
        })
    }

    private fun handleWireGuardByNetworkStatus(network: Network) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val cm = getSystemService(ConnectivityManager::class.java)

        val cap = cm.getNetworkCapabilities(network)
        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val notificationEnable = prefs.getBoolean("notificationEnable", false)

        if (cap != null) {
            val connectedWg = connectedToWireGuard()
            Log.e(tag, "wifi:" + cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            Log.e(tag, "cellular:" + cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            Log.e(tag, "wg connect status:$connectedWg")
            if (cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                if (connectedWg) {
                    val result = turnOffWireguard(3)
                    if (notificationEnable && result) {
                        NotificationHelper.notify(
                            getString(R.string.turn_off_wireguard_title),
                            getString(R.string.turn_off_wireguard_content),
                            this,
                            nm
                        )
                    }
                }
                return
            }
            if (cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) && !connectedWg) {
                val result = turnOnWireGuard()
                if (notificationEnable && result) {
                    NotificationHelper.notify(
                        getString(R.string.turn_on_wireguard_title),
                        getString(R.string.turn_on_wireguard_content),
                        this,
                        nm
                    )
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun turnOffWireguard(retry: Int): Boolean {
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val wgEnable = prefs.getBoolean("wgEnable", false)
        val wgName = prefs.getString("wgName", "")

        if (!wgEnable || wgName == "") {
            return false
        }

        val ssids = prefs.getString("ssids", "")
        val ssidSet = ssids?.split(",")?.toSet()

        val wm = getSystemService(WifiManager::class.java)
        val ci = wm.connectionInfo
        val ssid = ci.ssid.removeSurrounding("\"")
        Log.e(tag, "ssid $ssid, rssi $ci.rssi")
        // If wifi strength is low, retry turn off after 10s.
        if (ci.rssi < -67) {
            try {
                Thread.sleep(10000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val cm = getSystemService(ConnectivityManager::class.java)
            val cap = cm.getNetworkCapabilities(cm.activeNetwork)
            if (cap != null) {
                if (cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && connectedToWireGuard()) {
                    Log.e(tag, "Retry turn off $retry countdown")
                    return turnOffWireguard(retry - 1)
                }
            }
            return false
        }
        if (ssidSet == null || ssidSet.isEmpty() || ssidSet.contains(ssid)) {
            Log.e(tag, "turn off the wireguard")
            val sendIntent: Intent = Intent().apply {
                `package` = "com.wireguard.android"
                action = "com.wireguard.android.action.SET_TUNNEL_DOWN"
                putExtra("tunnel", wgName)
            }
            sendBroadcast(sendIntent)
            return true
        }
        return false
    }

    private fun connectedToWireGuard(): Boolean {
        for (ni in NetworkInterface.getNetworkInterfaces()) {
            Log.e(tag, "iface:" + ni.name)
            Log.e(tag, "iface status:" + ni.isUp)
//            for (ia in ni.interfaceAddresses) {
//                Log.e(tag, "iface addr:" + ia.address.hostAddress)
//            }
            if (ni.name.startsWith("tun") && ni.isUp) {
                return true
            }
        }
        return false
    }

    private fun turnOnWireGuard(): Boolean {
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val wgEnable = prefs.getBoolean("wgEnable", false)
        val wgName = prefs.getString("wgName", "")

        if (!wgEnable || wgName == "") {
            return false
        }

        Log.e(tag, "turn on the wireguard")
        val sendIntent: Intent = Intent().apply {
            `package` = "com.wireguard.android"
            action = "com.wireguard.android.action.SET_TUNNEL_UP"
            putExtra("tunnel", wgName)
        }
        sendBroadcast(sendIntent)
        return true
    }

}