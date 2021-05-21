package com.lastassassin.lastassassinv1

import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_host_lobby.*
import org.json.JSONObject

public val handler = Handler()
public var game_started = false


class host_lobby : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_host_lobby)

        game_started = false

        val intent = intent
        val token = intent.getStringExtra("token")
        val game_code = intent.getStringExtra("gameCode")
        val host = intent.getStringExtra("host")
        val player_name = intent.getStringExtra("player_name")
        val mode = intent.getStringExtra("mode")
        var delay = intent.getIntExtra("delay", 60)

        val queue = Volley.newRequestQueue(this)

        textView19.text = game_code

        if (host != "true") {
            textView8.isInvisible = true
            button5.isInvisible = true
        }

        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                if (game_started == false) {
                    if (host == "true") {
                        lobbyHostHeartbeat(token, game_code, player_name, queue)
                    }
                    else {
                        lobbyPlayerHeartbeat(token, game_code, player_name, queue)
                    }
                    handler.postDelayed(this, 1000)
                }
            }
        }


        handler.post(runnableCode)

        if (game_started == true){
            handler.removeCallbacks(runnableCode)
        }

    }


    public fun lobbyHostHeartbeat(token: String, game_code: String, player_name: String, queue: RequestQueue) {
//        val queue = Volley.newRequestQueue(this)
        val url = "https://api.lastassassin.app/host"

        var myJsonObj = JSONObject("{Token:" + token + "}")

        val hostHeartbeat = JsonObjectRequest(
            Request.Method.POST,
            url,
            myJsonObj,

            { response2 ->

                if (response2.has("Error")) {
                    Snackbar.make(textView8, response2["Error"].toString(), Snackbar.LENGTH_LONG).show()
                }
                else {

                    var players = response2["Players"].toString()

                    players = players.replace("\"", "").replace("[", "").replace("]", "")
                    var result = players.split(",")

                    listOfPlayers.text = ""
                    for (i in 0 until result.size) {
                        listOfPlayers.append(result.get(i))
                        listOfPlayers.append("\n")
                    }

                    if (response2.has("GameStarted")) {
                        game_started = response2["GameStarted"].toString().toBoolean()
                        handler.removeCallbacksAndMessages(null)

                        if (game_started == true) {
                            queue.stop()

                            val intent = Intent(this, game_play::class.java).apply {
                            }
                            intent.putExtra("gameCode", game_code)
                            intent.putExtra("playerName", player_name)
                            intent.putExtra("died", "false")
                            intent.putExtra("mode", response2["Mode"].toString())
                            intent.putExtra("cooldown", response2["Cooldown"].toString())
                            intent.putExtra("tagDistance", response2["TagDistance"].toString())
                            intent.putExtra("lagDistance", response2["LagDistance"].toString())
                            intent.putExtra("token", token)
                            vib()
                            startActivity(intent)
                        }
                    }
                }
            },
            { error ->
                queue.stop()
                vib()
                Snackbar.make(textView8, "There is an error with that game code", Snackbar.LENGTH_LONG).show()
            }
        )
        queue.add(hostHeartbeat)

    }

    public fun lobbyPlayerHeartbeat(token: String, game_code: String, player_name: String, queue2: RequestQueue) {
//        val queue2 = Volley.newRequestQueue(this)

        val url2 = "https://api.lastassassin.app/lobby"

        if (game_code != "" && player_name != "") {

            val playerHeartbeat = JsonObjectRequest(
                Request.Method.POST,
                url2,
                JSONObject("{Token:" + token + "}"),

                { response2 ->

                    if (response2.has("Error")) {
                        Snackbar.make(textView8, response2["Error"].toString(), Snackbar.LENGTH_LONG).show()
                    }
                    else {
                        var players = response2["Players"].toString()

                        players = players.replace("\"", "").replace("[", "").replace("]", "")
                        var result = players.split(",")

                        listOfPlayers.text = ""
                        for (i in 0 until result.size) {
                            listOfPlayers.append(result.get(i))
                            listOfPlayers.append("\n")
                        }

                        if (response2.has("GameStarted")) {
                                handler.removeCallbacksAndMessages(null)
                                queue2.stop()

                                val intent = Intent(this, game_play::class.java).apply {
                                }
                                intent.putExtra("gameCode", game_code)
                                intent.putExtra("playerName", player_name)
                                intent.putExtra("died", "false")
                                intent.putExtra("mode", response2["Mode"].toString())
                                intent.putExtra("cooldown", response2["Cooldown"].toString())
                                intent.putExtra("tagDistance", response2["TagDistance"].toString())
                                intent.putExtra("lagDistance", response2["LagDistance"].toString())
                                intent.putExtra("token", token)
                                vib()
                                startActivity(intent)
//                            }
                        }
                    }
                },
                { error ->
                    queue2.stop()
                    Snackbar.make(textView8, "There is an error with that game code", Snackbar.LENGTH_LONG).show()
                }
            )
            queue2.add(playerHeartbeat)

        }
        else {
            Snackbar.make(textView8, "An unexpected error has occurred", Snackbar.LENGTH_LONG).show()
        }

    }


    fun startGame(view: View) {

        val queue = Volley.newRequestQueue(this)
        val url = "https://api.lastassassin.app/start"

        val intent = intent
        val token = intent.getStringExtra("token")
        val game_code = intent.getStringExtra("gameCode")
        val player_name = intent.getStringExtra("player_name")
        val mode = intent.getStringExtra("mode")
        var players = listOfPlayers.text.toString()
        var result = players.split("\n")

        if (result.size >= 4) { //Check for at least 3 players
                val startGame = JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    JSONObject("{Token:" + token + "}"),
                    { response2 ->

                        if (response2.has("Error")) {
                            Snackbar.make(textView8, response2["Error"].toString(), Snackbar.LENGTH_LONG).show()
                        }
                        else {
                            queue.stop()

                            val intent = Intent(this, game_play::class.java).apply {
                            }
                            intent.putExtra("gameCode", game_code)
                            intent.putExtra("playerName", player_name)
                            intent.putExtra("died", "false")
                            intent.putExtra("mode", mode)
                            intent.putExtra("token", token)
                            startActivity(intent)
                        }

                    },
                    { error ->
                        queue.stop()
                        Snackbar.make(textView8, "An unexpected error has occurred", Snackbar.LENGTH_LONG).show()
                    }
                )
                queue.add(startGame)
        }
        else {
            Snackbar.make(textView8, "There must be at least 3 players", Snackbar.LENGTH_LONG).show()
        }

    }


    fun showSettings(view: View) {
        val intent = intent
        val token = intent.getStringExtra("token")
        val game_code = intent.getStringExtra("gameCode")
        val host = intent.getStringExtra("host")
        val player_name = intent.getStringExtra("player_name")
        val mode = intent.getStringExtra("mode")
        val delay = intent.getIntExtra("delay", 60)


        val intent2 = Intent(this, settingsView::class.java).apply {
        }
        intent2.putExtra("game_code", game_code)
        intent2.putExtra("host", host)
        intent2.putExtra("player_name", player_name)
        intent2.putExtra("mode", mode)
        intent2.putExtra("delay", delay)
        intent2.putExtra("token", token)
        startActivity(intent2)

    }

    fun vib(){
        var v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

}


