package org.markensic.commonlib.reflect

import java.lang.reflect.Field
import java.lang.reflect.Modifier

fun <T> Class<T>.getAndRemoveFinalModifier(fieldName: String): Field {
  return try {
    getDeclaredField(fieldName).apply {
      isAccessible = true
      val accessFlagsField = this::class.java.getDeclaredField("accessFlags")
      accessFlagsField.isAccessible = true
      accessFlagsField.setInt(this, modifiers.and(Modifier.FINAL.inv()))
    }
  } catch (e: Exception) {
    throw e
  }
}
