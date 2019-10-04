package me.sbogolepov.wvm.raw

interface Expression

interface ConstantExpression: Expression

class ExpressionImpl(val instructions: Array<Instruction>) : Expression