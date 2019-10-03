package me.sbogolepov.wvm.io

import me.sbogolepov.wvm.parser.ParseResult
import kotlin.experimental.and

@ExperimentalUnsignedTypes
fun RawDataReader.readUnsignedLeb128(): ParseResult<ULong> {
    var result = 0uL
    var shift = 0
    do {
        val byte = readByte()
        val value: UInt = (byte and 0x7f).toUInt()
        result += (value shl shift)
        shift += 7
    } while (byte and 0x80.toByte() == 0x80.toByte())
    return ParseResult(result, shift / 7)
}

fun RawDataReader.readSignedLeb128(): ParseResult<Long> {
    var result = 0L
    var shift = 0
    var byte: Byte
    do {
        byte = readByte()
        val value = byte.toInt() and 0x7f
        result += (value shl shift)
        shift += 7
    } while (byte and 0x80.toByte() == 0x80.toByte())
    if (shift < Long.SIZE_BITS && (byte and 0x40.toByte() == 0x40.toByte())) {
        result = result or (0L.inv() shl shift)
    }
    return ParseResult(result, shift / 7)
}

fun RawDataReader.u32(): ParseResult<UInt> = readUnsignedLeb128().let {
    ParseResult(it.data.toUInt(), it.bytesRead)
}
