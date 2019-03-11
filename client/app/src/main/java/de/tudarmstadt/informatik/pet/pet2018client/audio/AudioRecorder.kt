package de.tudarmstadt.informatik.pet.pet2018client.audio

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorder (private val mFilename: String) {

    private val LOG_TAG = "AudioRecorder"

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null

    fun stopRecording() {
        mRecorder?.stop()
        mRecorder?.release()
        mRecorder = null
        Log.i(LOG_TAG, "stop recording")
    }

    fun startRecording() {
        mRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.UNPROCESSED)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(mFilename)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
            start()
        }
        Log.i(LOG_TAG, "recording audio")
    }

    fun play() {
        mPlayer = MediaPlayer().apply {
            try {
                setDataSource(mFilename)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }
}

