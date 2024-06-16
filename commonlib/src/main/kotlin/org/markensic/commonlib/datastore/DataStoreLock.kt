package org.markensic.commonlib.datastore

import java.util.concurrent.locks.ReentrantReadWriteLock

val sLock = ReentrantReadWriteLock()

inline fun <R> lockWrite(func: () -> R?): R? {
  return lockWrite(func, null)
}

inline fun <R> lockWrite(func: () -> R?, exceptionCallback: ExceptionCallback<R>?): R? {
  return try {
    sLock.writeLock().lock()
    func()
  } catch (e: Exception) {
    exceptionCallback?.callException(e)
  } finally {
    sLock.writeLock().unlock()
  }
}

inline fun <R> lockRead(func: () -> R?): R? {
  return lockRead(func, null)
}

inline fun <R> lockRead(func: () -> R?, exceptionCallback: ExceptionCallback<R>?): R? {
  return try {
    sLock.readLock().lock()
    func()
  } catch (e: Exception) {
    exceptionCallback?.callException(e)
  } finally {
    sLock.readLock().unlock()
  }
}

inline fun <R1, R2> lockReadAndWrite(
  readFunc: () -> R1?,
  writeFunc: (R1?) -> R2?
): R2? {
  return lockReadAndWrite(readFunc, writeFunc, null)
}

inline fun <R1, R2> lockReadAndWrite(
  readFunc: () -> R1?,
  writeFunc: (R1?) -> R2?,
  exceptionCallback: ExceptionCallback<R2>?
): R2? {
  var r1: R1? = null
  var r2: R2? = null
  val s = try {
    sLock.readLock().lock()
    r1 = readFunc()
    sLock.readLock().unlock()
    try {
      sLock.writeLock().lock()
      r2 = writeFunc(r1)
      sLock.readLock().lock()
    } finally {
      sLock.writeLock().unlock()
    }
  } catch (e: Exception) {
    r2 = exceptionCallback?.callException(e)
  } finally {
    sLock.readLock().unlock()
  }
  return r2
}

interface ExceptionCallback<R> {
  fun callException(tr: Throwable): R
}
