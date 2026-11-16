package com.margy.musicbox.data.auth

import android.content.Context
import android.content.SharedPreferences

/**
 * Токены сервисов. Лежат в приватном SharedPreferences приложения,
 * наружу не отдаём и в git не коммитим.
 */
object TokenStore {

    private const val PREFS = "musicbox_tokens"
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    fun save(key: String, token: String) {
        prefs?.edit()?.putString(key, token)?.apply()
    }

    fun get(key: String): String? = prefs?.getString(key, null)

    fun clear(key: String) {
        prefs?.edit()?.remove(key)?.apply()
    }

    const val YANDEX = "yandex"
    const val VK = "vk"
}
