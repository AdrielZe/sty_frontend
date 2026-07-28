package com.example.training_tracker.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.training_tracker.MainActivity
import com.example.training_tracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WorkoutTimerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null

    private var accumulatedMs = 0L
    private var timerStartMs = 0L
    private var workoutId: String = ""
    private var isPaused = false

    private val prefs by lazy {
        getSharedPreferences("workout_timer_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_ACCUMULATED_MS = "EXTRA_ACCUMULATED_MS"
        const val EXTRA_WORKOUT_ID = "EXTRA_WORKOUT_ID"
        const val NOTIFICATION_WORKOUT_ID_KEY = "workoutId"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "workout_timer_channel"
        private const val PREF_ACCUMULATED_MS = "accumulated_ms"
        private const val PREF_TIMER_START_MS = "timer_start_ms"
        private const val PREF_WORKOUT_ID = "workout_id"
        private const val PREF_IS_PAUSED = "is_paused"

        private val _elapsedMs = MutableStateFlow(0L)
        val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

        private val _isPaused = MutableStateFlow(false)
        val isPausedFlow: StateFlow<Boolean> = _isPaused.asStateFlow()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                accumulatedMs = intent.getLongExtra(EXTRA_ACCUMULATED_MS, 0L)
                timerStartMs = System.currentTimeMillis()
                workoutId = intent.getStringExtra(EXTRA_WORKOUT_ID) ?: ""
                isPaused = false
                _isPaused.value = false
                saveState()
                startForegroundNotification()
                startTimer()
            }
            ACTION_PAUSE -> {
                accumulatedMs = _elapsedMs.value
                isPaused = true
                _isPaused.value = true
                timerJob?.cancel()
                saveState()
                updateNotification(_elapsedMs.value)
            }
            ACTION_RESUME -> {
                timerStartMs = System.currentTimeMillis()
                isPaused = false
                _isPaused.value = false
                saveState()
                startTimer()
            }
            ACTION_STOP -> {
                clearState()
                stopSelf()
            }
            null -> {
                // Reiniciado pelo OS via START_STICKY — restaura do estado salvo
                accumulatedMs = prefs.getLong(PREF_ACCUMULATED_MS, 0L)
                timerStartMs = prefs.getLong(PREF_TIMER_START_MS, System.currentTimeMillis())
                workoutId = prefs.getString(PREF_WORKOUT_ID, "") ?: ""
                isPaused = prefs.getBoolean(PREF_IS_PAUSED, false)
                _isPaused.value = isPaused
                if (accumulatedMs > 0L) {
                    startForegroundNotification()
                    if (!isPaused) startTimer()
                    else updateNotification(accumulatedMs)
                }
            }
        }
        return START_STICKY
    }

    private fun saveState() {
        prefs.edit()
            .putLong(PREF_ACCUMULATED_MS, accumulatedMs)
            .putLong(PREF_TIMER_START_MS, timerStartMs)
            .putString(PREF_WORKOUT_ID, workoutId)
            .putBoolean(PREF_IS_PAUSED, isPaused)
            .apply()
    }

    private fun clearState() {
        prefs.edit().clear().apply()
        _elapsedMs.value = 0L
        _isPaused.value = false
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                val elapsed = accumulatedMs + (System.currentTimeMillis() - timerStartMs)
                _elapsedMs.value = elapsed
                updateNotification(elapsed)
                delay(1000)
            }
        }
    }

    private fun startForegroundNotification() {
        val notification = buildNotification(_elapsedMs.value)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(elapsedMs: Long) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(elapsedMs))
    }

    private fun buildNotification(elapsedMs: Long): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(NOTIFICATION_WORKOUT_ID_KEY, workoutId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, WorkoutTimerService::class.java).apply {
                action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeLabel = if (isPaused)
            getString(R.string.notification_action_resume)
        else
            getString(R.string.notification_action_pause)

        val pauseResumeIcon = if (isPaused)
            android.R.drawable.ic_media_play
        else
            android.R.drawable.ic_media_pause

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_workout_title))
            .setContentText(getString(R.string.notification_workout_text, formatTime(elapsedMs)))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .addAction(pauseResumeIcon, pauseResumeLabel, pauseResumeIntent)
            .build()
    }

    private fun formatTime(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms / 60000) % 60
        val seconds = (ms / 1000) % 60
        return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
        else "%02d:%02d".format(minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        _elapsedMs.value = 0L
        _isPaused.value = false
    }
}
