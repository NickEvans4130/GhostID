package com.ghostid.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for an alias. Sensitive fields (bio, notes) are stored encrypted.
 * Passwords are stored encrypted in AccountEntity.
 */
@Entity(tableName = "aliases")
data class AliasEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val nationality: String,
    val street: String,
    val city: String,
    val postcode: String,
    val country: String,
    val phoneNumber: String,
    val occupation: String,
    val starSign: String,
    val bloodType: String,
    val bioEncrypted: String,          // AES-256-GCM encrypted
    val photoPath: String?,
    val accentColorInt: Int,
    val tags: List<String>,            // stored as JSON via Converters
    val notesEncrypted: String,        // AES-256-GCM encrypted
    val createdAt: Long,
)
