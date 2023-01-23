package com.example.scanbleeseo.Permission

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.scanbleeseo.R

class Permission_Localisation_refuseeActivity : AppCompatActivity() {
    val targetIntent_location = Intent().apply {
        action = android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS
    }
    companion object{
        fun getStartIntent(context: Context): Intent {
            return Intent(context, Permission_Localisation_refuseeActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_localisation_refusee)
        findViewById<Button>(R.id.button_activer_localisation).setOnClickListener {
            startActivity(targetIntent_location);
        }
    }
}