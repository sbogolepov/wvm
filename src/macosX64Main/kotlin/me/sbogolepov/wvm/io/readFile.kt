package me.sbogolepov.wvm.io

import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.Foundation.NSFileHandle
import platform.Foundation.fileHandleForReadingAtPath
import platform.posix.fprintf
import platform.posix.stderr

actual class FileHandle(val nsFileHandle: NSFileHandle)

actual fun openFile(path: String, readMode: FileReadMode): FileOpenResult {
    val handle = NSFileHandle.fileHandleForReadingAtPath(path)
    return if (handle == null) {
        FileOpenResult.Error("Cannot open file at $path")
    } else {
        FileOpenResult.Ok(FileHandle(handle))
    }
}

actual class RawData(val nsData: NSData)

@ExperimentalUnsignedTypes
// TODO: Not only file endianness matters, but also system's.
class NativeRawDataReader(rawData: RawData, override val endianness: Endianness = Endianness.LITTLE) : RawDataReader {

    private val bytesStart: CPointer<ByteVar> = rawData.nsData.bytes as CPointer<ByteVar>

    private var offset: Long = 0L

    override fun readByte(): Byte {
        return bytesStart[offset++]
    }

    override fun readUInt8(): UByte = readByte().toUByte()

    override fun readUInt16(): UShort =
        readBytes(2) { (it as CArrayPointer<ShortVar>)[0].toUShort() }

    override fun readUInt32(): UInt =
        readBytes(4) { (it as CArrayPointer<IntVar>)[0].toUInt() }

    override fun readFloat32(): Float =
        readBytes(4) { (it as CArrayPointer<FloatVar>)[0] }

    override fun readFloat64(): Double =
        readBytes(8) { (it as CArrayPointer<DoubleVar>)[0] }

    private inline fun <T> readBytes(n: Int, conversion: (CArrayPointer<ByteVar>) -> T): T = memScoped {
        val bytes = allocArray<ByteVar>(n)
        for (i in 0 until n) {
            val memAddress = when (endianness) {
                Endianness.BIG -> n - 1 - i
                Endianness.LITTLE -> i
            }
            bytes[i] = (bytesStart + offset)!![memAddress]
        }
        offset += n
        conversion(bytes)
    }

    override fun peek(): Byte {
        return bytesStart[offset]
    }
}

fun FileHandle.readAll(): RawData {
    val data = nsFileHandle.readDataToEndOfFile()
    return RawData(data)
}

actual fun printError(message: String) {
    fprintf(stderr, "%s\n", message)
}

actual fun createRawDataReader(handle: FileHandle, endianness: Endianness): RawDataReader {
    return NativeRawDataReader(handle.readAll(), endianness)
}