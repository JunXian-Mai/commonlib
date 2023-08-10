package org.markensic.commonlib.thread

import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock

class ModifyThreadPool(
  corePoolSize: Int,
  maximumPoolSize: Int,
  keepAliveTime: Long,
  unit: TimeUnit,
  workQueue: Int,
  threadFactory: ThreadFactory,
  handler: RejectedExecutionHandler,
  private val holderCore: Boolean = false
) : ThreadPoolExecutor(
  corePoolSize,
  maximumPoolSize,
  keepAliveTime,
  unit,
  ResizableCapacityLinkedBlockingQueue<Runnable>(workQueue),
  threadFactory,
  handler
) {

  init {
    //允许回收核心线程，实现动态修改线程池
    allowCoreThreadTimeOut(true)
  }

  private val lock = ReentrantReadWriteLock()

  var coreGrowthRate = 2f

  var queueGrowthRate = 2f

  override fun execute(command: Runnable?) {
    (queue as ResizableCapacityLinkedBlockingQueue<Runnable>).also { queue ->
      lock.readLock().lock()
      var isFull = queue.remainingCapacity() == 0
      lock.readLock().unlock()

      if (isFull) {
        try {
          lock.writeLock().lock()
          isFull = queue.remainingCapacity() == 0
          if (isFull) {
            val allQueueSize = queue.size + queue.remainingCapacity()
            if (!holderCore) {
              corePoolSize = (corePoolSize * coreGrowthRate).toInt() + 1
              maximumPoolSize = corePoolSize
              prestartAllCoreThreads()
            }

            queue.setCapacity((allQueueSize * queueGrowthRate).toInt() + 1)
          }
        } finally {
          lock.writeLock().unlock()
        }
      }
    }
    super.execute(command)
  }
}
