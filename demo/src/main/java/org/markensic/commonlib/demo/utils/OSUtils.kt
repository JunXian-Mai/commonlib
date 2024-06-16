package org.markensic.commonlib.demo.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi


object OSUtils {
  fun getTotalSize(context: Context): Long {
    val path = Environment.getExternalStorageDirectory()
    val stat = StatFs(path.path)
    val blockSize = stat.blockSizeLong
    val totalBlocks = stat.blockCountLong
    println("getTotalSize -> ${blockSize * totalBlocks} bytes")
    return blockSize * totalBlocks
  }

  fun getFreeSize(context: Context): Long {
    val path = Environment.getExternalStorageDirectory()
    val stat = StatFs(path.path)
    val blockSize = stat.blockSizeLong
    val availableBlocks = stat.availableBlocksLong
    println("getFreeSize -> ${blockSize * availableBlocks} bytes")
    return blockSize * availableBlocks
  }
}
