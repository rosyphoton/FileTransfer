package de.tudarmstadt.informatik.pet.pet2018client

import de.tudarmstadt.informatik.pet.pet2018client.util.AsciiCodec
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AsciiCodecTest {

    lateinit var codec: AsciiCodec

    @Before
    fun setUp() {
        codec = AsciiCodec()
    }

    @Test
    fun bytesToString_isCorrect() {
        assertEquals("ab", codec.bytesToString(arrayListOf(97, 98)))
    }

    @Test
    fun bitstringToBytes_isCorrect() {
        assertEquals(arrayListOf(97, 98), codec.bitstringToBytes("0 1 1 0 0 0 0 1 0 1 1 0 0 0 1 0".split(" ")))
    }

    @Test
    fun decode_isCorrect() {
        assertEquals("ab", codec.decodeFromBitstring("0 1 1 0 0 0 0 1 0 1 1 0 0 0 1 0".split(" ")))
    }

    @Test
    fun decode_worksWithUnevenLength() {
        assertEquals("ab", codec.decodeFromBitstring("0 1 1 0 0 0 0 1 0 1 1 0 0 0 1 0 1 1 1".split(" ")))
    }

    @Test
    fun decode_raisesGapsToZeros() {
        assertEquals("a", codec.decodeFromBitstring(arrayListOf("0", "1", "1", "0", " ", "0", " ", "1")))
    }

    @Test
    fun errorCorrection_correctsBitErrorsToNearestChar() {
        assertEquals(0b01100001, codec.correctBitErrors(0b11100001))
        assertEquals(0b01100001, codec.correctBitErrors(0b00000000))
    }

    @Test
    fun errorCorrection_doesNotModifyCorrectChar() {
        assertEquals(0b01100001, codec.correctBitErrors(0b01100001))
    }
}