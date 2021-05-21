package com.lastassassin.lastassassinv1

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.content_create_game.*
import org.json.JSONObject


class createGame : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_game)

        val spinner: Spinner = findViewById(R.id.spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.game_modes_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

    }

    fun createGame(view: View) {

        val queue = Volley.newRequestQueue(this)
        val url = "https://api.lastassassin.app/create"

        val player = editText.text.toString()


        if (player != "") {
            errorTxt.text = ""

            val createAGame = JsonObjectRequest(
                Request.Method.POST,
                url,
                JSONObject("{Host:" + player + "}"),
                { response2 ->

                    if (response2.has("Error")) {
                        errorTxt.text = response2["Error"].toString()
                    }
                    else {
                        queue.stop()

                        val settings = applicationContext.getSharedPreferences("PREFS_NAME", 0)
                        val editor = settings.edit()
                        editor.putString("latestToken", response2["Token"].toString())
                        editor.apply()


                        val intent = Intent(this, host_lobby::class.java).apply {
                        }
                        intent.putExtra("token", response2["Token"].toString())
                        intent.putExtra("host", "true")
                        intent.putExtra("player_name", player)
                        intent.putExtra("mode", "Honor") //spinner.selectedItem.toString()
                        intent.putExtra("gameCode", response2["Token"].toString().take(5))
                        startActivity(intent)
                    }

                },
                { error ->
                    errorTxt.text = "Sorry, we experienced an unexpected error"
                    queue.stop()
                }
            )
            queue.add(createAGame)
        }
        else {
            errorTxt.text = "Please enter your name"
        }

    }

}

