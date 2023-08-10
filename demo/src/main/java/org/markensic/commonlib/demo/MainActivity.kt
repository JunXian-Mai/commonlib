package org.markensic.commonlib.demo

import android.app.Activity
import android.os.Bundle
import org.markensic.commonlib.thread.Schedulers
import org.markensic.commonlib.utils.ToastUtils
import java.util.concurrent.atomic.AtomicInteger

class MainActivity: Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val ai = AtomicInteger(0)
    Schedulers.UI.postDelayed({
      ToastUtils.showNow("-1000", 7000)
    }, 6000)

    while (ai.get() < 10) {
      ToastUtils.addShow(ai.getAndIncrement().toString(), 5000)
    }
  }
}
