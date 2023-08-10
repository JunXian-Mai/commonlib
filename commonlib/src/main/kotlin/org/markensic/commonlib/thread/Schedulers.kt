package org.markensic.commonlib.thread

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicInteger

object Schedulers {
  val cpu = Executors.newWorkStealingPool(ThreadPool.cpuCount)

  val io = ThreadPool.createVariableThreadPool("io-task", holderCore = true)

  val cpuForkJoinPool = cpu as ForkJoinPool

  object UI {
    private val mainHandler = Handler(Looper.getMainLooper())

    fun postAtFrontOfQueue(runnable: () -> Unit) {
      mainHandler.postAtFrontOfQueue(runnable)
    }

    fun post(runnable: () -> Unit) {
      mainHandler.post(runnable)
    }

    fun postDelayed(runnable: () -> Unit, delayMillis: Long) {
      mainHandler.postDelayed(runnable, delayMillis)
    }
  }

  object Work {
    private val count = AtomicInteger(0)
    private val workHandlerThread = createWorkHandlerThread()
    @Volatile private var _workHandler: Handler? = null

    private fun createWorkHandlerThread() =
      HandlerThread(String.format("Work-HandlerThread-%d", count.getAndIncrement()))

    private fun prepare(): Handler {
      if (_workHandler == null) {
        synchronized(Work::class) {
          if (_workHandler == null) {
            workHandlerThread.start()
            _workHandler = Handler(workHandlerThread.looper)
          }
        }
      }
      return _workHandler ?: throw RuntimeException("Work-Handler not already")
    }

    fun post(runnable: () -> Unit) {
      prepare().post(runnable)
    }

    fun createWorkHandler(handleMessage: (Message) -> Unit): Handler {
      return with(createWorkHandlerThread()) {
        start()
        object : Handler(looper) {
          override fun handleMessage(msg: Message) {
            handleMessage(msg)
          }
        }
      }
    }
  }
}
