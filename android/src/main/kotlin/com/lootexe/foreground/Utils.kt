package com.lootexe.foreground

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lootexe.foreground.model.IconConfiguration
import com.lootexe.foreground.model.NotificationConfiguration

class Utils {
    companion object {
        fun getIconResourceId(context: Context, config: IconConfiguration?): Int {
            with (context) {
                return if (config == null) {
                    val manager = packageManager
                    manager.getApplicationInfo(
                        packageName,
                        PackageManager.GET_META_DATA
                    ).icon
                } else {
                    resources.getIdentifier(
                        config.name,
                        config.resourceType,
                        packageName)
                }
            }
        }

        fun getPendingIntent(context: Context): PendingIntent {
            val manager = context.packageManager
            val intent = manager.getLaunchIntentForPackage(context.packageName)
            //val intent = Intent(context, ForegroundService::class.java)

            return PendingIntent.getActivity(
                context,
                20000,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE
                } else {
                    0
                })
        }

        fun getNotification(context: Context,
                            config: NotificationConfiguration,
                            iconId: Int,
                            pendingIntent: PendingIntent): Notification {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                with (Notification.Builder(context, config.channel.id)) {
                    setOngoing(true)
                    setSmallIcon(iconId)
                    setContentIntent(pendingIntent)
                    setContentTitle(config.title)
                    setContentText(config.text)
                    setVisibility(config.visibility)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                    }

                    build()
                }
            } else {
                with (NotificationCompat.Builder(context, config.channel.id)) {
                    setOngoing(true)
                    setSmallIcon(iconId)
                    setContentIntent(pendingIntent)
                    setContentTitle(config.title)
                    setContentText(config.text)
                    setVisibility(config.visibility)
                    priority = config.channel.importance

                    build()
                }
            }
        }
    }
}