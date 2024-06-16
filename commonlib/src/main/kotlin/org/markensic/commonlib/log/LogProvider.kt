package org.markensic.commonlib.log

object LogProvider: ILog {
  @Volatile
  private var _log: ILog? = null

  fun set(iLog: ILog) {
    synchronized(this::class) {
      _log = iLog
    }
  }

  override fun v(tag: String, msg: String, tr: Throwable?): Int {
    return _log?.v(tag, msg, tr) ?: -1
  }

  override fun d(tag: String, msg: String, tr: Throwable?): Int {
    return _log?.d(tag, msg, tr) ?: -1
  }

  override fun i(tag: String, msg: String, tr: Throwable?): Int {
    return _log?.i(tag, msg, tr) ?: -1
  }

  override fun w(tag: String, msg: String, tr: Throwable?): Int {
    return _log?.w(tag, msg, tr) ?: -1
  }

  override fun e(tag: String, msg: String, tr: Throwable?): Int {
    return _log?.e(tag, msg, tr) ?: -1
  }
}
