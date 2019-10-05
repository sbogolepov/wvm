package me.sbogolepov.wvm.tools.classgen

import me.sbogolepov.wvm.raw.generateMemOpClasses
import me.sbogolepov.wvm.raw.generateMemOpParser

fun main() {
    println(generateMemOpClasses().joinToString("\n"))
    println(generateMemOpParser().joinToString(("\n")))
}