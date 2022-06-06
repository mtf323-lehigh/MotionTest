package com.example.motiontest

import android.content.Context
import android.hardware.Sensor
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.example.motiontest.databinding.ActivityMainBinding
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*

const val POLL_PERIOD_MILLIS = 500

class MainActivity : AppCompatActivity(), SensorEventListener {
    //private var TAG = "AccelTest"
    private lateinit var binding: ActivityMainBinding

    private var latestAccel: Float = 0.0f

    private lateinit var accelXTextView: TextView
    private lateinit var accelYTextView: TextView
    private lateinit var accelZTextView: TextView
    private lateinit var rotXTextView: TextView
    private lateinit var rotYTextView: TextView
    private lateinit var rotZTextView: TextView

    private lateinit var accelSensor: Sensor
    private lateinit var gyroSensor: Sensor

    private lateinit var sensorHandler: Handler
    private lateinit var sensorThread: HandlerThread
    private lateinit var sensorManager: SensorManager

    private inner class UiHandler: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val b = msg.data
            when (b.getString("update?")) {
                "accel" -> {
                    accelXTextView.text = msg.data.getFloat("accelX").toString()
                    accelYTextView.text = msg.data.getFloat("accelY").toString()
                    accelZTextView.text = msg.data.getFloat("accelZ").toString()
                }
                "rot" -> {
                    rotXTextView.text = msg.data.getFloat("rotX").toString()
                    rotYTextView.text = msg.data.getFloat("rotY").toString()
                    rotZTextView.text = msg.data.getFloat("rotZ").toString()
                }
            }
        }
    }

    private lateinit var uiHandler: UiHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        accelXTextView = findViewById(R.id.gravText1)
        accelYTextView = findViewById(R.id.gravText2)
        accelZTextView = findViewById(R.id.gravText3)
        rotXTextView = findViewById(R.id.rotText1)
        rotYTextView = findViewById(R.id.rotText2)
        rotZTextView = findViewById(R.id.rotText3)

        // setup UI handler
        uiHandler = UiHandler()

        // init sensor and manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // create thread for sensor polling
        sensorThread = HandlerThread("Sensor handler", Process.THREAD_PRIORITY_MORE_FAVORABLE)
        sensorThread.start()
        sensorHandler = Handler(sensorThread.looper)
        sensorManager.registerListener(this, accelSensor, POLL_PERIOD_MILLIS * 1000, sensorHandler)
        sensorManager.registerListener(this, gyroSensor, POLL_PERIOD_MILLIS * 1000, sensorHandler)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorThread.quitSafely()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                val accelX = event.values[0]
                val accelY = event.values[1]
                val accelZ = event.values[2]

                //val latestAccel = kotlin.math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)

                val bundle = Bundle()
                bundle.putString("update?", "accel")
                bundle.putFloat("accelX", accelX)
                bundle.putFloat("accelY", accelY)
                bundle.putFloat("accelZ", accelZ)
                val msg = Message()
                msg.data = bundle
                uiHandler.sendMessage(msg)
            }
            Sensor.TYPE_GYROSCOPE -> {
                val rotX = event.values[0]
                val rotY = event.values[1]
                val rotZ = event.values[2]

                val bundle = Bundle()
                bundle.putString("update?", "rot")
                bundle.putFloat("rotX", rotX)
                bundle.putFloat("rotY", rotY)
                bundle.putFloat("rotZ", rotZ)
                val msg = Message()
                msg.data = bundle
                uiHandler.sendMessage(msg)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // nothing!
    }

    /*private fun updateUI(view: View) {
        Log.i(TAG, "Entering updateUI()")
        val endTime = System.currentTimeMillis() + 5 * 1000
        while (System.currentTimeMillis() < endTime) {
            val msg = Message()
            val b = Bundle()
            b.putString("accel", latestAccel.toString())
            msg.data = b
            handler.sendMessage(msg)
        }
    }*/

    /*fun buttonOnClick(view: View) {
        Log.i(TAG, "Clicked!")
        Thread(Runnable() {
            updateUI(findViewById(R.id.grav_text))
        }).start()
    }*/

    /**
     * A native method that is implemented by the 'motiontest' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'motiontest' library on application startup.
        init {
            System.loadLibrary("motiontest")
        }
    }
}