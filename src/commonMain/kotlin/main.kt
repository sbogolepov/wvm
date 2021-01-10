import me.sbogolepov.wvm.exec.Interpreter
import me.sbogolepov.wvm.exec.instantiate
import me.sbogolepov.wvm.io.*
import me.sbogolepov.wvm.parser.ParserGenerator
import me.sbogolepov.wvm.parser.module
import okio.Path.Companion.toPath

fun main(args: Array<String>) {
    val reader: RawDataReader = OkioRawDataReader(args[0].toPath())
    val parser = ParserGenerator(reader)
    val module = parser.module()

    val interpreter = Interpreter()
    interpreter.instantiate(module, "main")
}