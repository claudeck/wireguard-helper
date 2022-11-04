package com.claudejiang.wghelper

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editor = getSharedPreferences("config", Context.MODE_PRIVATE).edit()
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val wgEnable = prefs.getBoolean("wgEnable", false)
        val wgName = prefs.getString("wgName", "")
        val ssids = prefs.getString("ssids", "")

        val wgEnableSwitch : SwitchMaterial = findViewById(R.id.wg_switch)
        val wgNameInput : EditText = findViewById(R.id.wg_name_input)
        val ssIdsInput : EditText = findViewById(R.id.ssids_input)
        val readme : TextView = findViewById(R.id.readme)

        readme.setText(Html.fromHtml(getString(R.string.readme), Html.FROM_HTML_MODE_LEGACY))

        wgEnableSwitch.isChecked = wgEnable
        wgNameInput.setText(wgName)
        ssIdsInput.setText(ssids)

        wgEnableSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && wgNameInput.text.toString() == "") {
                Toast.makeText(this, "请输入Wireguard Name，然后在激活服务", Toast.LENGTH_SHORT).show()
                buttonView.isChecked = false
            }

            editor.putBoolean("wgEnable", buttonView.isChecked)
            editor.apply()
        }

        wgNameInput.addTextChangedListener {
            Log.d(TAG, "wgName changed")
            editor.putString("wgName", wgNameInput.text.toString())
            editor.apply()
        }

        ssIdsInput.addTextChangedListener {
            editor.putString("ssids", ssIdsInput.text.toString())
            editor.apply()
        }
    }
}