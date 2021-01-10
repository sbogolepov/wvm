package me.sbogolepov.wvm.tools.nm

import me.sbogolepov.wvm.io.*
import me.sbogolepov.wvm.parser.ParserGenerator
import me.sbogolepov.wvm.parser.generator.ExportSection
import me.sbogolepov.wvm.parser.module
import okio.Path.Companion.toPath

fun main(args: Array<String>) {
    val file = args[0]
    val reader: RawDataReader = OkioRawDataReader(file.toPath())
    val parser = ParserGenerator(reader)
    val module = parser.module()
    module.sections.filterIsInstance<ExportSection>().lastOrNull()?.exports?.forEach {
        println("D ${it.name}")
    }
}