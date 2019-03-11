package de.tudarmstadt.informatik.pet.pet2018client.util

class AsciiCodec {

    private val ALLOWED_CODES = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray().map { it.toInt() }

    fun decodeFromBitstring(bits: List<String>): String {
        val trimmedBits = bits.subList(0, bits.count() - bits.count() % 8)
        val raisedBits = trimmedBits.map { if (it.equals("1")) "1" else "0" }
        val bytes = bitstringToBytes(raisedBits)
        val correctedBytes = bytes.map { correctBitErrors(it) }
        return bytesToString(correctedBytes)
    }

    fun bitstringToBytes(bitstring: List<String>): List<Int> {
        return bitstring.chunked(8).map { it.joinToString(separator="").toInt(radix = 2) }
    }

    fun bytesToString(bytes: List<Int>): String {
        return bytes.map { it.toChar() }.joinToString(separator="")
    }

    fun correctBitErrors(received: Int): Int {
        if (ALLOWED_CODES.contains(received)) {
            return received
        } else {
            return 0 // TODO
        }
    }
}