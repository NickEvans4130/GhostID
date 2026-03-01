package com.ghostid.app.domain.model

import java.util.UUID

data class Account(
    val id: String = UUID.randomUUID().toString(),
    val aliasId: String,
    val platform: Platform,
    val username: String,
    val password: String,
    val status: AccountStatus = AccountStatus.PENDING,
    val accountCreatedAt: Long? = null,
)
