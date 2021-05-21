package com.lastassassin.lastassassinv1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View

class warning : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_warning)

        //only show the warning screen once.
        val settings = applicationContext.getSharedPreferences("warning", 0)
        val latestToken = settings.getString("warned", "noToken")

        if(latestToken == "yes") {
            val intent = intent
            val type = intent.getStringExtra("type")

            if (type == "create") {
                displayCreateGame()
            }
            else {
                displayJoinGame()
            }
        }
        else {
            val settings2 = applicationContext.getSharedPreferences("warning", 0)
            val editor = settings2.edit()
            editor.putString("warned", "yes")
            editor.apply()
        }


    }

    fun continueGame(view: View) {
        val intent = intent
        val type = intent.getStringExtra("type")

        if (type == "create") {
            displayCreateGame()
        }
        else {
            displayJoinGame()
        }
    }

    fun displayCreateGame() {
        val intent = Intent(this, createGame::class.java).apply {
        }
        startActivity(intent)
    }

    fun displayJoinGame() {
        val intent = Intent(this, join_game::class.java).apply {
        }
        startActivity(intent)
    }


}