package me.sbogolepov.wvm.io

import me.sbogolepov.wvm.parser.ParserGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail


class TestLeb128 {
    @Test
    fun testUnsignedLeb128() {
        val path = "/Users/jetbrains/src/wvm/src/commonTest/resources/trivial_leb128_unsigned.bin"
        val handle = when (val result = openFile(path, FileReadMode.RB)) {
            is FileOpenResult.Error -> fail("IO failed: ${result.message}")
            is FileOpenResult.Ok -> result.handle
        }
        val parser = ParserGenerator(createRawDataReader(handle))
        val uLong = parser.unsignedLeb128()
        assertEquals(624485uL, uLong)
    }

    @Test
    fun testSignedLeb128() {
        val path = "/Users/jetbrains/src/wvm/src/commonTest/resources/trivial_leb128_signed.bin"
        val handle = when (val result = openFile(path, FileReadMode.RB)) {
            is FileOpenResult.Error -> fail("IO failed: ${result.message}")
            is FileOpenResult.Ok -> result.handle
        }
        val parser = ParserGenerator(createRawDataReader(handle))
        val value = parser.signedLeb128()
        assertEquals(-123456L, value)
    }
}