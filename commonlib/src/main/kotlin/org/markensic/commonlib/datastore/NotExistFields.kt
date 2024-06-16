package org.markensic.commonlib.datastore

data class NotExistFields(
  private var flag: Int = NORMAL_FIELD_FLAGS,
  private val set: MutableSet<String> = mutableSetOf()
) {
  companion object {
    private const val NORMAL_FIELD_FLAGS = 0
    private const val CLEAR_ALL_FIELD_FLAGS = 1
  }

  internal fun setAllClearFlag() {
    flag = CLEAR_ALL_FIELD_FLAGS
  }

  fun add(key: String) {
    set.add(key)
  }

  fun remove(key: String) {
    flag = NORMAL_FIELD_FLAGS
    set.remove(key)
  }

  fun has(key: String): Boolean {
    if (flag == CLEAR_ALL_FIELD_FLAGS) {
      return true
    }
    return set.contains(key)
  }
}
