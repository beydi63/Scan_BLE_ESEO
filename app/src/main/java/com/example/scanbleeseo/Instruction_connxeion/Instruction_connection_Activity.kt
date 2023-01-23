package com.example.scanbleeseo.Instruction_connxeion

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.example.scanbleeseo.R

class Instruction_connection_Activity : AppCompatActivity() {

    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, Instruction_connection_Activity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction_connection)
    }
}