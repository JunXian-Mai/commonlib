package org.markensic.commonlib.demo

import android.app.Activity
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.multiprocess.RemoteListenableWorker
import androidx.work.multiprocess.RemoteWorkManager
import androidx.work.multiprocess.RemoteWorkerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.markensic.commonlib.datastore.DataStoreProxy
import org.markensic.commonlib.demo.utils.OSUtils
import org.markensic.commonlib.demo.utils.PermissionUtils
import org.markensic.commonlib.secure.md5
import org.markensic.commonlib.simple.work.SafeDataHandlerWork
import java.io.File
import java.util.concurrent.atomic.AtomicLong

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    PermissionUtils.checkSelfPermission(
      this,
      PermissionUtils.STORAGE_PERMISSION + PermissionUtils.FOREGROUND_NOTIFICATIONS_PERMISSION
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      PermissionUtils.requestManageExternalStoragePermission(this)
    }

    setContentView(R.layout.activity_main)

    findViewById<Button>(R.id.clean).setOnClickListener {
      RemoteWorkManager.getInstance(this).enqueue(
        OneTimeWorkRequest.Builder(SafeDataHandlerWork::class.java)
          .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
          .setInputData(Data.Builder().apply {
            val remoteComponentName = ComponentName(packageName, RemoteWorkerService::class.java.name)
            putString(RemoteListenableWorker.ARGUMENT_PACKAGE_NAME, remoteComponentName.packageName)
            putString(RemoteListenableWorker.ARGUMENT_CLASS_NAME, remoteComponentName.className)
          }.build()).build()
      )

      it.isEnabled = false
    }

    findViewById<Button>(R.id.reclean).setOnClickListener {
      it.isEnabled = false
      CoroutineScope(Dispatchers.IO).launch {
        val fileFlags = packageName.md5()
        val overrideFilePath = File("/sdcard/Download/${fileFlags}/")
        overrideFilePath.deleteRecursively()
        withContext(Dispatchers.Main) {
          updateFreeSizeTip()
          it.isEnabled = true
        }
      }
    }

//    updateFreeSizeTip()
//
//    CoroutineScope(Dispatchers.IO).launch {
//      val freeSize = AtomicLong(OSUtils.getFreeSize(this@MainActivity))
//      while (freeSize.get() >= 0) {
//        Thread.sleep(3000)
//        withContext(Dispatchers.Main) {
//          freeSize.set(updateFreeSizeTip())
//        }
//      }
//    }

    val ds = DataStoreProxy(this, "tpb")
    lifecycleScope.launch {
      println(ds.has("str"))
      println(ds.has("strSet"))
      println(ds.has("int"))
      println(ds.has("float"))
      println(ds.has("boolean"))

      println(ds.hasByClass<String>("str"))
      println(ds.hasByClass<Set<String>>("strSet"))
      println(ds.hasByClass<Int>("int"))
      println(ds.hasByClass<Float>("float"))
      println(ds.hasByClass<Boolean>("boolean"))

      println(ds.remove("str"))
      println(ds.remove("strSet"))
      println(ds.remove("int"))
      println(ds.remove("float"))
      println(ds.remove("boolean"))

      println(ds.has("str"))
      println(ds.has("strSet"))
      println(ds.has("int"))
      println(ds.has("float"))
      println(ds.has("boolean"))

      println(ds.hasByClass<String>("str"))
      println(ds.hasByClass<Set<String>>("strSet"))
      println(ds.hasByClass<Int>("int"))
      println(ds.hasByClass<Float>("float"))
      println(ds.hasByClass<Boolean>("boolean"))

      ds.putCache("str", "1")
      val set = mutableSetOf<String>()
      set.add("set")
      ds.putCache("strSet", set)
      ds.putCache("int", 2)
      ds.putCache("float", 2f)
      ds.putCache("boolean", true)

      println(ds.removeCache("str"))
      println(ds.removeCache("strSet"))
      println(ds.removeCache("int"))
      println(ds.removeCache("float"))
      println(ds.removeCache("boolean"))

      println(ds.get("str", "0"))
      println(ds.get("strSet", mutableSetOf<String>()))
      println(ds.get("int", 0))
      println(ds.get("float", 0f))
      println(ds.get("boolean", false))

      ds.putCache("str", "1")
      set.add("set")
      ds.putCache("strSet", set)
      ds.putCache("int", 2)
      ds.putCache("float", 2f)
      ds.putCache("boolean", true)

      println(ds.removeCacheByClazz<String>("str"))
      println(ds.removeCacheByClazz<Set<String>>("strSet"))
      println(ds.removeCacheByClazz<Int>("int"))
      println(ds.removeCacheByClazz<Float>("float"))
      println(ds.removeCacheByClazz<Boolean>("boolean"))

      println(ds.get("str", "0"))
      println(ds.get("strSet", mutableSetOf<String>()))
      println(ds.get("int", 0))
      println(ds.get("float", 0f))
      println(ds.get("boolean", false))

      ds.putCache("str", "1")
      set.add("set")
      ds.putCache("strSet", set)
      ds.putCache("int", 2)
      ds.putCache("float", 2f)
      ds.putCache("boolean", true)

      println(ds.get("str", "0"))
      println(ds.get("strSet", mutableSetOf<String>()))
      println(ds.get("int", 0))
      println(ds.get("float", 0f))
      println(ds.get("boolean", false))

      println(ds.getAll())

      println(ds.get("str", "0"))
      println(ds.get("strSet", mutableSetOf<String>()))
      println(ds.get("int", 0))
      println(ds.get("float", 0f))
      println(ds.get("boolean", false))

      println(ds.clearCache())

      println(ds.get("str", "0"))
      println(ds.get("strSet", mutableSetOf<String>()))
      println(ds.get("int", 0))
      println(ds.get("float", 0f))
      println(ds.get("boolean", false))
    }
  }

  override fun onResume() {
    super.onResume()
    Log.d("Ht", "onResume")
  }

  private fun updateFreeSizeTip(): Long {
    val gSize = 1024 * 1024 * 1024
    val mSize = 1024 * 1024
    val kSize = 1024
    val currentFreeSize = OSUtils.getFreeSize(this@MainActivity)
    findViewById<TextView>(R.id.tip).text =
      "目前剩余空间 ${currentFreeSize / gSize} G, ${currentFreeSize % gSize / mSize} M, ${currentFreeSize % gSize % mSize / kSize} KBytes"
    return currentFreeSize
  }
}
