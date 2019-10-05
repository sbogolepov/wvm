package me.sbogolepov.wvm.tools.classgen

import me.sbogolepov.wvm.raw.*

fun main() {
    println(conversionsClasses.joinToString("\n"))
    println(conversionsParser.joinToString(("\n")))
}