package com.ghostid.app.domain.model

import java.util.UUID

data class AliasName(val firstName: String, val lastName: String) {
    val full: String get() = "$firstName $lastName"
    val initials: String get() = "${firstName.first().uppercaseChar()}${lastName.first().uppercaseChar()}"
}

data class AliasAddress(
    val street: String,
    val city: String,
    val postcode: String,
    val country: String,
) {
    val formatted: String get() = "$street, $city, $postcode, $country"
}

data class Alias(
    val id: String = UUID.randomUUID().toString(),
    val name: AliasName,
    val dateOfBirth: String,          // ISO-8601 date string: YYYY-MM-DD
    val nationality: String,
    val address: AliasAddress,
    val phoneNumber: String,
    val occupation: String,
    val starSign: String,
    val bloodType: String,
    val bio: String,
    val photoPath: String?,           // absolute path to cached face JPEG, null = use avatar
    val accentColorInt: Int,          // ARGB int from aliasAccentColors
    val accounts: List<Account> = emptyList(),
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
