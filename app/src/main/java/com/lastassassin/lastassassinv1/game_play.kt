package com.lastassassin.lastassassinv1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.*
import android.util.Log
import android.view.View
import android.view.View.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.game_play.*
import org.json.JSONObject
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;


public var loc1 = Location("")
public var loc2 = Location("")

class game_play : AppCompatActivity(), SensorEventListener {

    //variables
    private var sensorManager: SensorManager? = null
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var isUp = false
    val bearings = arrayOf("NE", "E", "SE", "S", "SW", "W", "NW", "N")
    var index = 0.0
    var compassDirection = "N"
    var arrowDirection = 0.0
    var lastArrowDirection = 0.0.toFloat()
    var facing = 0.0
    var tags = 0
    var rotate = 2
    private lateinit var locationCallback: LocationCallback
    private var mInterstitialAd: InterstitialAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.game_play)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val queue = Volley.newRequestQueue(this)

        val intent = intent
        val game_code = intent.getStringExtra("gameCode")
        val player_name = intent.getStringExtra("playerName")
        val mode = intent.getStringExtra("mode")
        var died = intent.getStringExtra("died")
        var token = intent.getStringExtra("token")


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        isUp = false;

        if (mode == "Auto") {
            button4.visibility = INVISIBLE
        }


        ///////////////////////////
        val request1 = createLocationRequest()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(request1!!)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    gameHeartbeat(game_code, player_name, location.latitude.toFloat(), location.longitude.toFloat(), died, token, queue)
                }
            }
        }

        ///////////////////////////////Check Permissions///////////////////////////////////////////
        if (ContextCompat.checkSelfPermission(this@game_play,
                Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@game_play,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }


        var adRequest = AdRequest.Builder().build()
        //Prod ID: ca-app-pub-5867008590692939/9502278244
        //Test ad ID: ca-app-pub-3940256099942544/1033173712
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
        ///////////////////////

        startLocationUpdates(request1)
        updateOrientationAngles()

    }


    fun displayAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

/////////////////Bearing//////////////////
override fun onResume() {
    super.onResume()
    val accelerometer: Sensor? = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
        sensorManager!!.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI
        )
    }
    sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
        sensorManager!!.registerListener(
            this,
            magneticField,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI
        )
    }

}

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }

    fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(
            rotationMatrix, null,
            accelerometerReading, magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event!!.sensor.type === Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(
                event!!.values, 0, accelerometerReading,
                0, accelerometerReading.size
            )
        } else if (event!!.sensor.type === Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(
                event!!.values, 0, magnetometerReading,
                0, magnetometerReading.size
            )
        }

        updateOrientationAngles()
        facing = (orientationAngles[0] * (180/3.14159265359))

        //Rotate Compass
        if (rotate % 15 == 0) {
            val targetDirection = loc1.bearingTo(loc2)
            getCompassDirection(targetDirection)
            rotate += 1
            if(rotate > 500000) {
                rotate = 2
            }
        }
        else{
            rotate += 1
        }

    }

