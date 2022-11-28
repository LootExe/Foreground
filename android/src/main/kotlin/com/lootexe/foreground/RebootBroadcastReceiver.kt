package com.lootexe.foreground

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

import com.lootexe.foreground.model.ServiceConfiguration

class RebootBroadcastReceiver: BroadcastReceiver() {
    companion object {
        /**
         * Schedules this [RebootBroadcastReceiver] to be run whenever the Android device reboots.
         */
        fun enableRunOnReboot(context: Context) {
            setRunOnReboot(context, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        }

        /**
         * Unschedules this [RebootBroadcastReceiver] to be run whenever the Android device reboots.
         * This [RebootBroadcastReceiver] will no longer be run upon reboot.
         */
        fun disableRunOnReboot(context: Context) {
            setRunOnReboot(context, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
        }

        private fun setRunOnReboot(context: Context, state: Int) {
            with(context.packageManager) {
                val receiver = ComponentName(context, RebootBroadcastReceiver::class.java)
                setComponentEnabledSetting(receiver, state, PackageManager.DONT_KILL_APP)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return

        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val config = ServiceConfiguration.loadFromPreferences(context)
        config ?: return

        ForegroundService.startService(context, config.toJson())
    }
}