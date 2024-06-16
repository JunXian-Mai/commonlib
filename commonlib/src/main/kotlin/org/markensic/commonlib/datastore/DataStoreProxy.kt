package org.markensic.commonlib.datastore

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.markensic.commonlib.log.LogProvider
import java.util.concurrent.Callable
import kotlin.reflect.KClass

class DataStoreProxy(val context: Context, val spName: String, val migrations: Boolean = true) {
  companion object {
    const val TAG = "DataStoreProxy"
  }

  val innerContext = context.applicationContext
  val Context.dataStoreProxy by preferencesDataStore(
    name = spName,
    produceMigrations = { context ->
      if (migrations) {
        listOf(SharedPreferencesMigration(context, spName))
      } else {
        listOf()
      }
    }
  )
  val cacheMap: HashMap<Pair<KClass<*>, String>, Any> = HashMap()
  val notExistField: NotExistFields = NotExistFields()

  inline fun <reified T> putCache(key: String, value: T?) {
    LogProvider.d(TAG, "putCache<${T::class.java.simpleName}>($key, $value)")
    value?.let { v ->
      lockWrite {
        cacheMap[T::class to key] = v
        notExistField.remove(key)
      }
    } ?: run {
      removeCacheByClazz<T>(key)
    }
  }

  inline fun <reified T> getCache(key: String): T? {
    LogProvider.d(TAG, "getCache<${T::class.java.simpleName}>($key)")
    val r = lockRead {
      if (cacheMap.containsKey(T::class to key)) {
        return@lockRead cacheMap[T::class to key] as T
      } else {
        return@lockRead null
      }
    }
    LogProvider.d(TAG, "getCache<${T::class.java.simpleName}>($key) ==> return $r")
    return r
  }

  fun hasCache(key: String): Boolean {
    LogProvider.d(TAG, "hasCache($key)")
    val r = lockRead {
      var has = false
      val tmpList = cacheMap.keys.toList()
      tmpList.forEach { entry ->
        if (entry.second == key) {
          has = true
          return@forEach
        }
      }
      return@lockRead has
    } ?: false
    LogProvider.d(TAG, "hasCache($key) ==> return $r")
    return r
  }

  inline fun <reified T> hasCacheByClazz(key: String): Boolean {
    LogProvider.d(TAG, "hasCacheByClazz<${T::class.java.simpleName}>($key)")
    val r = lockRead {
      return@lockRead cacheMap.containsKey(T::class to key)
    } ?: false
    LogProvider.d(TAG, "hasCacheByClazz<${T::class.java.simpleName}>($key) ==> return $r")
    return r
  }

  fun removeCache(key: String): Boolean? {
    LogProvider.d(TAG, "removeCache($key)")
    val r = lockReadAndWrite({
      var realCacheKey: Pair<KClass<*>, String>? = null
      val tmpList = cacheMap.keys.toList()
      tmpList.forEach { entry ->
        if (entry.second == key) {
          realCacheKey = entry
          return@forEach
        }
      }
      realCacheKey
    }, { realCacheKey ->
      realCacheKey?.let {
        cacheMap.remove(it)
        true
      }
    }, object : ExceptionCallback<Boolean> {
      override fun callException(tr: Throwable): Boolean {
        return false
      }
    })
    LogProvider.d(TAG, "removeCache($key) ==> return $r")
    return r
  }

  inline fun <reified T> removeCacheByClazz(key: String): Boolean? {
    LogProvider.d(TAG, "removeCacheByClazz<${T::class.java.simpleName}>($key)")
    val r = lockReadAndWrite({
      if (cacheMap.containsKey(T::class to key)) {
        T::class to key
      } else {
        null
      }
    }, { realCacheKey ->
      realCacheKey?.let {
        cacheMap.remove(it)
        true
      }
    }, object : ExceptionCallback<Boolean> {
      override fun callException(tr: Throwable): Boolean {
        return false
      }
    })
    LogProvider.d(TAG, "removeCacheByClazz<${T::class.java.simpleName}>($key) ==> return $r")
    return r
  }

  fun clearCache() {
    LogProvider.d(TAG, "clearCache()")
    lockWrite {
      cacheMap.clear()
      notExistField.setAllClearFlag()
    }
  }

