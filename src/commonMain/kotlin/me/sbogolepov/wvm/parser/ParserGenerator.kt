package me.sbogolepov.wvm.parser

import me.sbogolepov.wvm.io.RawDataReader
import kotlin.experimental.and

data class ParseResult<out T>(val data: T, val bytesRead: Int)

// TODO: What if parser is called several times?
abstract class AParser<T> : Parser<T> {

    var bytesRead = 0
        private set

    abstract operator fun invoke(): T

    override fun result(): ParseResult<T> {
        return ParseResult(invoke(), bytesRead)
    }

    operator fun <G> Parser<G>.unaryPlus(): G {
        val (data, bytesRead1) = this.result()
        bytesRead += bytesRead1
        return data
    }
}

inline fun <reified T> parser(crossinline fn: AParser<T>.() -> T): AParser<T> = object : AParser<T>() {
    override fun invoke(): T {
        return fn()
    }
}

class ParserGenerator(val rawDataReader: RawDataReader) {

    val unsignedLeb128 get() = parser<ULong> {
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

    val signedLeb128 get() = parser<Long> {
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

    val u32 get() = parser<UInt> {
        (+unsignedLeb128).toUInt()
    }

    val i32 get() = parser<Int> {
        (+signedLeb128).toInt()
    }

    val i64 get() = parser<Long> {
        +signedLeb128
    }

    val f32 get() = rawDataReader.float

    val f64 get() = rawDataReader.double

    val byte get() = rawDataReader.byte

    inline fun <reified T> vector(element: Parser<T>) = parser<Array<T>> {
        val num = +u32
        Array(num.toInt()) { +element }
    }

    inline fun <reified T> parseWhile(
        action: AParser<T>,
        crossinline condition: (Byte?) -> Boolean
    ): Parser<List<T>> = parser {
        val data = mutableListOf<T>()
        while (condition(peek())) {
            data += (+action)
        }
        eat()
        data.toList()
    }

    fun peek(): Byte? = rawDataReader.peek()

    fun eat(n: Int = 1) = repeat(n) { rawDataReader.readByte() }
}

interface Parser<T> {

    fun result(): ParseResult<T>
}

val RawDataReader.byte: Parser<Byte>
    get() = object : Parser<Byte> {
        override fun result() = ParseResult(readByte(), 1)
    }

val RawDataReader.float: Parser<Float>
    get() = object : Parser<Float> {
        override fun result() = ParseResult(readFloat32(), 4)
    }

val RawDataReader.double: Parser<Double>
    get() = object : Parser<Double> {
        override fun result() = ParseResult(readFloat64(), 8)
    }