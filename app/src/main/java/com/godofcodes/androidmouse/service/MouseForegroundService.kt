package com.godofcodes.androidmouse.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.godofcodes.androidmouse.R
import com.godofcodes.androidmouse.domain.jiggler.JigglerController
import com.godofcodes.androidmouse.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MouseForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "mouse_service_channel"
        private const val NOTIFICATION_ID = 1

        fun startIntent(context: Context) =
            Intent(context, MouseForegroundService::class.java).also {
                it.action = ACTION_START
            }

        private const val ACTION_START = "START"
        const val ACTION_STOP_JIGGLER = "STOP_JIGGLER"
    }

    @Inject lateinit var jigglerController: JigglerController

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                createNotificationChannel()
                startForeground(NOTIFICATION_ID, buildNotification(jigglerActive = true))
                observeJigglerState()
            }
            ACTION_STOP_JIGGLER -> {
                jigglerController.stop()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun observeJigglerState() {
        serviceScope.launch {
            jigglerController.isRunning.collect { running ->
                if (running) {
                    updateNotification(jigglerActive = true)
                } else {
                    stopSelf()
                }
            }
        }
    }

    private fun updateNotification(jigglerActive: Boolean) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(jigglerActive))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun buildNotification(jigglerActive: Boolean): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val stopJigglerIntent = PendingIntent.getService(
            this, 1,
            Intent(this, MouseForegroundService::class.java).apply {
                action = ACTION_STOP_JIGGLER
            },
            PendingIntent.FLAG_IMMUTABLE,
        )
        val contentText = if (jigglerActive) {
            getString(R.string.notification_text_jiggler)
        } else {
            getString(R.string.notification_text)
        }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(openIntent)
            .setOngoing(true)
        if (jigglerActive) {
            builder.addAction(0, getString(R.string.notification_stop_jiggler), stopJigglerIntent)
        }
        return builder.build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
