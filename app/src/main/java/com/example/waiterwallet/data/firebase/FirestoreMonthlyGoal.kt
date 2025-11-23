package com.example.waiterwallet.data.firebase

import com.example.waiterwallet.data.MonthlyGoal
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import java.time.YearMonth

/**
 * Firestore-compatible data class for MonthlyGoal.
 */
data class FirestoreMonthlyGoal(
    @DocumentId
    val id: String = "", // Same as yearMonth
    
    @PropertyName("yearMonth")
    val yearMonth: String = "", // format: "2025-11"
    
    @PropertyName("goalTips")
    val goalTips: Double = 0.0,
    
    @PropertyName("commissionPercent")
    val commissionPercent: Double = 0.01,
    
    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @PropertyName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        yearMonth = "",
        goalTips = 0.0,
        commissionPercent = 0.01,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
    
    /**
     * Convert to Room entity for local cache
     */
    fun toRoomEntity(): MonthlyGoal {
        return MonthlyGoal(
            yearMonth = yearMonth,
            goalTips = goalTips,
            commissionPercent = commissionPercent
        )
    }
    
    companion object {
        /**
         * Convert from Room entity to Firestore document
         */
        fun fromRoomEntity(goal: MonthlyGoal): FirestoreMonthlyGoal {
            return FirestoreMonthlyGoal(
                id = goal.yearMonth,
                yearMonth = goal.yearMonth,
                goalTips = goal.goalTips,
                commissionPercent = goal.commissionPercent,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
