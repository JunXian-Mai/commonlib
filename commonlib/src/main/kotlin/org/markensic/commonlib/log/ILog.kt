package org.markensic.commonlib.log

import android.util.Log

interface ILog {
  companion object {
    const val VERBOSE = Log.VERBOSE
    const val DEBUG = Log.DEBUG
    const val INFO = Log.INFO
    const val WARN = Log.WARN
    const val ERROR = Log.ERROR
    const val ASSERT = Log.ASSERT
  }

  fun v(tag: String, msg: String, tr: Throwable? = null): Int

  fun d(tag: String, msg: String, tr: Throwable? = null): Int

  fun i(tag: String, msg: String, tr: Throwable? = null): Int

  fun w(tag: String, msg: String, tr: Throwable? = null): Int

  fun e(tag: String, msg: String, tr: Throwable? = null): Int
}
