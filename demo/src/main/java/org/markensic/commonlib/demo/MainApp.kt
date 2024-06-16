package org.markensic.commonlib.demo

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import org.markensic.commonlib.core.Commonlib

class MainApp : Application(), Configuration.Provider {
  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
    Commonlib.init(this)
  }

  override fun onCreate() {
    super.onCreate()
    WorkManager.initialize(this, workManagerConfiguration)
  }

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder().setMinimumLoggingLevel(Log.INFO)
      .setDefaultProcessName(packageName)
      .build()
}
