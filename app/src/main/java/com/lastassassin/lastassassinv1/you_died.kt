package com.lastassassin.lastassassinv1

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import kotlinx.android.synthetic.main.activity_you_died.*

class you_died : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_you_died)

        val intent = intent
        val players_alive = intent.getStringExtra("playersAlive")

        textView23.text = players_alive

    }

    fun continueGame(view: View) {
        val intent = intent
        val game_code = intent.getStringExtra("gameCode")
        val player_name = intent.getStringExtra("playerName")
        val token = intent.getStringExtra("token")


        val intent2 = Intent(this, game_play::class.java).apply {
        }
        intent2.putExtra("gameCode", game_code)
        intent2.putExtra("playerName", player_name)
        intent2.putExtra("delay", 0)
        intent2.putExtra("died", "true")
        intent2.putExtra("countdown", "true")
        intent2.putExtra("token", token)
        startActivity(intent2)
    }

}
