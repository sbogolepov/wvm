import me.sbogolepov.wvm.io.FileOpenResult
import me.sbogolepov.wvm.io.FileReadMode
import me.sbogolepov.wvm.io.printError
import me.sbogolepov.wvm.io.openFile

expect fun fromPlatform(): String

fun main(args: Array<String>) {
    when (val readResult = openFile(args[0], FileReadMode.RB)) {
        is FileOpenResult.Ok -> {
            println("Ok ${readResult.handle}")
        }
        is FileOpenResult.Error -> {
            printError(readResult.message)
        }
    }
}