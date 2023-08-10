package org.markensic.commonlib.thread

import android.os.HandlerThread
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object ThreadPool {
  private var mainHandlerThread: HandlerThread? = null
  private val threadPoolMap: MutableMap<String, ModifyThreadPool> = mutableMapOf()

  val cpuCount: Int
    get() {
      return Runtime.getRuntime().availableProcessors()
    }



  private fun getThreadFactory(name: String) =
    ThreadFactoryBuilder().setNameFormat("$name-%d").build()

  fun createVariableThreadPool(
    poolName: String,
    corePoolSize: Int = cpuCount,
    maximumPoolSize: Int = cpuCount * 2,
    keepLive: Pair<Long, TimeUnit> = 60.toLong() to TimeUnit.SECONDS,
    reject: RejectedExecutionHandler = ThreadPoolExecutor.AbortPolicy(),
    holderCore: Boolean = false
  ) = threadPoolMap[poolName].let { pool ->
    pool ?: ModifyThreadPool(
      corePoolSize,
      maximumPoolSize,
      keepLive.first,
      keepLive.second,
      cpuCount * 2,
      getThreadFactory(poolName),
      reject,
      holderCore
    ).also {
      threadPoolMap[poolName] = it
    }
  }

  fun createSingleThreadPool(
    poolName: String,
    keepLive: Pair<Long, TimeUnit> = 60.toLong() to TimeUnit.SECONDS,
    reject: RejectedExecutionHandler = ThreadPoolExecutor.AbortPolicy()
  ) = threadPoolMap[poolName].let { pool ->
    pool ?: ModifyThreadPool(
      1,
      1,
      keepLive.first,
      keepLive.second,
      10,
      getThreadFactory(poolName),
      reject,
      true
    ).also {
      threadPoolMap[poolName] = it
    }
  }
}
