package me.sbogolepov.wvm.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestPrimitiveIO {
    @Test
    fun ioTest() {
        // TODO: how to do it better?
        val path = "/Users/jetbrains/src/wvm/src/commonTest/resources/trivial_binary.bin"
        val handle = when (val result = openFile(path, FileReadMode.RB)) {
            is FileOpenResult.Error -> fail("IO failed: ${result.message}")
            is FileOpenResult.Ok -> result.handle
        }
        val reader: RawDataReader = createRawDataReader(handle)
        assertEquals(3.14f, reader.readFloat32())
    }
}