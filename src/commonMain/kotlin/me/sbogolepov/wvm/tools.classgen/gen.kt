package me.sbogolepov.wvm.tools.classgen

import me.sbogolepov.wvm.raw.*

fun main() {
    println(generateFloatCmpClasses().joinToString("\n"))
    println(generateFloatCmpParser().joinToString(("\n")))
}