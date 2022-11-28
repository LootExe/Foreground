package com.lootexe.foreground.model

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException

data class ServiceConfiguration(val notification: NotificationConfiguration,
                                val runOnBoot: Boolean) {
    companion object {
        private const val KEY = "ServiceConfiguration"

        fun fromJson(json: String): ServiceConfiguration {
            return with(GsonBuilder()) {
                serializeNulls()
                create()
            }.fromJson(json, ServiceConfiguration::class.java)
        }

        fun loadFromPreferences(context: Context): ServiceConfiguration? {
            val jsonString = with(context) {
                getSharedPreferences(packageName, Context.MODE_PRIVATE)
                    .getString(KEY, null)
            }

            jsonString ?: return null

            return try {
                fromJson(jsonString)
            } catch (e: JsonParseException) {
                Log.e(ServiceConfiguration::class.java.toString(), e.message.toString())
                return null
            }
        }
    }

    fun toJson(): String {
        return with(GsonBuilder()) {
            serializeNulls()
            create()
        }.toJson(this)
    }

    fun saveToPreferences(context: Context) {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val json = this.toJson()

        with(prefs.edit()) {
            putString(KEY, json)
            commit()
        }
    }

    fun clearPreferences(context: Context) {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

        with(prefs.edit()) {
            clear()
            commit()
        }
    }
}