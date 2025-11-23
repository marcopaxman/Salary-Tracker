package com.example.waiterwallet.data.firebase

import com.example.waiterwallet.data.DailyEntry
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import java.time.LocalDate

/**
 * Firestore-compatible data class for DailyEntry.
 * Uses String for dates (ISO-8601 format) to work with Firestore.
 */
data class FirestoreDailyEntry(
    @DocumentId
    val id: String = "",
    
    @PropertyName("date")
    val date: String = "", // ISO-8601 format: "2025-11-23"
    
    @PropertyName("turnover")
    val turnover: Double = 0.0,
    
    @PropertyName("tipsCash")
    val tipsCash: Double? = null,
    
    @PropertyName("tipsCard")
    val tipsCard: Double? = null,
    
    @PropertyName("notes")
    val notes: String? = null,
    
    @PropertyName("jobId")
    val jobId: String? = null,
    
    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @PropertyName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        date = "",
        turnover = 0.0,
        tipsCash = null,
        tipsCard = null,
        notes = null,
        jobId = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
    
    /**
     * Convert to Room entity for local cache
     */
    fun toRoomEntity(): DailyEntry {
        return DailyEntry(
            date = LocalDate.parse(date),
            turnover = turnover,
            tipsCash = tipsCash,
            tipsCard = tipsCard,
            notes = notes,
            jobId = jobId?.toLongOrNull()
        )
    }
    
    companion object {
        /**
         * Convert from Room entity to Firestore document
         */
        fun fromRoomEntity(entry: DailyEntry): FirestoreDailyEntry {
            val dateString = entry.date.toString()
            return FirestoreDailyEntry(
                id = dateString, // Use date as document ID for easy querying
                date = dateString,
                turnover = entry.turnover,
                tipsCash = entry.tipsCash,
                tipsCard = entry.tipsCard,
                notes = entry.notes,
                jobId = entry.jobId?.toString(),
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
