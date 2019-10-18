package me.sbogolepov.wvm.utility

import java.nio.ByteBuffer

actual fun ByteArray.getLong(): Long = ByteBuffer.wrap(this).long

actual fun ByteArray.getFloat(): Float = ByteBuffer.wrap(this).float

actual fun ByteArray.getDouble(): Double = ByteBuffer.wrap(this).double

actual fun ByteArray.getInt(): Int = ByteBuffer.wrap(this).int