package de.tudarmstadt.informatik.pet.pet2018client.motion

import android.util.Log
import de.tudarmstadt.informatik.pet.pet2018client.util.AsciiCodec
import de.tudarmstadt.informatik.pet.pet2018client.util.DecodedSymbol
import de.tudarmstadt.informatik.pet.pet2018client.util.RingBuffer
import de.tudarmstadt.informatik.pet.pet2018client.util.WrapCallback

class SensorAnalyzer: WrapCallback {

    private val LOG_TAG = "SensorAnalyzer"

    private val SAMPLING_RATE = 200 //180 // realtime: 200                  // per second

    private val INITIAL_PREAMBLE_THRESHOLD = 0.19   // tuning parameter! (depending on the table setup!)

    private val SYMBOL_LENGTH = 1.5 //3 // realtime: 1.5                  // seconds. tuning parameter!
    private val DUTY_CYCLE_1 = 0.81  // old algorithm: 0.10  // 0.15  //0.65         // percentage. tuning parameter!
    private val DUTY_CYCLE_0 = 0.05 // old algorithm: 0.07  // 0.08   //0.07        // percentage. tuning parameter!

    private val PREAMBLE_SYMBOL_COUNT = 4    // times. tuning parameter! Preamble must actually contain
                                             // 1 symbol more for synchronization!
    private val PREAMBLE_AVERAGE_DECREASE_THRESHOLD = 0.935//0.935  // 0.90 // percentage. tuning parameter!

    private val L = (SYMBOL_LENGTH * SAMPLING_RATE).toInt()

    private val codec = AsciiCodec()

    private val dataBuffer: RingBuffer<DataPoint> = RingBuffer(L)
    private var dataCounter = 0
    private var lastTimestamp: Long = 0

    private var decoding = false
    private var synchronizing = false
    private var didSynchronizationReset = false
    private var averageBeforeSynchronization = 0f
    private var preambleCounter = 0
    private var nextProbeCounter = 0
    private var synchronizedOneLevel = 0f

    private var decodedBits = ArrayList<String>()

    init {
        dataBuffer.addCallback(this)
    }

    fun reset() {
        Log.i(LOG_TAG, "Resetting the sensor analyzer state")

        decodedBits = ArrayList()
        decoding = false
        synchronizing = false
        didSynchronizationReset = false
        averageBeforeSynchronization = 0f
        preambleCounter = 0
        nextProbeCounter = 0
        dataCounter = 0
        dataBuffer.reset(0)
        lastTimestamp = 0
        synchronizedOneLevel = 0f
    }

    fun onAccelerometerChanged(timestamp: Long, x: Float, y: Float, z: Float) {
        Log.v(LOG_TAG, "Enqueuing data point: x=" + x + ", y=" + y + ", z=" + z)

        dataBuffer.enqueue(DataPoint(x, y, z))
        dataCounter++

        // measure sampling rate for parameter tuning
        val period = (timestamp - lastTimestamp) / 1e9
        val samplingRate = 1/period
        lastTimestamp = timestamp
        Log.v(LOG_TAG, "Current sampling rate: " + samplingRate + " (timestamp is " + timestamp + ")")

        // when we are in the synchronization state, check whether the average decreases more than the threshold,
        // indicating the beginning of the zero symbol
        if (synchronizing && !didSynchronizationReset && nextProbeCounter > 10) {
            if (movingAverage() < averageBeforeSynchronization * PREAMBLE_AVERAGE_DECREASE_THRESHOLD) {
                // reset the buffer to start time-synchronized recording
                Log.d(LOG_TAG, "Resetting data buffer after seeing a decreasing moving average")
                Log.d(LOG_TAG, "Data index: " + dataCounter)
                dataBuffer.reset(0)
                didSynchronizationReset = true
            }
            nextProbeCounter = 0
        } else if (synchronizing) {
            nextProbeCounter++;
        }
    }

    fun movingAverage(): Float {
        var sum = 0f
        for (point: DataPoint in dataBuffer.elements) {
            // even if the compiler might say this is never null, it is!
            if (point != null) {
                sum += point.highestDistortionAbsolute()
            }
        }
        return sum / dataBuffer.elements.size
    }

    fun decodeSymbolFromBufferState(): DecodedSymbol {
        val threshold_1 = DUTY_CYCLE_1 * synchronizedOneLevel
        val threshold_0 = DUTY_CYCLE_0 * synchronizedOneLevel

        val avg = movingAverage()

        if (avg > threshold_1) {
            return DecodedSymbol.ONE
        } else if (avg > threshold_0) {
            return DecodedSymbol.ZERO
        } else {
            return DecodedSymbol.NOTHING
        }
    }

    fun getDecodedBitstring(): String {
        val builder = StringBuilder()
        var counter = 0
        decodedBits.forEach {
            builder.append(it)
            counter = (counter + 1) % 8
            if (counter == 0) {
                builder.append(" ")
            }
        }
        return builder.toString()
    }

    override fun onBufferWrap() {
        if (decoding) {
            val symbol = decodeSymbolFromBufferState()
            // synchronized to buffer lengths. Just add this symbol
            decodedBits.add(symbol.toString())
            Log.d(LOG_TAG, "Got symbol after buffer wrap: " + symbol.toString() + " (avg is " + movingAverage() + ")")
            Log.d(LOG_TAG, "Data index: " + dataCounter)
            Log.i(LOG_TAG, "Current message: " + getDecodedBitstring() + " - " + codec.decodeFromBitstring(decodedBits))
        }

        else if (synchronizing && didSynchronizationReset) {
            // this buffer wrap means that we got exactly one synchronized symbol. Is it actually nothing?
            if (movingAverage() < 0.5) {
                // start decoding!
                decoding = true
                synchronizing = false
                Log.i(LOG_TAG, "Finished synchronization using preamble, starting to decode!")
            } else {
                decoding = false
                synchronizing = false
                didSynchronizationReset = false
                preambleCounter = 0
                Log.i(LOG_TAG, "Finished synchronization, but didn't get a nothing!")
                Log.i(LOG_TAG, "(average: " + movingAverage() + ")")
            }
        }

        else if (!synchronizing) {
            // is this a ONE symbol?
            if (movingAverage() > INITIAL_PREAMBLE_THRESHOLD) {
                // increment the preamble counter
                preambleCounter++;
                Log.d(LOG_TAG, "Got ONE symbol for possible preamble. Counter state: " + preambleCounter)
                // finished enough one symbols?
                if (preambleCounter >= PREAMBLE_SYMBOL_COUNT) {
                    // received enough symbols! Start synchronization using exactly one zero symbol
                    synchronizing = true
                    didSynchronizationReset = false
                    averageBeforeSynchronization = movingAverage()
                    synchronizedOneLevel = movingAverage()
                    Log.i(LOG_TAG, "Detected preamble, moving over to synchronization phase!")
                }
            } else {
                // reset preamble
                decoding = false
                preambleCounter = 0
                Log.d(LOG_TAG, "Resetting preamble counting because no ONE symbol was received")
            }
        }
    }
}