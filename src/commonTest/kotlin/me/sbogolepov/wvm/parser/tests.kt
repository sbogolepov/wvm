package me.sbogolepov.wvm.parser

import me.sbogolepov.wvm.io.*
import me.sbogolepov.wvm.parser.generator.TypeSection
import me.sbogolepov.wvm.parser.generator.ValueType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class Tests {
    @ExperimentalStdlibApi
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
        val (sectionHeader, _) = reader.sectionHeader()
        val (section, _) = reader.sectionByHeader(sectionHeader)
        assertTrue { section is TypeSection }
        val typeSection = section as TypeSection
        assertEquals(1, typeSection.types.size)
        assertEquals(1, typeSection.types[0].results.size)
        assertEquals(ValueType.I32, typeSection.types[0].results[0])
    }
}