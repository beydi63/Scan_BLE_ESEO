package com.example.scanbleeseo.MainActivity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.example.scanbleeseo.Commande_Led.CommandeActivity
import com.example.scanbleeseo.Instruction_connxeion.Instruction_connection_Activity
import com.example.scanbleeseo.R
import com.example.scanbleeseo.Scan_BLE.Scan_BLEActivity

class MainActivity : AppCompatActivity() {


    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MaterialDialog(this).show() {
            title(R.string.before_scan)
            message(R.string.before_scan1)
            icon(R.drawable.on)
        }
        findViewById<Button>(R.id.scan_periph).setOnClickListener{
            startActivity(Scan_BLEActivity.getStartIntent(this))
        }
        findViewById<Button>(R.id.commande_internet).setOnClickListener{
            startActivity(CommandeActivity.getStartIntent(this))
        }
        findViewById<Button>(R.id.prerequis).setOnClickListener{
            startActivity(Instruction_connection_Activity.getStartIntent(this))
        }

    }
}