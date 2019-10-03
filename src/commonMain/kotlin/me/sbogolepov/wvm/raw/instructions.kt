package me.sbogolepov.wvm.raw

sealed class Instruction
object Unreachable : Instruction()
object NOP : Instruction()
class Block()