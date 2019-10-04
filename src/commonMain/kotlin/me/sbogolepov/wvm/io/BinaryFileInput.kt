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

    fun peek(): Byte
}

fun rawDataReaderFrom(array: ByteArray): RawDataReader = object : RawDataReader {

    var offset = 0

    override val endianness: Endianness
        get() = TODO("not implemented")

    override fun readByte(): Byte {
        return array[offset++]
    }

    override fun readUInt8(): UByte {
        TODO("not implemented")
    }

    override fun readUInt16(): UShort {
        TODO("not implemented")
    }

    override fun readUInt32(): UInt {
        TODO("not implemented")
    }

    override fun readFloat32(): Float {
        TODO("not implemented")
    }

    override fun readFloat64(): Double {
        TODO("not implemented")
    }

    override fun peek(): Byte {
        return array[offset]
    }
}

fun RawDataReader.readBytes(n: Int): ByteArray =
    ByteArray(n) { readByte() }

fun RawDataReader.byte(): ParseResult<Byte> = ParseResult(readByte(), 1)

