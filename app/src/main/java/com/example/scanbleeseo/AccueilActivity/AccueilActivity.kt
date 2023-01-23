package com.example.scanbleeseo.AccueilActivity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.afollestad.materialdialogs.MaterialDialog
import com.example.scanbleeseo.Instruction_connxeion.Instruction_connection_Activity
import com.example.scanbleeseo.MainActivity.MainActivity
import com.example.scanbleeseo.R

class AccueilActivity : AppCompatActivity() {

    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, AccueilActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accueil)

        findViewById<ImageView>(R.id.logo_scan_ble).setOnClickListener{
            startActivity(MainActivity.getStartIntent(this))
        }



    }

}