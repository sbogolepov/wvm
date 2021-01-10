package me.sbogolepov.wvm.io

import me.sbogolepov.wvm.parser.ParserGenerator
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail


class TestLeb128 {
    @Test
    fun testUnsignedLeb128() {
        val path = "/Users/sergey.bogolepov/wvm/src/commonTest/resources/trivial_leb128_unsigned.bin"
        val parser = ParserGenerator(OkioRawDataReader(path.toPath()))
        val uLong = parser.unsignedLeb128()
        assertEquals(624485uL, uLong)
    }

    @Test
    fun testSignedLeb128() {
        val path = "/Users/sergey.bogolepov/wvm/src/commonTest/resources/trivial_leb128_signed.bin"
        val parser = ParserGenerator(OkioRawDataReader(path.toPath()))
        val value = parser.signedLeb128()
        assertEquals(-123456L, value)
    }
}