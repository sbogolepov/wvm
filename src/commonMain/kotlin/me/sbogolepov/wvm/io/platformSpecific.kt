package me.sbogolepov.wvm.io

expect class FileHandle

sealed class FileOpenResult {
    class Error(val message: String): FileOpenResult()
    class Ok(val handle: FileHandle): FileOpenResult()
}

enum class FileReadMode {
    R, RB
}

expect fun openFile(path: String, readMode: FileReadMode): FileOpenResult

expect fun printError(message: String)

expect fun createRawDataReader(handle: FileHandle, endianness: Endianness = Endianness.LITTLE): RawDataReader
