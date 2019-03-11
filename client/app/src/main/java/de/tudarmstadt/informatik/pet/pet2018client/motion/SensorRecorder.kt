package de.tudarmstadt.informatik.pet.pet2018client.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class SensorRecorder(private val mAccelerometerFilename: String,
                     private val mGyroscopeFilename: String,
                     private val mSensorAnalyzer: SensorAnalyzer): SensorEventListener {

    private val LOG_TAG = "SensorRecorder"

    private lateinit var sensorManager: SensorManager

    private lateinit var accelerometerFile: OutputStreamWriter
    private lateinit var gyroscopeFile: OutputStreamWriter

    var onAccelerometerChanged: (Float, Float, Float) -> Unit = fun (x, y, z) {}
    var onGyroscopeChanged: (Float, Float, Float) -> Unit = fun (x, y, z) {}

    fun startRecording(context: Context) {
        sensorManager = context.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        accelerometerFile = OutputStreamWriter(FileOutputStream(File(mAccelerometerFilename)))
        gyroscopeFile = OutputStreamWriter(FileOutputStream(File(mGyroscopeFilename)))

        sensorManager.registerListener(this, accelerometer, 20) // 20us interval
        sensorManager.registerListener(this, gyroscope, 20) // 20us interval

        Log.i(LOG_TAG, "starting to record sensor data")
    }

    fun stopRecording() {
        sensorManager.unregisterListener(this)
        accelerometerFile.close()
        gyroscopeFile.close()

        Log.i(LOG_TAG, "finished recording sensor data")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            val accelX = event.values[0]
            val accelY = event.values[1]
            val accelZ = event.values[2]

            saveDataPoint(accelerometerFile, event.timestamp, accelX, accelY, accelZ)
            onAccelerometerChanged(accelX, accelY, accelZ)
            mSensorAnalyzer.onAccelerometerChanged(event.timestamp, accelX, accelY, accelZ)

        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            val gyroX = event.values[0]
            val gyroY = event.values[1]
            val gyroZ = event.values[2]

            saveDataPoint(gyroscopeFile, event.timestamp, gyroX, gyroY, gyroZ)
            onGyroscopeChanged(gyroX, gyroY, gyroZ)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun saveDataPoint(buffer: OutputStreamWriter, timestamp: Long, x: Float, y: Float, z: Float) {
        buffer.write("${timestamp};${x};${y};${z}\n")
    }
}