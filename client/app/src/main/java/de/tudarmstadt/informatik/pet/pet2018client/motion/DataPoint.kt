package de.tudarmstadt.informatik.pet.pet2018client.motion

import kotlin.math.absoluteValue

class DataPoint(val x: Float, val y: Float, val z: Float) {

    private val GRAVITY: Float = 9.81f
    private val GRAVITY_AXIS_DETECTION_THRESHOLD: Float = 2f // tuning parameter!

    fun highestDistortionAbsolute(): Float {
        // find axis with highest distortion from normal value (either GRAVITY or 0 => assuming the device is
        // positioned orthogonal to one of the axis!)
        val distortionX = if(isGravityAxis(x)) (x - GRAVITY).absoluteValue else x.absoluteValue
        val distortionY = if(isGravityAxis(y)) (y - GRAVITY).absoluteValue else y.absoluteValue
        val distortionZ = if(isGravityAxis(z)) (z - GRAVITY).absoluteValue else z.absoluteValue

        // return the maximum distortion
        return maxOf(distortionX, distortionY, distortionZ)
    }

    private fun isGravityAxis(value: Float): Boolean {
        // if the absolute distance between the value and GRAVITY is within the detection threshold, assume this
        // is the axis affected by gravity
        return (value - GRAVITY).absoluteValue < GRAVITY_AXIS_DETECTION_THRESHOLD
    }
}