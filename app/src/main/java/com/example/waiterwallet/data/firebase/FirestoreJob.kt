package com.example.waiterwallet.data.firebase

import com.example.waiterwallet.data.Job
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Firestore-compatible data class for Job.
 */
data class FirestoreJob(
    @DocumentId
    val id: String = "",
    
    @PropertyName("name")
    val name: String = "",
    
    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @PropertyName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        name = "",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
    
    /**
     * Convert to Room entity for local cache
     */
    fun toRoomEntity(): Job {
        return Job(
            id = id.toLongOrNull() ?: 0L,
            name = name
        )
    }
    
    companion object {
        /**
         * Convert from Room entity to Firestore document
         */
        fun fromRoomEntity(job: Job): FirestoreJob {
            return FirestoreJob(
                id = if (job.id == 0L) "" else job.id.toString(),
                name = job.name,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
