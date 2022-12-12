package com.lootexe.foreground

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.google.gson.JsonParseException

import com.lootexe.foreground.model.ServiceConfiguration
import io.flutter.FlutterInjector

import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation

class ForegroundService: Service() {
    companion object {
        private const val DOMAIN = "com.lootexe.foreground"

        const val ACTION_SERVICE_START = "$DOMAIN.SERVICE_START"
        const val ACTION_SERVICE_STOP = "$DOMAIN.SERVICE_STOP"

        var isRunning = false
            private set

        private fun start(context: Context, intent: Intent) {
            context.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        }

        fun startService(context: Context, json: String) {
            if (isRunning) {
                return
            }

            val intent = Intent(context, ForegroundService::class.java).apply {
                action = ACTION_SERVICE_START
                putExtra("config", json)
            }

            start(context, intent)
        }

        fun stopService(context: Context) {
            if (!isRunning) {
                return
            }

            val intent = Intent(context, ForegroundService::class.java).apply {
                action = ACTION_SERVICE_STOP
            }

            start(context, intent)
        }
    }

    private var engine: FlutterEngine? = null
    private var backgroundChannel: MethodChannel? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent ?: return START_NOT_STICKY

        return when (intent.action ?: "") {
            ACTION_SERVICE_START -> {
                startForeground(intent.getStringExtra("config"))
            }
            ACTION_SERVICE_STOP -> {
                stopForeground()
            }
            else -> START_NOT_STICKY
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RebootBroadcastReceiver.disableRunOnReboot(this)
    }

    private fun startForeground(jsonConfig: String?): Int {
        if (isRunning) {
            return START_STICKY
        }

        jsonConfig ?: return START_NOT_STICKY

        val config = try {
            ServiceConfiguration.fromJson(jsonConfig)
        } catch (e: JsonParseException) {
            Log.e(javaClass.simpleName, e.message.toString())
            return START_NOT_STICKY
        }

        createFlutterEngine(config)
        configureReboot(config)
        createNotificationChannel(config)
        val notification = buildNotification(config)

        startForeground(1000, notification)
        isRunning = true
        return START_STICKY
    }

    private fun stopForeground(): Int {
        if (!isRunning) {
            return START_NOT_STICKY
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }

        backgroundChannel?.invokeMethod("onStopped", null)
        engine?.destroy()
        engine = null

        stopSelf()
        isRunning = false
        return START_NOT_STICKY
    }

    private fun configureReboot(config: ServiceConfiguration) {
        if (config.runOnBoot) {
            config.saveToPreferences(this)
            RebootBroadcastReceiver.enableRunOnReboot(this)
        } else {
            config.clearPreferences(this)
            RebootBroadcastReceiver.disableRunOnReboot(this)
        }
    }

    private fun createNotificationChannel(config: ServiceConfiguration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = with(config) {
                NotificationChannel(
                    notification.channel.id,
                    notification.channel.name,
                    notification.channel.importance).apply {
                    description = notification.channel.description
                }
            }

            with(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
                createNotificationChannel(channel)
            }
        }
    }

    private fun buildNotification(config: ServiceConfiguration): Notification {
        val pendingIntent = Utils.getPendingIntent(this)
        val iconId = Utils.getIconResourceId(applicationContext, config.notification.icon)
        return Utils.getNotification(this, config.notification, iconId, pendingIntent)
    }

    private fun createFlutterEngine(config: ServiceConfiguration) {
        config.callback ?: return

        if (engine != null) {
            return
        }

        engine = FlutterEngine(this)
        engine ?: return

        val loader = FlutterInjector.instance().flutterLoader()

        if (!loader.initialized()) {
            loader.startInitialization(applicationContext)
        }

        loader.ensureInitializationComplete(applicationContext, null)

        val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(config.callback)
        val dartBundlePath = loader.findAppBundlePath()

        val handler = MethodChannel.MethodCallHandler { call, _ ->
            when (call.method) {
                "initialize" ->
                    backgroundChannel?.invokeMethod("onStarted", null)
            }
        }

        engine?.apply {
            backgroundChannel = MethodChannel(dartExecutor,
                "com.lootexe.foreground.method.background")
            backgroundChannel?.setMethodCallHandler(handler)

            dartExecutor.executeDartCallback(
                DartExecutor.DartCallback(
                    applicationContext.assets,
                    dartBundlePath,
                    callbackInfo
                )
            )
        }
    }
}
