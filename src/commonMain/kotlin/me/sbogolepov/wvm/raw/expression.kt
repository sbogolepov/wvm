package me.sbogolepov.wvm.raw

interface Expression

interface ConstantExpression: Expression

class InstructionSeq(val instructions: Array<Instruction>) : Expression