//////////////////////////////////////////

    public fun gameHeartbeat(game_code: String, player_name: String, latitude: Float, longitude: Float, died: String, token: String, queue: RequestQueue) {

//        val queue = Volley.newRequestQueue(this)
        val url = "https://api.lastassassin.app/game"

        if (game_code != "" && player_name != "" && latitude != null && longitude != null) {

            //lobby heartbeat and if that works then send to next activity
            val gHeartbeat = JsonObjectRequest(
                Request.Method.POST,
                url,
                JSONObject("{Token:" + token + ",Latitude:" + latitude + ",Longitude:" + longitude + "}"),
                { response2 ->

                    if (response2.has("Countdown")) {
                        var c = response2["Countdown"].toString().toFloat()
//                        Log.d("countdown", c.toString())
                        gameDelayText.text = "Game Begins In: " + c.toInt().toString() + " seconds.\nGo hide!"
                    }
                    else {
                        gameDelay.visibility = INVISIBLE
                        gameDelayText.visibility = INVISIBLE
                        //TODO close the ad... impossible. User must close it themselves...
                    }

                    if (response2.has("Pending")) {
                        var pending = response2["Pending"].toString()

                        if(pending != "[]") {
                            var pendingTags = pending.replace("[", "").replace("]", "").replace("\"", "")

                            if (pendingTags.contains(",")) {
                                var result = pendingTags.split(",")
                                pendingTags = result[0].toString()
                            }

                            fusedLocationClient.removeLocationUpdates(locationCallback)
                            queue.stop()

                            val intent3 = Intent(this, verify::class.java).apply {
                            }
                            intent3.putExtra("gameCode", game_code)
                            intent3.putExtra("playerName", player_name)
                            intent3.putExtra("playersAlive", response2["PlayersAlive"].toString())
                            intent3.putExtra("pendingTags", pendingTags)
                            intent3.putExtra("token", token)
                            startActivity(intent3)

                        }
                    }

                    if (response2.has("PlayersAlive")) {

                        if (response2["Living"] == false && died == "false") { //send to you_died screen
                            fusedLocationClient.removeLocationUpdates(locationCallback)
                            queue.stop()
                            vib()
                            val intent = Intent(this, you_died::class.java).apply {
                            }
                            intent.putExtra("gameCode", game_code)
                            intent.putExtra("playerName", player_name)
                            intent.putExtra("playersAlive", response2["PlayersAlive"].toString())
                            startActivity(intent)
                        }
                    }

                    if (response2.has("LastStanding")) {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                        queue.stop()

                        vib()
                        val intent = Intent(this, game_over::class.java).apply {
                        }
                        intent.putExtra("lastStanding", response2["LastStanding"].toString())
                        intent.putExtra("lastStandingLat", response2["LastLat"].toString())
                        intent.putExtra("lastStandingLong", response2["LastLong"].toString())
                        intent.putExtra("finalScores", response2["FinalScores"].toString())
                        intent.putExtra("token", token)
                        startActivity(intent)
                    }


                    if (response2.has("TargetName")) {

                        if(targetName.text != "Unknown" && targetName.text.toString() != "Target: " + response2["TargetName"].toString()) {
                            vib()
                            Snackbar.make(view3, "You have a new target", Snackbar.LENGTH_LONG).show()
                        }

                        targetName.text = "Target: " + response2["TargetName"].toString()
                    }
                    else {
                        targetName.text = "Unknown"
                    }
                    if (response2.has("Tags")) {
                        if (response2["Tags"].toString().toInt() > tags){
                            vib()
                            Snackbar.make(view3, "You killed your target", Snackbar.LENGTH_LONG).show()
                            tags = tags + 1
                        }

                        currentKills.text = "Tags: " + response2["Tags"].toString()
                    }
                    else {
                        currentKills.text = "Unknown"
                    }
                    if (response2.has("PlayersAlive")) {
                        alive.text = "Alive: " + response2["PlayersAlive"].toString()
                    }
                    else {
                        alive.text = "Unknown"
                    }

                    loc1.latitude = latitude.toDouble()
                    loc1.longitude = longitude.toDouble()

                    if (response2.has("TargetLat")) {
                        if (response2["TargetLat"] != 0) {
                            loc2.latitude = response2["TargetLat"].toString().toDouble()
                            loc2.longitude = response2["TargetLong"].toString().toDouble()
                        } else {
                            loc2.latitude = 40.2519
                            loc2.longitude = -111.6766
                        }
                    }

                    val targetDistance = loc1.distanceTo(loc2)
                    data.text = targetDistance.toInt().toString() + "m"

                },
                { error ->
                    queue.stop()
                    Snackbar.make(view3, "An unexpected error has occurred", Snackbar.LENGTH_LONG).show()
                }
            )
            queue.add(gHeartbeat)

        }
        else {
            Snackbar.make(view3, "Error: Missing data", Snackbar.LENGTH_LONG).show()
        }

    }


    public fun attemptTag(view:View) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.lastassassin.app/tag"

        val intent = intent
        val token = intent.getStringExtra("token")

        vib()

        if (token != "") {

            val attemptRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                JSONObject("{Token:" + token + "}"),

                { response2 ->

                    if (response2.has("Error")) {
                        Snackbar.make(view3, response2["Error"].toString(), Snackbar.LENGTH_LONG).show()
                    }
                    else {
                        Snackbar.make(view3, "Tag attempt sent", Snackbar.LENGTH_LONG).show()
                    }
                    queue.stop()

                },
                { error ->
                    queue.stop()
                    Snackbar.make(view3, "An unexpected error has occurred", Snackbar.LENGTH_LONG).show()
                }
            )
            queue.add(attemptRequest)
        }

    }


    fun createLocationRequest(): LocationRequest? {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return locationRequest
    }


    private fun startLocationUpdates(request1 : LocationRequest) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED

        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(request1,
            locationCallback,
            Looper.getMainLooper())
    }


    //TODO: arrow doesn't always point right direction... I feel like it should be working...
    fun getCompassDirection(targetDirection: Float) {
//        Log.d("targetDirection", targetDirection.toString())

        index = targetDirection - 22.5
        if (index < 0) {
            index += 360
        }
        index = (index / 45)
        compassDirection = bearings[index.toInt()]

        arrowDirection = (targetDirection - facing) //?

//        Log.d("facing", facing.toString())
//        Log.d("arrowDirection", arrowDirection.toString())

        rotateCompass(arrowDirection.toFloat())
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            this@game_play,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) ===
                                PackageManager.PERMISSION_GRANTED)
                    ) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


    fun rotateCompass(arrowDirection: Float) {
        val rotCompass = RotateAnimation(
            lastArrowDirection,
            arrowDirection,
            Animation.RELATIVE_TO_SELF,
            .5f,
            Animation.RELATIVE_TO_SELF,
            .5f
        )
        rotCompass.fillAfter = true
        compass.startAnimation(rotCompass)
        lastArrowDirection = arrowDirection
    }

    fun vib(){
        var v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

}

