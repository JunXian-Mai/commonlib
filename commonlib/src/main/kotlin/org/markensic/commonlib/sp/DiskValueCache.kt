package org.markensic.commonlib.sp

import java.util.concurrent.locks.ReentrantReadWriteLock

abstract class DiskValueCache<T> {
  private val lock = ReentrantReadWriteLock()
  private var _version = 0
  private var version = 0
  private val diskValue = initDiskValue()

  var value: T = diskValue.value
    set(value) {
      try {
        lock.writeLock().lock()
        diskValue.value = value
        field = value
        _version++
      } finally {
        lock.writeLock().unlock()
      }
    }
    get() {
      try {
        lock.readLock().lock()
        if (_version > version) {
          lock.readLock().unlock()
          lock.writeLock().lock()
          try {
            version = _version
            field = diskValue.value
            lock.readLock().lock()
          } finally {
            lock.writeLock().unlock()
          }
        }
        return field
      } finally {
        lock.readLock().unlock()
      }
    }

  fun beChange() {
    try {
      lock.writeLock().lock()
      _version++
    } finally {
      lock.writeLock().unlock()
    }
  }

  private fun initDiskValue() = implDiskValue()

  abstract fun implDiskValue(): DiskValue<T>

  interface DiskValue<P> {
    var value: P
  }
}