  suspend inline fun <reified T> update(key: String, value: T, exception: ExceptionCallback<Unit>? = null) {
    try {
      putCache(key, value)

      LogProvider.d(TAG, "update<${T::class.java.simpleName}>($key, $value)")
      innerContext.dataStoreProxy.edit {
        T::class.whenPreferencesKey({
          it[stringPreferencesKey(key)] = value as String
        }, {
          it[stringSetPreferencesKey(key)] = value as Set<String>
        }, {
          it[intPreferencesKey(key)] = value as Int
        }, {
          it[longPreferencesKey(key)] = value as Long
        }, {
          it[floatPreferencesKey(key)] = value as Float
        }, {
          it[booleanPreferencesKey(key)] = value as Boolean
        })
      }
    } catch (e: Exception) {
      exception?.callException(e)
    }
  }

  suspend inline fun <reified T> get(key: String, defValue: T?): T? {
    val cacheField = getCache<T>(key)
    val needCheckFile = if (T::class == String::class) {
      (cacheField as String?).isNullOrBlank()
    } else if (T::class == Set::class) {
      (cacheField as Set<*>?).isNullOrEmpty()
    } else {
      cacheField == null
    } && lockRead {
      notExistField.has(key)
    } != true

    var r: T? = if (needCheckFile) defValue else cacheField
    if (needCheckFile) {
      LogProvider.d(TAG, "get<${T::class.java.simpleName}>($key, $defValue)")
      innerContext.dataStoreProxy.data.catch {
        LogProvider.e(TAG, "[$key] get<${T::class.java.simpleName}> error: $it")
      }.map {
        val tR = T::class.whenPreferencesKey({
          it[stringPreferencesKey(key)] as T?
        }, {
          it[stringSetPreferencesKey(key)] as T?
        }, {
          it[intPreferencesKey(key)] as T?
        }, {
          it[longPreferencesKey(key)] as T?
        }, {
          it[floatPreferencesKey(key)] as T?
        }, {
          it[booleanPreferencesKey(key)] as T?
        })

        tR?.apply {
          putCache<T>(key, this)
        } ?: run {
          LogProvider.d(TAG, "add not exist field -> $key")
          lockWrite {
            notExistField.add(key)
          }
          defValue
        }
      }.firstOrNull {
        r = it
        true
      }
      LogProvider.d(TAG, "get<${T::class.java.simpleName}>($key, $defValue) ==> return $r")
    }

    return r
  }

  suspend fun updateAll(exception: ((Exception) -> Unit)? = null) {
    LogProvider.d(TAG, "updateAll()")
    try {
      innerContext.dataStoreProxy.edit {
        lockRead {
          for (entry in cacheMap) {
            val clazz = entry.key.first
            val realKey = entry.key.second
            clazz.whenPreferencesKey({
              it[stringPreferencesKey(realKey)] = entry.value as String
            }, {
              it[stringSetPreferencesKey(realKey)] = entry.value as Set<String>
            }, {
              it[intPreferencesKey(realKey)] = entry.value as Int
            }, {
              it[longPreferencesKey(realKey)] = entry.value as Long
            }, {
              it[floatPreferencesKey(realKey)] = entry.value as Float
            }, {
              it[booleanPreferencesKey(realKey)] = entry.value as Boolean
            })
          }
        }
      }
    } catch (e: Exception) {
      exception?.invoke(e)
    }
  }

  suspend fun getAll(): MutableMap<String, Any> {
    updateAll()

    LogProvider.d(TAG, "getAll()")
    val rMap = mutableMapOf<String, Any>()
    try {
      innerContext.dataStoreProxy.edit {
        it.asMap().forEach { entry ->
          rMap[entry.key.name] = entry.value
          lockWrite {
            putCache(entry.key.name, entry.value)
          }
        }
      }
    } catch (e: Exception) {
      LogProvider.e(TAG, "e: $e")
    }
    return rMap
  }

  suspend fun has(key: String): Boolean {
    var hasCache = hasCache(key)
    if (!hasCache) {
      LogProvider.d(TAG, "has($key)")
      try {
        innerContext.dataStoreProxy.edit {
          it.asMap().forEach { entry ->
            if (entry.key.name == key) {
              hasCache = true
              return@forEach
            }
          }
        }
      } catch (e: Exception) {
        LogProvider.e(TAG, "e: $e")
      }
      LogProvider.d(TAG, "has($key) ==> return $hasCache")
    }

    return hasCache
  }

