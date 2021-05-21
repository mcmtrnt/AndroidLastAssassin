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
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_home_location.*
import org.json.JSONObject

public var loc3 = Location("")
public var loc4 = Location("")

class homeLocation : AppCompatActivity(), SensorEventListener {

    private var sensorManager1: SensorManager? = null
    private val accelerometerReading1 = FloatArray(3)
    private val magnetometerReading1 = FloatArray(3)
    private val rotationMatrix1 = FloatArray(9)
    private val orientationAngles1 = FloatArray(3)
    private lateinit var fusedLocationClient1: FusedLocationProviderClient
    val bearings = arrayOf("NE", "E", "SE", "S", "SW", "W", "NW", "N")
    var index = 0.0
    var compassDirection = "N"
    var arrowDirection = 0.0
    var lastArrowDirection = 0.0.toFloat()
    var facing = 0.0
    var rotate = 1

    private lateinit var locationCallback1: LocationCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_home_location)

        sensorManager1 = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val queue = Volley.newRequestQueue(this)

        val intent = intent
        val token = intent.getStringExtra("token")

        fusedLocationClient1 = LocationServices.getFusedLocationProviderClient(this)

        val request2 = createLocationRequest()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(request2!!)

        locationCallback1 = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    findTheGroup(location.latitude.toFloat(), location.longitude.toFloat(), token, queue)
                }
            }
        }

        startLocationUpdates(request2)
        updateOrientationAngles()


    }

    fun findTheGroup(latitude: Float, longitude: Float, token: String, queue: RequestQueue) {

//        val queue = Volley.newRequestQueue(this)
        val url = "https://api.lastassassin.app/game"

        if (token != "" && latitude != null && longitude != null) {
            val gHeartbeat = JsonObjectRequest(
                Request.Method.POST,
                url,
                JSONObject("{Token:" + token + ",Latitude:" + latitude + ",Longitude:" + longitude + "}"),
                { response2 ->
                    loc3.latitude = latitude.toDouble()
                    loc3.longitude = longitude.toDouble()

                    if (response2.has("LastLat")) {
                        if (response2["LastLat"] != 0) {
                            loc4.latitude = response2["LastLat"].toString().toDouble()
                            loc4.longitude = response2["LastLong"].toString().toDouble()
                        } else {
                            loc4.latitude = 40.2519
                            loc4.longitude = -111.6766
                        }
                    }

                    val targetDistance = loc3.distanceTo(loc4)
                    targetName2.text = targetDistance.toInt().toString() + "m"

                },
                { error ->
                    queue.stop()
                    Snackbar.make(textView9, "An unexpected error has occurred", Snackbar.LENGTH_LONG).show()
                }
            )
            queue.add(gHeartbeat)

        }

    }

    fun mainMenu(view: View) {
        fusedLocationClient1.removeLocationUpdates(locationCallback1)

        val intent = Intent(this, MainActivity::class.java).apply {
        }
        startActivity(intent)
    }


    /////////////////Bearing//////////////////
    override fun onResume() {
        super.onResume()

//        val accelerometer: Sensor? = sensorManager1!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager1!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager1!!.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager1!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager1!!.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }

    }

    override fun onPause() {
        super.onPause()
        sensorManager1!!.unregisterListener(this)
    }

    fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(
            rotationMatrix1, null,
            accelerometerReading1, magnetometerReading1
        )
        SensorManager.getOrientation(rotationMatrix1, orientationAngles1)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event!!.sensor.type === Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(
                event!!.values, 0, accelerometerReading1,
                0, accelerometerReading1.size
            )
        } else if (event!!.sensor.type === Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(
                event!!.values, 0, magnetometerReading1,
                0, magnetometerReading1.size
            )
        }

        updateOrientationAngles()
        facing = (orientationAngles1[0] * (180/3.14159265359))

        //Rotate compass
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

    /////////////LOCATION REQUEST//////////////////
    fun createLocationRequest(): LocationRequest? {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return locationRequest
    }

    private fun startLocationUpdates(request2 : LocationRequest) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient1.requestLocationUpdates(request2,
            locationCallback1,
            Looper.myLooper())
    }

    fun getCompassDirection(targetDirection: Float) {
        index = targetDirection - 22.5
        if (index < 0) {
            index += 360
        }
        index = (index / 45)
        compassDirection = bearings[index.toInt()]

        arrowDirection = (targetDirection - facing)

        rotateCompass(arrowDirection.toFloat())
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
        compass2.startAnimation(rotCompass)
        lastArrowDirection = arrowDirection
    }

}