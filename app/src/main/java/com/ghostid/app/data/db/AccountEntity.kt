package com.ghostid.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    foreignKeys = [ForeignKey(
        entity = AliasEntity::class,
        parentColumns = ["id"],
        childColumns = ["aliasId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("aliasId")],
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val aliasId: String,
    val platformName: String,      // Platform.name()
    val username: String,
    val passwordEncrypted: String, // AES-256-GCM encrypted
)
