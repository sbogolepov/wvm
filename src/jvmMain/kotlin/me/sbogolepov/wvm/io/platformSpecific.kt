package me.sbogolepov.wvm.io

import sun.misc.IOUtils
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths

actual class FileHandle(val file: File)

actual fun openFile(
    path: String,
    readMode: FileReadMode
): FileOpenResult {
    val path1 = Paths.get(path)
    return if (Files.notExists(path1)) {
        FileOpenResult.Error("File not exists")
    } else {
        FileOpenResult.Ok(FileHandle(File(path)))
    }
}

actual fun printError(message: String) {
    System.err.println(message)
}

actual fun createRawDataReader(handle: FileHandle, endianness: Endianness): RawDataReader {
    return JvmRawDataReader(handle.file, endianness)
}

@ExperimentalUnsignedTypes
class JvmRawDataReader(val file: File, override val endianness: Endianness) : RawDataReader {

    private val stream = FileInputStream(file)
    private val dataStream = ByteBuffer.wrap(stream.readBytes()).apply {
        order(when (endianness) {
            Endianness.LITTLE -> ByteOrder.LITTLE_ENDIAN
            Endianness.BIG -> ByteOrder.BIG_ENDIAN
        })
    }.also {
        println()
    }

    override fun readByte(): Byte =
        dataStream.get()


    override fun readUInt8(): UByte =
        dataStream.get().toUByte()

    override fun readUInt16(): UShort =
        dataStream.short.toUShort()

    override fun readUInt32(): UInt =
        dataStream.int.toUInt()

    override fun readFloat32(): Float =
        dataStream.float

    override fun readFloat64(): Double =
        dataStream.double

}

    String(this, Charsets.UTF_8)
