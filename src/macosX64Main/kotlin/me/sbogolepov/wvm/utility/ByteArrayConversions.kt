package me.sbogolepov.wvm.utility

actual fun ByteArray.getInt(): Int = getIntAt(0)

actual fun ByteArray.getLong(): Long = getLongAt(0)

actual fun ByteArray.getFloat(): Float = getFloatAt(0)

actual fun ByteArray.getDouble(): Double = getDoubleAt(0)
