package me.sbogolepov.wvm.io

import me.sbogolepov.wvm.parser.ParseResult

expect class RawData

enum class Endianness {
    BIG, LITTLE
}

// TODO: Add dispose
interface RawDataReader {
    val endianness: Endianness

    fun readByte(): Byte

    fun readUInt8(): UByte

    fun readUInt16(): UShort

    fun readUInt32(): UInt

    fun readFloat32(): Float

    fun readFloat64(): Double
}

fun RawDataReader.readBytes(n: Int): ByteArray =
    ByteArray(n) { readByte() }

fun RawDataReader.byte(): ParseResult<Byte> = ParseResult(readByte(), 1)

