package com.ghostid.app.domain.model

data class HealthCheckWarning(
    val duplicateValue: String,
    val affectedAliases: List<String>, // e.g. ["Alex Taylor (Signal)", "Jordan Brooks (Telegram)"]
)
