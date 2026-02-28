package com.ghostid.app.domain.usecase

import android.content.Context
import com.ghostid.app.data.crypto.CryptoManager
import com.ghostid.app.data.repository.AliasRepository
import com.ghostid.app.domain.model.Alias
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

data class AliasExportPayload(
    val version: Int = 1,
    val alias: AliasExportData,
)

data class AliasExportData(
    val id: String,
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
    val bio: String,
    val accounts: List<AccountExportData>,
    val tags: List<String>,
    val notes: String,
    val createdAt: Long,
)

data class AccountExportData(
    val platform: String,
    val username: String,
    val password: String,
)

class ExportAliasUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AliasRepository,
    private val crypto: CryptoManager,
    private val gson: Gson,
) {
    /**
     * Exports the alias to an encrypted JSON file in the app's external files dir.
     * Returns the file path on success.
     */
    suspend fun invoke(aliasId: String): String? {
        val alias = repository.getAliasById(aliasId) ?: return null
        val payload = alias.toExportPayload()
        val json = gson.toJson(payload)
        val encrypted = crypto.encrypt(json)

        val exportsDir = File(context.filesDir, "exports").also { it.mkdirs() }
        val fileName = "ghostid_${alias.name.full.replace(" ", "_")}_${alias.id.take(8)}.json.enc"
        val file = File(exportsDir, fileName)
        file.writeText(encrypted)
        return file.absolutePath
    }

    private fun Alias.toExportPayload() = AliasExportPayload(
        alias = AliasExportData(
            id = id,
            firstName = name.firstName,
            lastName = name.lastName,
            dateOfBirth = dateOfBirth,
            nationality = nationality,
            street = address.street,
            city = address.city,
            postcode = address.postcode,
            country = address.country,
            phoneNumber = phoneNumber,
            occupation = occupation,
            starSign = starSign,
            bloodType = bloodType,
            bio = bio,
            accounts = accounts.map { acc ->
                AccountExportData(
                    platform = acc.platform.name,
                    username = acc.username,
                    password = acc.password,
                )
            },
            tags = tags,
            notes = notes,
            createdAt = createdAt,
        )
    )
}
