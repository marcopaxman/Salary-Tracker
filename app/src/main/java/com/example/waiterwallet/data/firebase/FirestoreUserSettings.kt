package com.example.waiterwallet.data.firebase

import com.google.firebase.firestore.PropertyName

/**
 * Firestore-compatible data class for user settings.
 * Stored in users/{userId}/settings/preferences
 */
data class FirestoreUserSettings(
    @PropertyName("commissionPercent")
    val commissionPercent: Double = 0.01,
    
    @PropertyName("reminderEnabled")
    val reminderEnabled: Boolean = true,
    
    @PropertyName("reminderHour")
    val reminderHour: Int = 22,
    
    @PropertyName("reminderMinute")
    val reminderMinute: Int = 0,
    
    @PropertyName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        commissionPercent = 0.01,
        reminderEnabled = true,
        reminderHour = 22,
        reminderMinute = 0,
        updatedAt = System.currentTimeMillis()
    )
}
