import me.sbogolepov.wvm.exec.Interpreter
import me.sbogolepov.wvm.exec.instantiate
import me.sbogolepov.wvm.io.*
import me.sbogolepov.wvm.parser.ParserGenerator
import me.sbogolepov.wvm.parser.module

fun main(args: Array<String>) {
    // TODO: use kotlinx.cli after switch 1.3.60
    val handle = when (val readResult = openFile(args[0], FileReadMode.RB)) {
        is FileOpenResult.Ok -> {
            readResult.handle
        }
        is FileOpenResult.Error -> {
            printError(readResult.message)
            return
        }
    }
    val reader: RawDataReader = createRawDataReader(handle)
    val parser = ParserGenerator(reader)
    val module = parser.module()

    val interpreter = Interpreter()
    interpreter.instantiate(module, "main")
}