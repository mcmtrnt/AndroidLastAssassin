package com.lastassassin.lastassassinv1

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import kotlinx.android.synthetic.main.activity_game_over.*
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

class game_over : AppCompatActivity() {
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_game_over)

        val intent = intent
        val lastStanding = intent.getStringExtra("lastStanding")
        var finalScores = intent.getStringExtra("finalScores")

        finalScores = finalScores.replace("{", "").replace("}", "").replace("\"", "")
        var result = finalScores.split(",")

        listOfWinners.append("SCORES:\n")
        for (i in 0 until result.size) {
            var player = result.get(i).split(":")
            listOfWinners.append(player[0] + ":" + "\t")
            listOfWinners.append(player[1] + "\n")
        }

        textView32.text = lastStanding


        var adRequest = AdRequest.Builder().build()
        //Prod ID: ca-app-pub-5867008590692939/9502278244
        //Test ID: ca-app-pub-3940256099942544/1033173712
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("TAG", adError?.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("TAG", "Ad was loaded.")
                mInterstitialAd = interstitialAd
                displayAd()
            }
        })


        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("TAG", "Ad was dismissed.")
            }
            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Log.d("TAG", "Ad failed to show.")
            }
            override fun onAdShowedFullScreenContent() {
                Log.d("TAG", "Ad showed fullscreen content.")
                mInterstitialAd = null;
            }
        }

    }

    fun findGroup(view: View) {
        val intent2 = intent
        val token = intent2.getStringExtra("token")

        val intent3 = Intent(this, homeLocation::class.java).apply {
        }
        intent3.putExtra("token", token)
        startActivity(intent3)

    }

    fun mainMenu(view: View) {
        val intent = Intent(this, MainActivity::class.java).apply {
        }
        startActivity(intent)
    }


    fun displayAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

}