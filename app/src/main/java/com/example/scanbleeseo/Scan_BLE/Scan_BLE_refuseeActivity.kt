package com.example.scanbleeseo.Scan_BLE

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.scanbleeseo.R

class Scan_BLE_refuseeActivity : AppCompatActivity() {
    val targetIntent_location = Intent().apply {
        action = android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
    }
    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, Scan_BLE_refuseeActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_ble_refusee)
        findViewById<Button>(R.id.button_activer_bluetooth).setOnClickListener {
            startActivity(targetIntent_location);
        }
    }
}