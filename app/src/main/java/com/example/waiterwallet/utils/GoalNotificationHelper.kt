package com.example.waiterwallet.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.waiterwallet.MainActivity
import com.example.waiterwallet.R

object GoalNotificationHelper {
    
    private const val CHANNEL_ID = "goal_achievement_channel"
    private const val NOTIFICATION_ID = 200
    
    fun showGoalAchievedNotification(context: Context, goalAmount: Double, currentAmount: Double) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Goal Achievements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when you reach your earnings goals"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🎉 Goal Achieved!")
            .setContentText("Congratulations! You've reached your monthly tips goal of R${"%.2f".format(goalAmount)}!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Congratulations! You've reached your monthly tips goal of R${"%.2f".format(goalAmount)}. Current total: R${"%.2f".format(currentAmount)}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    fun showMilestoneNotification(context: Context, percentage: Int, goalAmount: Double, currentAmount: Double) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Goal Achievements",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when you reach your earnings goals"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$percentage% of Goal Reached!")
            .setContentText("You're $percentage% to your goal! Current: R${"%.2f".format(currentAmount)} / Goal: R${"%.2f".format(goalAmount)}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID + percentage, notification)
    }
}
