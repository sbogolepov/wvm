package me.sbogolepov.wvm.io

import me.sbogolepov.wvm.parser.ParseResult
import okio.*

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

    fun peek(): Byte?
}

fun RawDataReader.readBytes(n: Int): ByteArray =
    ByteArray(n) { readByte() }

fun RawDataReader.byte(): ParseResult<Byte> = ParseResult(readByte(), 1)

class OkioRawDataReader(private val path: Path, override val endianness: Endianness = Endianness.LITTLE) : RawDataReader {

    private val source: BufferedSource = FileSystem.SYSTEM.source(path).buffer()

    override fun readByte(): Byte = source.readByte()

    override fun readUInt8(): UByte = source.readByte().toUByte()

    override fun readUInt16(): UShort = when (endianness) {
            Endianness.BIG -> source.readShort()
            Endianness.LITTLE -> source.readShortLe()
        }.toUShort()

    override fun readUInt32(): UInt = when (endianness) {
            Endianness.BIG -> source.readInt()
            Endianness.LITTLE -> source.readIntLe()
        }.toUInt()

    override fun readFloat32(): Float =
        Float.fromBits(source.readIntLe())

    override fun readFloat64(): Double =
        Double.fromBits(source.readLongLe())

    override fun peek(): Byte? =
        if (source.exhausted()) null
        else source.peek().readByte()
}

