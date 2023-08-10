package org.markensic.commonlib.core

import android.app.Application
import org.markensic.commonlib.thread.Schedulers
import org.markensic.commonlib.utils.ToastUtils

object Commonlib {
  fun init(application: Application) {
    Schedulers.Work.post {
      ToastUtils.register(application)
    }
  }
}
