package me.sbogolepov.wvm.io

import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestPrimitiveIO {
    @Test
    fun ioTest() {
        val path = "/Users/sergey.bogolepov/wvm/src/commonTest/resources/trivial_binary.bin"
        val reader: RawDataReader = OkioRawDataReader(path.toPath())
        assertEquals(3.14f, reader.readFloat32())
    }
}