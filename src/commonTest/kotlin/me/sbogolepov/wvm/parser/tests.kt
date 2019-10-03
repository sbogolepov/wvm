package me.sbogolepov.wvm.parser

import me.sbogolepov.wvm.io.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class Tests {
    @Test
    fun test() {
        val path = "/Users/jetbrains/src/wvm/src/commonTest/resources/trivial.wasm"
        val handle = when (val result = openFile(path, FileReadMode.RB)) {
            is FileOpenResult.Error -> fail("IO failed: ${result.message}")
            is FileOpenResult.Ok -> result.handle
        }
        val reader: RawDataReader = createRawDataReader(handle)
        reader.magic().forEachIndexed { idx, value ->
            assertEquals(WASM_MAGIC[idx], value)
        }
        assertEquals(1u,reader.wasmVersion())
    }
}