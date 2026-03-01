package com.ghostid.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "temp_emails",
    foreignKeys = [ForeignKey(
        entity = AliasEntity::class,
        parentColumns = ["id"],
        childColumns = ["aliasId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("aliasId")],
)
data class TempEmailEntity(
    @PrimaryKey val aliasId: String,
    val accountId: String,
    val address: String,
    val encryptedPassword: String,
    val encryptedToken: String,
    val tokenExpiresAt: Long = 0L,
)
