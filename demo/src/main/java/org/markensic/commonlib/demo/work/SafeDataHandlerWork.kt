package org.markensic.commonlib.simple.work

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.multiprocess.RemoteCoroutineWorker
import okio.buffer
import okio.sink
import org.markensic.commonlib.demo.R
import org.markensic.commonlib.secure.md5
import org.markensic.commonlib.demo.utils.OSUtils
import java.io.File
import java.util.concurrent.ThreadLocalRandom

class SafeDataHandlerWork(
  val appContext: Context,
  val params: WorkerParameters
) : RemoteCoroutineWorker(appContext, params) {
  private val tag = "SafeDataHandlerWork"
  private val ntfId = ThreadLocalRandom.current().nextInt(1111, 9999)
  private val workNotificationChannelId = "${appContext.packageName}RemoteWorker-Channel"

  private fun createNotification(): Notification {
    NotificationManagerCompat.from(appContext).apply {
      createNotificationChannel(
        NotificationChannelCompat.Builder(
          workNotificationChannelId,
          NotificationManager.IMPORTANCE_HIGH
        ).apply {
          setName("Worker-Channel")
        }.build()
      )
    }

    return NotificationCompat.Builder(appContext, workNotificationChannelId)
      .apply {
        setContentTitle("数据安全删除")
        setContentText("cleaning...")
        setAutoCancel(false)
        setOngoing(true)
        setSmallIcon(R.mipmap.ic_launcher)
      }.build()
  }

  override suspend fun doRemoteWork(): Result {
    setForegroundAsync(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ForegroundInfo(
          ntfId, createNotification(),
          ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
      } else {
        ForegroundInfo(
          ntfId, createNotification()
        )
      }
    )

    val fileFlags = appContext.packageName.md5()
    val overrideFilePath = File("/sdcard/Download/${fileFlags}/${System.currentTimeMillis()}/")
    // Log.d(tag, "ifExist to delete ${overrideFilePath.deleteRecursively()}")
    Log.d(tag, "create ${overrideFilePath.mkdirs()}")

    val totalSize = OSUtils.getTotalSize(appContext)
    Log.d(tag, "TotalSize -> $totalSize")
    val freeSize = OSUtils.getFreeSize(appContext)
    Log.d(tag, "FreeSize -> $freeSize")

    val gSize = 1024 * 1024 * 1024
    val unitSize = 1024 * 32
    var remainFreeSize = freeSize
    var c = 0
    while (remainFreeSize > 0) {
      val overrideFile = File(overrideFilePath, "file_${c}")
      Log.d(tag, "ready write in ${overrideFile.path}!")

      overrideFile.sink().buffer().use {
        var writedSize = 0
        while (writedSize < gSize) {
          val sb = StringBuilder()
          val st = System.currentTimeMillis()
          if (remainFreeSize < unitSize) {
            val l = ThreadLocalRandom.current().nextInt(1, 9)
            repeat(remainFreeSize.toInt()) { sb.append(l) }
          } else {
            val l = ThreadLocalRandom.current().nextLong(1111111111111111, 9999999999999999)
            repeat(unitSize / 16) { sb.append(l) }
          }

          val fw = sb.toString()

          it.writeUtf8(fw)
          val et = System.currentTimeMillis()
          Log.d(tag, "cost time -> ${et - st}")

          val fwSize = fw.toByteArray().size
          Log.d(tag, "writeUnitSize -> $fwSize")
          writedSize += fwSize

          remainFreeSize -= fwSize
          Log.d(tag, "remainFreeSize -> $remainFreeSize")
          if (remainFreeSize <= 0) {
            break
          }
        }
        c++
      }
      if (remainFreeSize <= 0) {
        break
      }
    }

    return Result.success()
  }
}
