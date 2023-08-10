package org.markensic.commonlib.thread

import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool

object Schedulers {
  val cpu = Executors.newWorkStealingPool(ThreadPool.cpuCount)

  val io = ThreadPool.createVariableThreadPool("io-task-%d", holderCore = true)

  val cpuForkJoinPool = cpu as ForkJoinPool
}
