package de.tudarmstadt.informatik.pet.pet2018client

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.GeneralizedGoertzel
import be.tarsos.dsp.pitch.Goertzel
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import de.tudarmstadt.informatik.pet.pet2018client.audio.*
import de.tudarmstadt.informatik.pet.pet2018client.motion.SensorAnalyzer
import de.tudarmstadt.informatik.pet.pet2018client.motion.SensorRecorder
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private val LOG_TAG = "MainActivity"

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private var showMotionLayout = true

    //Analyse audio data
    private var recordingAudio: Boolean = false
    private lateinit var audioDispatcher: AudioDispatcher
    private lateinit var audioThread: Thread
    private lateinit var attc: AudioToTextConverter
    private lateinit var wis: WAVInputStream
    private lateinit var recAudioFile : String
    private lateinit var inputAudioFile : String
    private lateinit var audioConverter: AudioConverter

    private lateinit var tv_rec: TextView
    private lateinit var btn_rec: Button

    //Analyse motion data
    private var recordingMotionData: Boolean = false

    private lateinit var audioRecorder: AudioRecorder

    private lateinit var sensorRecorder: SensorRecorder
    private val sensorAnalyzer: SensorAnalyzer = SensorAnalyzer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputAudioFile = "${externalCacheDir.absolutePath}/audio.wav"
        recAudioFile = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath + "/audio.3gp"
        Log.i(LOG_TAG, "Writing audio to file: ${recAudioFile}")
        Log.i(LOG_TAG, "Reading audio from file: ${recAudioFile}")

        audioRecorder = AudioRecorder(recAudioFile)
        audioConverter = AudioConverter(this)
        this.copyAssetSoundToFile(recAudioFile)

        this.loadFFmpegLib()
        AndroidFFMPEGLocator(this)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        val accelerometerFilename =
            getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath + "/accelerometer-data.csv"
        val gyroscopeFilename =
            getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath + "/gyroscope-data.csv"
        Log.i(LOG_TAG, "Writing sensor data to files ${accelerometerFilename} and ${gyroscopeFilename}")

        sensorRecorder = SensorRecorder(accelerometerFilename, gyroscopeFilename, sensorAnalyzer)

        tv_rec = findViewById<TextView>(R.id.textView_rec) as TextView
        btn_rec = findViewById<TextView>(R.id.btn_record) as Button
    }

    private fun loadFFmpegLib()
    {
        try {
            FFmpeg.getInstance(this).loadBinary(object : FFmpegLoadBinaryResponseHandler {
                override fun onFailure() {

                }

                override fun onSuccess() {

                }

                override fun onStart() {

                }

                override fun onFinish() {

                }
            })
        } catch (e: Exception) {
            Log.e("AudioConverter", "Failed to load FFmpeg library!", e)
        }
    }

    private fun copyAssetSoundToFile(filename: String) {
        //val filename = "${externalCacheDir.absolutePath}/test_record.wav"

        val f = File(inputAudioFile)
        try {
            val asset = assets.open("audio.wav")
            //val asset = assets.open("test_record.wav")
            val size = asset.available()
            val buffer = ByteArray(size)
            asset.read(buffer)
            asset.close()

            val fos = FileOutputStream(f)
            fos.write(buffer)
            fos.close()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        this.wis = WAVInputStream(inputAudioFile)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = requestCode == REQUEST_RECORD_AUDIO_PERMISSION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (!permissionToRecordAccepted) finish()
    }

    fun recordAudio(view: View) {
        if (recordingAudio)
        {
            audioRecorder.stopRecording()
            recordingAudio = false
            (view as Button).setText(R.string.record_audio)
            this.convertAudio()
        }
        else
        {
            audioRecorder.startRecording()
            recordingAudio = true
            (view as Button).setText(R.string.stop_recording)
        }
    }

    fun analyzeAudio(view: View)
    {
        if (recordingAudio) {
            audioDispatcher.stop()
            audioThread.join()
            recordingAudio = false

            runOnUiThread {
                tv_rec.text = "Not recording!"

                val tv_19: TextView = findViewById<TextView>(R.id.textView_19) as TextView
                val tv_20: TextView = findViewById<TextView>(R.id.textView_20) as TextView
                val tv_21: TextView = findViewById<TextView>(R.id.textView_21) as TextView
                tv_19.setText("15 Hz\n0")
                tv_20.setText("20 Hz\n0")
                tv_21.setText("25 Hz\n0")

                val tv_result: TextView = findViewById(R.id.tv_result)
                try {
                    val text = this.attc.text
                    tv_result.setText("Decoded message: " + text)
                    Log.i("MainActivity", "Decoded message: " + text)

                    val sb: StringBuilder = StringBuilder()
                    for (b in text.toByteArray()) {
                        sb.append(b)
                        sb.append(" ")
                    }
                    Log.i("MainActivity", "Byte array: " + sb.toString())
                } catch (e: Exception) {
                    Log.e("MainActivity", "No text found", e)
                    tv_result.setText("Decoded message: No text found!")
                }
            }
        } else {
            val gg: GeneralizedGoertzel
            val buffer = 2048

            val timespan = findViewById<EditText>(R.id.et_timespan).text.toString().toInt()
            val magStart = findViewById<EditText>(R.id.et_magStart).text.toString().toDouble()

            val pattern = this.getPattern()
            val apr_start = AudioPatternRecognizer(pattern, timespan, magStart)
            val apr_stop = AudioPatternRecognizer(pattern, timespan, 0.0)

            /*
             * power[0] => 50Hz => 0
             * power[1] => 70Hz => 1
             */
            var skipedSamples = 4
            val fdh = Goertzel.FrequenciesDetectedHandler { timestampSec, frequencies, powers, allFrequencies, allPowers ->
                if(skipedSamples > 0)
                    skipedSamples--
                else
                {
                    var timestamp = (timestampSec * 1000).toLong()

                    var bit = "0"
                    if(powers[0] < powers[1])
                        bit = "1"
                    Log.i("MainActivity", "[" + timestamp + "]: power[0] = " + powers[0] + "; power[1] = " + powers[1] + " => " + bit)
                    if (!apr_start.recognized.get())
                    {
                        apr_start.update(timestamp, powers[0], powers[1])
                        if(apr_start.recognized.get())
                        {
                            this.attc = AudioToTextConverter(timespan, timestamp, pattern.size)
                            this.attc.update(timestamp, powers[0], powers[1])
                        }
                    }
                    else if (!apr_stop.recognized.get())
                    {
                        apr_stop.update(timestamp, powers[0], powers[1])
                        if (!apr_stop.recognized.get())
                        {
                            attc.update(timestamp, powers[0], powers[1])
                            runOnUiThread {
                                tv_rec.text = "Recording...  Time processed: " + timestamp + "s"
                            }
                        }
                        else
                        {
                            runOnUiThread {
                                findViewById<Button>(R.id.btn_analyze_audio).performClick()
                            }
                        }
                    }
                }
            }

            //Analyse file
            if (findViewById<Switch>(R.id.file_mic_switch).isChecked)
            {
                audioDispatcher = AudioDispatcher(this.wis.inputStream, buffer, buffer / 2)
                gg = GeneralizedGoertzel(this.wis.sampleRate, buffer, doubleArrayOf(50.0, 70.0), fdh)

                audioDispatcher.addAudioProcessor(gg)

                audioThread = Thread(audioDispatcher, "Audio Thread")
                audioThread.start()

                recordingAudio = true
                runOnUiThread {
                    tv_rec.text = "Recording: Waiting for signal..."
                    val tv_result: TextView = findViewById(R.id.tv_result)
                    tv_result.text = ""
                }
            }
            else
            {
                //this.recordAudio(view)
                /*audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, buffer, buffer / 2)
                gg = GeneralizedGoertzel(44100f, buffer, doubleArrayOf(50.0, 70.0), fdh)

                audioDispatcher.addAudioProcessor(
                    GeneralizedGoertzel(
                        44100f,
                        buffer,
                        doubleArrayOf(30.0, 50.0, 70.0),
                        Goertzel.FrequenciesDetectedHandler { timestamp, frequencies, powers, allFrequencies, allPowers ->
                            runOnUiThread {
                                val tv_19: TextView = findViewById<TextView>(R.id.textView_19) as TextView
                                val tv_20: TextView = findViewById<TextView>(R.id.textView_20) as TextView
                                val tv_21: TextView = findViewById<TextView>(R.id.textView_21) as TextView
                                tv_19.setText(frequencies[0].toString() + " Hz\n" + powers[0])
                                tv_20.setText(frequencies[1].toString() + " Hz\n" + powers[1])
                                tv_21.setText(frequencies[2].toString() + " Hz\n" + powers[2])

                                if(powers[1] > powers[2])
                                {
                                    tv_20.setTextColor(Color.RED)
                                    tv_21.setTextColor(tv_19.currentTextColor)
                                }
                                else
                                {
                                    tv_20.setTextColor(tv_19.currentTextColor)
                                    tv_21.setTextColor(Color.RED)
                                }
                            }
                        })
                )*/
            }
        }
    }

    fun convertAudio()
    {
        tv_rec.text = "Converting..."
        btn_rec.isEnabled = false

        object : Thread()
        {
            override fun run()
            {
                if(audioConverter.convert(recAudioFile, inputAudioFile))
                    runOnUiThread {
                        tv_rec.text = "Conversion successful"
                        analyzeAudio(findViewById(R.id.btn_record))
                    }
                else
                    runOnUiThread { tv_rec.text = "Conversion failed!" }
                runOnUiThread { btn_rec.isEnabled = true }
            }
        }.start()
    }

    fun getPattern(): BooleanArray {
        val et: EditText = findViewById(R.id.et_pattern)
        val pattern = et.text.toString().toCharArray()

        val pArray = BooleanArray(pattern.size)
        for (i in 0..pArray.size - 1) {
            if (pattern[i] == '1')
                pArray[i] = true
            else if (pattern[i] == '0')
                pArray[i] = false
            else {
                runOnUiThread {
                    val tv_result: TextView = findViewById(R.id.tv_result)
                    tv_result.setText("Invalid pattern. Use only '0' and '1'!")
                }
                break
            }
        }

        return pArray
    }

    fun onUpload(view: View)
    {
        val intent = Intent()
        intent.setClass(this, TransActivity::class.java)
        startActivity(intent)
    }

    fun onSwitch(view: View) {
        if (this.showMotionLayout) {
            findViewById<ConstraintLayout>(R.id.cl_motion).visibility = GONE
            findViewById<ConstraintLayout>(R.id.cl_audio).visibility = VISIBLE
            findViewById<Button>(R.id.bt_switchView).setText("Switch to motion analysis")
        } else {
            findViewById<ConstraintLayout>(R.id.cl_motion).visibility = VISIBLE
            findViewById<ConstraintLayout>(R.id.cl_audio).visibility = GONE
            findViewById<Button>(R.id.bt_switchView).setText("Switch to audio analysis")
        }
        this.showMotionLayout = !this.showMotionLayout
    }

    fun recordMotionData(view: View) {
        if (recordingMotionData) {
            sensorRecorder.stopRecording()
            recordingMotionData = false
            (view as Button).setText(R.string.record_motion)
        } else {
            sensorRecorder.onAccelerometerChanged = fun(x, y, z) {
                findViewById<TextView>(R.id.txtAccelX).setText("%+.3f+".format(x))
                findViewById<TextView>(R.id.txtAccelY).setText("%+.3f+".format(y))
                findViewById<TextView>(R.id.txtAccelZ).setText("%+.3f+".format(z))
            }
            sensorRecorder.onGyroscopeChanged = fun(x, y, z) {
                findViewById<TextView>(R.id.txtGyroX).setText("%+.3f".format(x))
                findViewById<TextView>(R.id.txtGyroY).setText("%+.3f".format(y))
                findViewById<TextView>(R.id.txtGyroZ).setText("%+.3f".format(z))
            }
            sensorAnalyzer.reset()
            sensorRecorder.startRecording(this)
            recordingMotionData = true
            (view as Button).setText(R.string.stop_recording)
        }
    }

    fun playbackMotionDataFile(view: View) {
        Log.i(LOG_TAG, "starting motion data playback")

        sensorAnalyzer.reset()

        // read vendored CSV file from assets
        //val asset = assets.open("accelerometer-fake-data-101011100010.csv")
        val asset = assets.open("accelerometer-data-motiondata-36Hz-krk-conferenceroompult-20190206-1809.csv")
        val csv = asset.bufferedReader().use(BufferedReader::readText)
        asset.close()

        // decode CSV data
        for (line in csv.split("\n")) {
            if (line.equals("")) continue

            val fields = line.split(";")
            val x = fields[1].toFloat()
            val y = fields[2].toFloat()
            val z = fields[3].toFloat()

            // fake a sensor event (timing is actually unimportant here)
            sensorAnalyzer.onAccelerometerChanged(0, x, y, z)
        }
    }
}
