package com.lastassassin.lastassassinv1

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_host_lobby.*
import kotlinx.android.synthetic.main.activity_settings_view.*
import org.json.JSONObject

class settingsView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_settings_view)


        val intent = intent
        val delay = intent.getIntExtra("delay", 60)
        val mode = intent.getStringExtra("mode")


        ArrayAdapter.createFromResource(
            this,
            R.array.game_modes_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner2.adapter = adapter
        }

        editTextNumber.setText(delay.toString())

        if (mode == "Classic") { //TODO: Honor
            spinner2.setSelection(0)
        }
        else if (mode == "Manual") {
            spinner2.setSelection(1)
        }
        else if (mode == "Auto") {
            spinner2.setSelection(2)
        }
        else {
            spinner2.setSelection(0)
        }

    }

    fun backToLobby(view: View) {

        val intent = intent
        val game_code = intent.getStringExtra("game_code")
        val host = intent.getStringExtra("host")
        val player_name = intent.getStringExtra("player_name")
        val token = intent.getStringExtra("token")
        val delay = editTextNumber.text.toString().toInt()
        val mode = "Honor" //TODO:spinner2.selectedItem.toString()

        lobbyHostHeartbeat(token, game_code, player_name, mode, delay, host)

    }

    fun vib(){
        var v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }


    public fun lobbyHostHeartbeat(token: String, game_code: String, player_name: String, mode: String, delay: Int, host: String) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.lastassassin.app/host"

        var myJsonObj = JSONObject("{Token:" + token + ", Mode:" + "Honor" + ", Delay:" + delay + "}") //TODO: mode

        val hostHeartbeat = JsonObjectRequest(
            Request.Method.POST,
            url,
            myJsonObj,

            { response2 ->

                if (response2.has("Error")) {
                    Snackbar.make(textView10, response2["Error"].toString(), Snackbar.LENGTH_LONG).show()
                }
                else {

                    val intent2 = Intent(this, host_lobby::class.java).apply {
                    }
                    intent2.putExtra("gameCode", game_code)
                    intent2.putExtra("host", host)
                    intent2.putExtra("player_name", player_name)
                    intent2.putExtra("mode", mode)
                    intent2.putExtra("delay", delay)
                    intent2.putExtra("token", token)
                    vib()
                    startActivity(intent2)

                }
            },
            { error ->
                queue.stop()
                vib()
                Snackbar.make(textView10, "An unexpected error occurred", Snackbar.LENGTH_LONG).show()
            }
        )
        queue.add(hostHeartbeat)

    }


}