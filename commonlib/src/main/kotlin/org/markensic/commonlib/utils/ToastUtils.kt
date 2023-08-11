package org.markensic.commonlib.utils

import android.app.Application
import android.widget.Toast
import androidx.annotation.IntRange
import org.markensic.commonlib.thread.Schedulers
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object ToastUtils {
  @Volatile
  private var _context: Application? = null

  private val context: Application
    get() {
      return _context ?: throw RuntimeException("please call run ToastUtils.register()")
    }

  fun register(application: Application) {
    if (_context == null) {
      synchronized(ToastUtils::class) {
        if (_context == null) {
          _context = application
        }
      }
    }
  }

  private val queueLooping = AtomicBoolean(false)
  private val toastQueue = ConcurrentLinkedDeque<Pair<String, Long>>()
  private var lastToast: StatusToast? = null

  fun showNow(content: String, @IntRange(100, 7000) duration: Long = 3000) {
    toastQueue.addFirst(content to duration)
    if (queueLooping.compareAndSet(false, true)) {
      show()
    } else if (queueLooping.get()) {
      lastToast?.cancel()
      show()
    }
  }

  fun addShow(content: String, @IntRange(100, 7000) duration: Long = 3000) {
    toastQueue.addLast(content to duration)
    if (queueLooping.compareAndSet(false, true)) {
      show()
    }
  }

  private fun show() {
    toastQueue.pollFirst()?.let { (content, duration) ->
      val toast = StatusToast(Toast.makeText(context, content, Toast.LENGTH_LONG))
      lastToast = toast
      Schedulers.UI.post {
        toast.show()
        Schedulers.UI.postDelayed({
          val canceled = toast.canceled()
          toast.cancel()
          if (!canceled) {
            show()
          }
        }, duration)
      }
    } ?: {
      lastToast = null
      queueLooping.set(false)
    }
  }

  private class StatusToast(private val toast: Toast) {
    companion object {
      const val NEW = 0
      const val SHOWED = 1
      const val CANCELED = 2
    }

    private val status = AtomicInteger(NEW)

    fun show() {
      if (status.compareAndSet(NEW, SHOWED)) {
        toast.show()
      }
    }

    fun cancel() {
      if (!canceled()) {
        status.set(CANCELED)
        toast.cancel()
      }
    }

    fun canceled() = status.get() == CANCELED
  }
}
