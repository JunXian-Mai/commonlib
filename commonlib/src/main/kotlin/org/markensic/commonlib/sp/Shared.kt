package org.markensic.commonlib.sp

import android.app.Application
import android.content.Context
import org.markensic.commonlib.secure.md5

object Shared {
  @Volatile
  private var _context: Application? = null

  private val context: Application
    get() {
      return _context ?: throw RuntimeException("please call run Shared.register()")
    }

  fun register(application: Application) {
    if (_context == null) {
      synchronized(Shared::class) {
        if (_context == null) {
          _context = application
        }
      }
    }
  }

  val innerSPFileName: String
    get() = context.packageName.md5()

  fun getPreferences(fileName: String, mode: Int = Context.MODE_PRIVATE) =
    context.getSharedPreferences(fileName, mode)

  inline fun <reified T> String.getSharedValue(default: T? = null, fileName: String = innerSPFileName): T {
    val sp = getPreferences(fileName)
    return when (T::class) {
      String::class -> {
        sp.getString(this, default?.let { it as String } ?: "") as T
      }
      Int::class -> {
        sp.getInt(this, default?.let { it as Int } ?: -1) as T
      }
      Float::class -> {
        sp.getFloat(this, default?.let { it as Float } ?: -1f) as T
      }
      Long::class -> {
        sp.getLong(this, default?.let { it as Long } ?: -1L) as T
      }
      Boolean::class -> {
        sp.getBoolean(this, default?.let { it as Boolean } ?: false) as T
      }
      else -> {
        throw RuntimeException("T is not allow type")
      }
    }
  }

  inline fun <reified T> String.putSharedValue(value: T, fileName: String = innerSPFileName) {
   getPreferences(fileName).edit().apply {
     when (T::class) {
       String::class -> {
         putString(this@putSharedValue, value as String)
       }
       Int::class -> {
         putInt(this@putSharedValue, value as Int)
       }
       Float::class -> {
         putFloat(this@putSharedValue, value as Float)
       }
       Long::class -> {
         putLong(this@putSharedValue, value as Long)
       }
       Boolean::class -> {
         putBoolean(this@putSharedValue, value as Boolean)
       }
       else -> {
         throw RuntimeException("value is not allow type")
       }
     }
     apply()
   }
  }
}
