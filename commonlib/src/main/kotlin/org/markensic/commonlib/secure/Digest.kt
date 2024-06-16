package org.markensic.commonlib.secure

import java.math.BigInteger
import java.security.MessageDigest

const val HEX_RADIX = 16
const val DEC_RADIX = 10
const val OCT_RADIX = 8
const val BIN_RADIX = 2

const val ABOVE_ZERO = 1
const val ZERO = 0
const val UNDER_ZERO = -1

const val MD5 = "MD5"

fun String.md5(): String {
  return this.toByteArray().md5()
}

fun ByteArray.md5(): String {
  val md5 = MessageDigest.getInstance(MD5)
  val paddingChar = '0'
  val length = 32
  return BigInteger(ABOVE_ZERO, md5.digest(this)).toString(HEX_RADIX)
    .padStart(length, paddingChar).uppercase()
}