  suspend inline fun <reified T> hasByClass(key: String): Boolean {
    var hasCache = hasCacheByClazz<T>(key)
    if (!hasCache) {
      LogProvider.d(TAG, "hasByClass<${T::class.java.simpleName}>($key)")
      try {
        innerContext.dataStoreProxy.edit {
          hasCache = T::class.whenPreferencesKey({
            it.contains(stringPreferencesKey(key))
          }, {
            it.contains(stringSetPreferencesKey(key))
          }, {
            it.contains(intPreferencesKey(key))
          }, {
            it.contains(longPreferencesKey(key))
          }, {
            it.contains(floatPreferencesKey(key))
          }, {
            it.contains(booleanPreferencesKey(key))
          }) ?: false
        }
      } catch (e: Exception) {
        LogProvider.e(TAG, "e: $e")
      }
      LogProvider.d(TAG, "hasByClass<${T::class.java.simpleName}>($key) ==> return $hasCache")
    }

    return hasCache
  }

  suspend fun remove(key: String): Boolean? {
    removeCache(key)

    LogProvider.d(TAG, "remove($key)")
    var r: Boolean? = null
    try {
      var realKey: Preferences.Key<*>? = null
      innerContext.dataStoreProxy.edit {
        it.asMap().forEach { entry ->
          if (entry.key.name == key) {
            realKey = entry.key
            return@forEach
          }
        }

        realKey?.let { ik ->
          it.remove(ik)
          lockWrite {
            notExistField.add(key)
          }
          r = true
        }
      }
    } catch (e: Exception) {
      r = false
      LogProvider.e(TAG, "e: %s", e)
    }
    LogProvider.d(TAG, "remove($key) ==> return $r")
    return r
  }

  suspend inline fun <reified T> removeByClass(key: String): Boolean? {
    removeCacheByClazz<T>(key)

    LogProvider.d(TAG, "removeByClass<${T::class.java.simpleName}>($key)")
    var r: Boolean? = null
    try {
      var realKey: Preferences.Key<*>? = null
      innerContext.dataStoreProxy.edit {
        realKey = T::class.whenPreferencesKey({
          stringPreferencesKey(key)
        }, {
          stringSetPreferencesKey(key)
        }, {
          intPreferencesKey(key)
        }, {
          longPreferencesKey(key)
        }, {
          floatPreferencesKey(key)
        }, {
          booleanPreferencesKey(key)
        })

        realKey?.let { ik ->
          it.remove(ik)
          lockWrite {
            notExistField.add(key)
          }
          r = true
        }
      }
    } catch (e: Exception) {
      r = false
      LogProvider.e(TAG, "e: %s", e)
    }
    LogProvider.d(TAG, "removeByClass<${T::class.java.simpleName}>($key) ==> return $r")
    return r
  }

  suspend fun clear() {
    clearCache()

    LogProvider.d(TAG, "clear()")
    try {
      innerContext.dataStoreProxy.edit {
        it.clear()
      }
    } catch (e: Exception) {
      LogProvider.e(TAG, "e: $e")
    }
  }

  fun <R> syncLaunch(func: suspend DataStoreProxy.() -> R?): R? {
    var r: R?
    runBlocking {
      r = this@DataStoreProxy.func()
    }
    return r
  }

  /**
   * 使用 CLass 作为接收器会出现以下问题
   *
   * 以下Kotlin Class
   * Int::class.java
   * Long::class.java
   * Float::class.java
   * Boolean::class.java
   *
   * 经编译器优化后会为转变为基本类型
   *
   * int.class
   * long.class
   * float.class
   * boolean.class
   *
   * 但在泛型中使用的是封装类型 Integer Long Float Boolean
   * 需要转化后才能正确判断
   * Class.forName("java.lang.Integer") -> intCallable.call()
   * Class.forName("java.lang.Long") -> longCallable.call()
   * Class.forName("java.lang.Float") -> floatCallable.call()
   * Class.forName("java.lang.Boolean") -> booleanCallable.call()
   *
   * 因此使用 KClass 根据 kotlin class 进行判断
   * 美化写法
   */
  fun <R> KClass<*>.whenPreferencesKey(
    stringCallable: Callable<R>,
    stringSetCallable: Callable<R>,
    intCallable: Callable<R>,
    longCallable: Callable<R>,
    floatCallable: Callable<R>,
    booleanCallable: Callable<R>,
  ): R? {
    return when (this) {
      String::class -> stringCallable.call()
      Set::class -> stringSetCallable.call()
      Int::class -> intCallable.call()
      Long::class -> longCallable.call()
      Float::class -> floatCallable.call()
      Boolean::class -> booleanCallable.call()
      else -> null
    }
  }
}
