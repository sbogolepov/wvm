package me.sbogolepov.wvm.io

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
    private val byteBuffer = ByteBuffer.wrap(stream.readBytes()).apply {
        order(when (endianness) {
            Endianness.LITTLE -> ByteOrder.LITTLE_ENDIAN
            Endianness.BIG -> ByteOrder.BIG_ENDIAN
        })
    }.also {
        println()
    }

    override fun readByte(): Byte =
        byteBuffer.get()


    override fun readUInt8(): UByte =
        byteBuffer.get().toUByte()

    override fun readUInt16(): UShort =
        byteBuffer.short.toUShort()

    override fun readUInt32(): UInt =
        byteBuffer.int.toUInt()

    override fun readFloat32(): Float =
        byteBuffer.float

    override fun readFloat64(): Double =
        byteBuffer.double

    override fun peek(): Byte {
        return byteBuffer.get(byteBuffer.position())
    }
}
