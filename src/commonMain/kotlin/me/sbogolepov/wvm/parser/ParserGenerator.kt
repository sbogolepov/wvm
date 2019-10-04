package me.sbogolepov.wvm.parser

import me.sbogolepov.wvm.io.RawDataReader
import me.sbogolepov.wvm.io.rawDataReaderFrom
import me.sbogolepov.wvm.raw.Instruction
import kotlin.experimental.and

data class ParseResult<out T>(val data: T, val bytesRead: Int)

abstract class AParser<T> : Parser<T>, ByteCountingParser {

    override var bytesRead = 0

    abstract operator fun invoke(): T

    override fun result(): ParseResult<T> {
        return ParseResult(invoke(), bytesRead)
    }
}

inline fun <reified T> parser(crossinline fn: AParser<T>.() -> T): AParser<T> = object : AParser<T>() {
    override fun invoke(): T {
        return fn()
    }
}

class ParserGenerator(val rawDataReader: RawDataReader) {

    val unsignedLeb128 = parser<ULong> {
        var result = 0uL
        var shift = 0
        do {
            val byte = +rawDataReader.byte
            val value: UInt = (byte and 0x7f).toUInt()
            result += (value shl shift)
            shift += 7
        } while (byte and 0x80.toByte() == 0x80.toByte())
        result
    }

    val signedLeb128 = parser<Long> {
        var result = 0L
        var shift = 0
        var byte: Byte
        do {
            byte = +rawDataReader.byte
            val value = byte.toInt() and 0x7f
            result += (value shl shift)
            shift += 7
        } while (byte and 0x80.toByte() == 0x80.toByte())
        if (shift < Long.SIZE_BITS && (byte and 0x40.toByte() == 0x40.toByte())) {
            result = result or (0L.inv() shl shift)
        }
        result
    }

    val u32 = parser<UInt> {
        (+unsignedLeb128).toUInt()
    }

    val byte: Parser<Byte> = rawDataReader.byte

    inline fun <reified T> vector(element: Parser<T>) = parser<Array<T>> {
        val num = +u32
        Array(num.toInt()) { +element }
    }

    inline fun <reified T> parseWhile(
        action: AParser<T>,
        crossinline condition: (Byte) -> Boolean
    ): Parser<List<T>> = parser {
        val data = mutableListOf<T>()
        while (!condition(peek())) {
            data += (+action)
        }
        eat()
        data.toList()
    }

    fun peek(): Byte = rawDataReader.peek()

    fun eat(n: Int = 1) = repeat(n) { rawDataReader.readByte() }
}

interface Parser<T> {

    fun result(): ParseResult<T>
}

interface ByteCountingParser {
    var bytesRead: Int

    operator fun <G> Parser<G>.unaryPlus(): G {
        val (data, bytesRead1) = this.result()
        bytesRead += bytesRead1
        return data
    }
}

val RawDataReader.byte: Parser<Byte>
    get() = object : Parser<Byte> {
        override fun result() = ParseResult(readByte(), 1)
    }

val RawDataReader.float: Parser<Float>
    get() = object : Parser<Float> {
        override fun result() = ParseResult(readFloat32(), 4)
    }