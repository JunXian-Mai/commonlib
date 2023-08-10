package org.markensic.commonlib.demo

import android.app.Application
import android.content.Context
import org.markensic.commonlib.core.Commonlib

class MainApp: Application() {
  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
    Commonlib.init(this)
  }
}
