package com.ghostid.app.domain.usecase

import com.ghostid.app.data.crypto.CryptoManager
import com.ghostid.app.data.repository.AliasRepository
import com.ghostid.app.domain.model.Account
import com.ghostid.app.domain.model.Alias
import com.ghostid.app.domain.model.AliasAddress
import com.ghostid.app.domain.model.AliasName
import com.ghostid.app.domain.model.Platform
import com.ghostid.app.domain.model.aliasAccentColors
import com.google.gson.Gson
import java.io.File
import java.security.SecureRandom
import javax.inject.Inject

class ImportAliasUseCase @Inject constructor(
    private val repository: AliasRepository,
    private val crypto: CryptoManager,
    private val gson: Gson,
) {
    /**
     * Imports an alias from an encrypted JSON file at [filePath].
     * Returns the imported Alias on success, null on failure.
     */
    suspend fun invoke(filePath: String): Alias? = runCatching {
        val encrypted = File(filePath).readText()
        val json = crypto.decrypt(encrypted)
        val payload = gson.fromJson(json, AliasExportPayload::class.java)
        val data = payload.alias

        val rng = SecureRandom()
        val accentColor = aliasAccentColors[rng.nextInt(aliasAccentColors.size)]

        val alias = Alias(
            id = data.id,
            name = AliasName(data.firstName, data.lastName),
            dateOfBirth = data.dateOfBirth,
            nationality = data.nationality,
            address = AliasAddress(data.street, data.city, data.postcode, data.country),
            phoneNumber = data.phoneNumber,
            occupation = data.occupation,
            starSign = data.starSign,
            bloodType = data.bloodType,
            bio = data.bio,
            photoPath = null,
            accentColorInt = accentColor,
            accounts = data.accounts.map { acc ->
                Account(
                    aliasId = data.id,
                    platform = runCatching { Platform.valueOf(acc.platform) }.getOrDefault(Platform.EMAIL_PROTON),
                    username = acc.username,
                    password = acc.password,
                )
            },
            tags = data.tags,
            notes = data.notes,
            createdAt = data.createdAt,
        )

        repository.saveAlias(alias)
        alias
    }.getOrNull()
}
