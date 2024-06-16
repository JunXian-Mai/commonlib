package org.markensic.commonlib.datastore

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.markensic.commonlib.log.LogProvider

class DataStoreSharedPreProxy(context: Context, spName: String) : SharedPreferences {
  companion object {
    private const val TAG = "DataStoreSharedPreProxy"
    private val Lock = Any()
    private val listeners = ArrayList<OnSharedPreferenceChangeListener>()
  }

  private val ds = DataStoreProxy(context, spName)
  private val editor = Editor(this, ds)


  class Editor internal constructor(private val dsp: DataStoreSharedPreProxy, private val ds: DataStoreProxy) : SharedPreferences.Editor {

    override fun putString(key: String, value: String?) = this.apply {
      ds.putCache<String>(key, value)
      dsp.notifyChange(key)
    }

    override fun putStringSet(
      key: String,
      values: Set<String>?
    ) = this.apply {
      ds.putCache<Set<String>>(key, values)
      dsp.notifyChange(key)
    }

    override fun putInt(key: String, value: Int) = this.apply {
      ds.putCache<Int>(key, value)
      dsp.notifyChange(key)
    }

    override fun putLong(key: String, value: Long) = this.apply {
      ds.putCache<Long>(key, value)
      dsp.notifyChange(key)
    }

    override fun putFloat(key: String, value: Float) = this.apply {
      ds.putCache<Float>(key, value)
      dsp.notifyChange(key)
    }

    override fun putBoolean(key: String, value: Boolean) = this.apply {
      ds.putCache<Boolean>(key, value)
      dsp.notifyChange(key)
    }

    override fun remove(key: String) = this.apply {
      ds.syncLaunch {
        ds.remove(key)
      }
      dsp.notifyChange(key)
    }

    override fun clear() = this.apply {
      ds.syncLaunch {
        ds.clear()
      }
    }

    override fun commit(): Boolean {
      LogProvider.d(TAG, "commit()")
      var result = true
      ds.syncLaunch {
        ds.updateAll {
          result = false
        }
      }
      return result
    }

    override fun apply() {
      LogProvider.d(TAG, "apply()")
      CoroutineScope(Dispatchers.Unconfined).launch {
        ds.updateAll()
      }
    }
  }

  override fun getAll(): MutableMap<String, *> {
    LogProvider.d(TAG, "getAll()")
    return ds.syncLaunch {
      ds.getAll()
    } ?: mutableMapOf<String, Any>()
  }

  override fun getString(key: String, defValue: String?): String? {
    return ds.syncLaunch {
      get<String>(key, defValue)
    }
  }

  override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
    return ds.syncLaunch {
      get<Set<String>>(key, defValues)
    }
  }

  override fun getInt(key: String, defValue: Int): Int {
    return ds.syncLaunch {
      get<Int>(key, defValue)
    } ?: defValue
  }

  override fun getLong(key: String, defValue: Long): Long {
    return ds.syncLaunch {
      get<Long>(key, defValue)
    } ?: defValue
  }

  override fun getFloat(key: String, defValue: Float): Float {
    return ds.syncLaunch {
      get<Float>(key, defValue)
    } ?: defValue
  }

  override fun getBoolean(key: String, defValue: Boolean): Boolean {
    return ds.syncLaunch {
      get<Boolean>(key, defValue)
    } ?: defValue
  }

  override fun contains(key: String): Boolean {
    return ds.syncLaunch {
      ds.has(key)
    } ?: false
  }

  override fun edit(): Editor {
    return editor
  }

  override fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?) {
    synchronized(Lock) {
      listener?.let {
        listeners.add(it)
      }
    }
  }

  override fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?) {
    synchronized(Lock) {
      listener?.let {
        listeners.remove(it)
      }
    }
  }

  private fun collectListeners(): List<OnSharedPreferenceChangeListener> {
    synchronized(Lock) {
      return listeners.map { it }
    }
  }

  internal fun notifyChange(key: String) {
    collectListeners().forEach {
      it.onSharedPreferenceChanged(this, key)
    }
  }
}
