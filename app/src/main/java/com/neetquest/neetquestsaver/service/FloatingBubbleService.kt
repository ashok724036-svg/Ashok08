package com.neetquest.neetquestsaver.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.view.*
import androidx.core.app.NotificationCompat
import com.neetquest.neetquestsaver.R
import com.neetquest.neetquestsaver.ui.CaptureRequestActivity
import kotlin.math.abs

class FloatingBubbleService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var params: WindowManager.LayoutParams? = null

    companion object {
        const val CHANNEL_ID   = "bubble_channel"
        const val NOTIF_ID     = 1001
        const val ACTION_START = "START"
        const val ACTION_STOP  = "STOP"

        fun start(context: Context) {
            val i = Intent(context, FloatingBubbleService::class.java).apply { action = ACTION_START }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(i)
            else context.startService(i)
        }

        fun stop(context: Context) {
            val i = Intent(context, FloatingBubbleService::class.java).apply { action = ACTION_STOP }
            context.startService(i)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createChannel()
        startForeground(NOTIF_ID, buildNotif())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> addBubble()
            ACTION_STOP  -> { removeBubble(); stopSelf() }
        }
        return START_STICKY
    }

    override fun onDestroy() { removeBubble(); super.onDestroy() }

    private fun addBubble() {
        if (bubbleView != null) return

        bubbleView = LayoutInflater.from(this)
            .inflate(R.layout.view_floating_bubble, null)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100; y = 400
        }

        var initX = 0; var initY = 0
        var touchX = 0f; var touchY = 0f
        var moved = false

        bubbleView!!.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    initX = params!!.x; initY = params!!.y
                    touchX = e.rawX; touchY = e.rawY
                    moved = false; true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - touchX).toInt()
                    val dy = (e.rawY - touchY).toInt()
                    if (abs(dx) > 8 || abs(dy) > 8) moved = true
                    params!!.x = initX + dx
                    params!!.y = initY + dy
                    windowManager.updateViewLayout(bubbleView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        val intent = Intent(this, CaptureRequestActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                    true
                }
                else -> false
            }
        }
        windowManager.addView(bubbleView, params)
    }

    private fun removeBubble() {
        try { bubbleView?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        bubbleView = null
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, "Floating Bubble",
                NotificationManager.IMPORTANCE_LOW).also {
                it.setShowBadge(false)
                getSystemService(NotificationManager::class.java).createNotificationChannel(it)
            }
        }
    }

    private fun buildNotif(): Notification {
        val stopPi = PendingIntent.getService(
            this, 0,
            Intent(this, FloatingBubbleService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NEETQuestSaver Active")
            .setContentText("Tap bubble to capture questions")
            .setSmallIcon(R.drawable.ic_capture_bubble)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(0, "Disable", stopPi)
            .build()
    }
}
