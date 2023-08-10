package org.markensic.commonlib.thread

import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

class ThreadFactoryBuilder {
  private var nameFormat: String? = null
  private var daemon: Boolean? = null
  private var priority: Int? = null
  private var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
  private var backingThreadFactory: ThreadFactory? = null

  fun setNameFormat(nameFormat: String): ThreadFactoryBuilder {
    return apply {
      checkArgument(nameFormat.contains("%d"), "Thread nameFormat (%s) must be contains '%d'", nameFormat)
      this.nameFormat = nameFormat
    }
  }

  fun setDaemon(daemon: Boolean): ThreadFactoryBuilder {
    return apply {
      this.daemon = daemon
    }
  }

  fun setPriority(priority: Int): ThreadFactoryBuilder {
    return apply {
      checkArgument(priority >= 1, "Thread priority (%s) must be >= %s", priority, 1)
      checkArgument(priority <= 10, "Thread priority (%s) must be <= %s", priority, 10)
      this.priority = priority
    }
  }

  fun setUncaughtExceptionHandler(uncaughtExceptionHandler: Thread.UncaughtExceptionHandler): ThreadFactoryBuilder {
    return apply {
      this.uncaughtExceptionHandler = checkNotNull(uncaughtExceptionHandler)
    }
  }

  fun setThreadFactory(backingThreadFactory: ThreadFactory): ThreadFactoryBuilder {
    return apply {
      this.backingThreadFactory = checkNotNull(backingThreadFactory)
    }
  }

  fun build() = doBuild(this)

  private fun checkArgument(b: Boolean, errorMessageTemplate: String, vararg p: Any?) {
    if (!b) {
      throw IllegalArgumentException(String.format(errorMessageTemplate, p))
    }
  }

  private fun <T> checkNotNull(reference: T) = reference ?: throw NullPointerException()

  companion object {

    private fun doBuild(builder: ThreadFactoryBuilder): ThreadFactory {

      val backingThreadFactory = builder.backingThreadFactory ?: Executors.defaultThreadFactory()
      val count = AtomicLong(0L)

      return ThreadFactory { runnable ->
        backingThreadFactory.newThread(runnable).apply {
          builder.nameFormat?.let {
            name = format(it, count.getAndIncrement())
          }

          builder.daemon?.let {
            isDaemon = it
          }

          builder.priority?.let {
            priority = it
          }

          builder.uncaughtExceptionHandler?.let {
            uncaughtExceptionHandler = it
          }
        }
      }
    }

    private fun format(format: String, vararg args: Any): String {
      return String.format(Locale.ROOT, format, *args)
    }
  }
}
