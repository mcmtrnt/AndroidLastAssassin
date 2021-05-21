package com.lastassassin.lastassassinv1


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ///////////////////////////////Check Permissions///////////////////////////////////////////
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        MobileAds.initialize(this) {}

    }



    fun displayCreateGame(view: View) {
        val intent = Intent(this, warning::class.java).apply {
        }
        intent.putExtra("type", "create")
        startActivity(intent)
    }

    fun displayJoinGame(view: View) {
        val intent = Intent(this, warning::class.java).apply {
        }
        intent.putExtra("type", "join")
        startActivity(intent)
    }

    fun showTutorial(view: View) {
        val intent = Intent(this, tutorial::class.java).apply {
        }
        startActivity(intent)
    }


}




