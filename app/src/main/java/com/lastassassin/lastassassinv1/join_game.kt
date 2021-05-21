package com.lastassassin.lastassassinv1

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_host_lobby.*
import kotlinx.android.synthetic.main.activity_join_game.*
import org.json.JSONObject

class join_game : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        setContentView(R.layout.activity_join_game)

    }


    fun joinGame(view: View) {

        val queue = Volley.newRequestQueue(this)
        var url = "https://api.lastassassin.app/join"

        val game_code = editGameCode.text.toString().toUpperCase()
        val player_name = editPlayerName.text.toString()

        val settings = applicationContext.getSharedPreferences("PREFS_NAME", 0)
        val latestToken = settings.getString("latestToken", "noToken")

        if (latestToken!!.take(5) == game_code) {
            //Rejoin game: lobby heartbeat and if game has started then send to game_play, else send to lobby
            Log.d("latestToken", latestToken)

            url = "https://api.lastassassin.app/lobby"

            val playerHeartbeat = JsonObjectRequest(
                Request.Method.POST,
                url,
                JSONObject("{Token:" + latestToken + "}"),

                { response2 ->

                    if (response2.has("Error")) {
                        Snackbar.make(textView8, response2["Error"].toString(), Snackbar.LENGTH_LONG).show()
                    }
                    else {
                        if (response2.has("GameStarted")) { //response2["GameStarted"] == true
                            queue.stop()

                            val intent = Intent(this, game_play::class.java).apply {
                            }
                            intent.putExtra("gameCode", game_code)
                            intent.putExtra("playerName", player_name)
                            intent.putExtra("delay", response2["Delay"].toString().toInt())
                            intent.putExtra("died", "false")
                            intent.putExtra("countdown", "true")
                            intent.putExtra("mode", response2["Mode"].toString())
                            intent.putExtra("cooldown", response2["Cooldown"].toString())
                            intent.putExtra("tagDistance", response2["TagDistance"].toString())
                            intent.putExtra("lagDistance", response2["LagDistance"].toString())
                            intent.putExtra("token", latestToken)
                            vib()
                            startActivity(intent)
                        }
                        else { //game has not started. send to lobby
                            val intent = Intent(this, host_lobby::class.java).apply {
                            }
                            intent.putExtra("token", latestToken)
                            intent.putExtra("player_name", player_name)
                            intent.putExtra("gameCode", latestToken.take(5))
                            intent.putExtra("host", "false")
                            vib()
                            startActivity(intent)
                        }

                    }
                },
                { error ->
                    queue.stop()
                    Snackbar.make(textView8, "There is an error with that game code", Snackbar.LENGTH_LONG).show()
                }
            )
            queue.add(playerHeartbeat)


        }
        else {
            url = "https://api.lastassassin.app/join"

            if (game_code != "" && player_name != "") {
                errorMessage.text = ""

                val playerHeartbeat = JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    JSONObject("{Game:" + game_code + ", Player:" + player_name + "}"),

                    { response2 ->

                        if (response2.has("Error")) {
                            errorMessage.text = response2["Error"].toString()
                        } else {
                            queue.stop()

                            val settings = applicationContext.getSharedPreferences("PREFS_NAME", 0)
                            val editor = settings.edit()
                            editor.putString("latestToken", response2["Token"].toString())
                            editor.apply()

                            val intent = Intent(this, host_lobby::class.java).apply {
                            }
                            intent.putExtra("token", response2["Token"].toString())
                            intent.putExtra("player_name", response2["Player"].toString())
                            intent.putExtra("gameCode", game_code)
                            intent.putExtra("host", "false")
                            vib()
                            startActivity(intent)
                        }

                    },
                    { error ->
                        //TODO: error handling
                        errorMessage.text = "There is an error with that game code"
                    }
                )
                queue.add(playerHeartbeat)


            } else {
                errorMessage.text = "Please fill out all the fields"
            }

        }

    }


    fun vib(){
        var v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

}