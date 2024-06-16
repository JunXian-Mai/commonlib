package org.markensic.commonlib.demo.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

object PermissionUtils {
  val STORAGE_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(
      Manifest.permission.READ_MEDIA_IMAGES,
      Manifest.permission.READ_MEDIA_VIDEO,
      Manifest.permission.READ_MEDIA_AUDIO
    )
  } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    arrayOf(
      Manifest.permission.READ_EXTERNAL_STORAGE
    )
  } else {
    arrayOf(
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
  }

  val FOREGROUND_NOTIFICATIONS_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    arrayOf(
      Manifest.permission.FOREGROUND_SERVICE_LOCATION,
      Manifest.permission.POST_NOTIFICATIONS,
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.FOREGROUND_SERVICE
    )
  } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(
      Manifest.permission.POST_NOTIFICATIONS,
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.FOREGROUND_SERVICE
    )
  } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    arrayOf(
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.FOREGROUND_SERVICE
    )
  } else {
    arrayOf(
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION
    )
  }

  @RequiresApi(Build.VERSION_CODES.R)
  fun requestManageExternalStoragePermission(context: Context) {
    if (!Environment.isExternalStorageManager()) {
      val intent = Intent().apply {
        action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
        data = Uri.fromParts("package", context.packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
    }
  }

  fun checkSelfPermission(activity: Activity, permissions: Array<String>) {
    var bool = true
    permissions.forEach {
      if (ActivityCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_DENIED) {
        bool = false
        return@forEach
      }
    }
    if (!bool) ActivityCompat.requestPermissions(activity, permissions, 100)
  }
}
