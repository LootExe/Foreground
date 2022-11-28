package com.lootexe.foreground

import android.content.Context
import android.util.Log

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.JSONMethodCodec
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject

/** ForegroundPlugin */
class ForegroundPlugin: FlutterPlugin, MethodChannel.MethodCallHandler {
  private lateinit var methodChannel: MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    context = binding.applicationContext
    methodChannel = MethodChannel(
      binding.binaryMessenger,
      "com.lootexe.foreground.method",
      JSONMethodCodec.INSTANCE
    )

    methodChannel.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    methodChannel.setMethodCallHandler(null)
  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    when (call.method) {
      "startService" -> {
        val json = call.arguments as? JSONObject

        if (json == null) {
          Log.e(javaClass.simpleName,
            "onMethodCall(): call.arguments is not a valid JSONObject")
          result.success(false)
        } else {
          ForegroundService.startService(context, json.toString())
          result.success(true)
        }
      }
      "stopService" -> {
        ForegroundService.stopService(context)
        result.success(true)
      }
      "isRunning" -> result.success(ForegroundService.isRunning)
      else -> result.notImplemented()
    }
  }
}
