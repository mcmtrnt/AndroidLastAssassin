package com.lastassassin.lastassassinv1

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_verify.*
import org.json.JSONObject

class verify : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_verify)

        val intent = intent
        var pendingTags = intent.getStringExtra("pendingTags")
        taggedText2.text = "Did you get tagged by " + pendingTags + "?"

    }

    fun yesVerify(view: View) {

        val intent = intent
        val game_code = intent.getStringExtra("gameCode")
        var playerName = intent.getStringExtra("playerName")
        var playersAlive = intent.getStringExtra("playersAlive")
        var pendingTags = intent.getStringExtra("pendingTags")
        val token = intent.getStringExtra("token")

        val queue2 = Volley.newRequestQueue(this)
        val url = "https://api.lastassassin.app/verify"

        vib()

        if(game_code != "" && playerName != "" && pendingTags != "") {

            val myJsonObj = JSONObject("{Token:" + token + ", Hunter:" + pendingTags + ", Accept:" + "\"true\"" + "}")

            val myVerifyRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                myJsonObj,
                Response.Listener { response2 ->

                    if (response2.has("Error")) {
                        Snackbar.make(taggedText2, response2["Error"].toString(), Snackbar.LENGTH_LONG).show()
                    }
                    else {
                        playersAlive = (playersAlive.toInt() - 1).toString()

                        val intent4 = Intent(this, you_died::class.java).apply {
                        }
                        intent4.putExtra("gameCode", game_code)
                        intent4.putExtra("playerName", playerName)
                        intent4.putExtra("playersAlive", playersAlive)
                        intent4.putExtra("token", token)
                        startActivity(intent4)
                    }

                },
                Response.ErrorListener { error ->
                    Snackbar.make(taggedText2, "An unexpected error has occurred", Snackbar.LENGTH_LONG).show()
                }
            )
            queue2.add(myVerifyRequest)

        }
        else {
            Snackbar.make(taggedText2, "Error: Missing information", Snackbar.LENGTH_LONG).show()
        }

    }


    fun noVerify(view: View) {

        val intent = intent
        val game_code = intent.getStringExtra("gameCode")
        var playerName = intent.getStringExtra("playerName")
        var playersAlive = intent.getStringExtra("playersAlive")
        var pendingTags = intent.getStringExtra("pendingTags")
        val token = intent.getStringExtra("token")

        val queue2 = Volley.newRequestQueue(this)
        val url = "https://api.lastassassin.app/verify"
        vib()

        if(game_code != "" && playerName != "" && pendingTags != "") {

            val myJsonObj = JSONObject("{Token:" + token + ", Hunter:" + pendingTags + ", Accept:" + "\"false\"" + "}")

            val myVerifyRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                myJsonObj,
                Response.Listener { response2 ->

                    if (response2.has("Error")) {
                        Snackbar.make(taggedText2, response2["Error"].toString(), Snackbar.LENGTH_LONG).show()
                    }
                    else {
                        playersAlive = (playersAlive.toInt() - 1).toString()

                        val intent4 = Intent(this, game_play::class.java).apply {
                        }
                        intent4.putExtra("gameCode", game_code)
                        intent4.putExtra("playerName", playerName)
                        intent4.putExtra("token", token)
                        intent4.putExtra("died", "false")
                        startActivity(intent4)
                    }

                },
                Response.ErrorListener { error ->
                    Snackbar.make(taggedText2, "An unexpected error has occurred", Snackbar.LENGTH_LONG).show()
                }
            )
            queue2.add(myVerifyRequest)

        }
        else {
            Snackbar.make(taggedText2, "Error: Missing information", Snackbar.LENGTH_LONG).show()
        }

    }


    fun vib(){
        var v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

}