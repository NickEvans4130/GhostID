package com.ghostid.app.data.repository

import com.ghostid.app.data.crypto.CryptoManager
import com.ghostid.app.data.db.AccountEntity
import com.ghostid.app.data.db.AliasDao
import com.ghostid.app.data.db.AliasEntity
import com.ghostid.app.domain.model.Account
import com.ghostid.app.domain.model.AccountStatus
import com.ghostid.app.domain.model.Alias
import com.ghostid.app.domain.model.AliasAddress
import com.ghostid.app.domain.model.AliasName
import com.ghostid.app.domain.model.HealthCheckWarning
import com.ghostid.app.domain.model.Platform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AliasRepository @Inject constructor(
    private val dao: AliasDao,
    private val crypto: CryptoManager,
) {
    fun observeAllAliases(): Flow<List<Alias>> =
        combine(dao.observeAllAliases(), dao.observeAllAccounts()) { entities, accounts ->
            val accountMap = accounts.groupBy { it.aliasId }
            entities.map { entity ->
                entity.toDomain(crypto, accountMap[entity.id] ?: emptyList())
            }
        }

    suspend fun getAliasById(id: String): Alias? {
        val entity = dao.getAliasById(id) ?: return null
        val accounts = dao.getAccountsForAlias(id)
        return entity.toDomain(crypto, accounts)
    }

    suspend fun saveAlias(alias: Alias) {
        dao.insertAlias(alias.toEntity(crypto))
        dao.replaceAccountsForAlias(alias.id, alias.accounts.map { it.toEntity(crypto) })
    }

    suspend fun updateAlias(alias: Alias) {
        dao.updateAlias(alias.toEntity(crypto))
        dao.replaceAccountsForAlias(alias.id, alias.accounts.map { it.toEntity(crypto) })
    }

    suspend fun deleteAlias(aliasId: String) {
        dao.deleteAliasById(aliasId)
    }

    suspend fun getAllAliases(): List<Alias> {
        val entities = dao.getAllAliases()
        return entities.map { entity ->
            val accounts = dao.getAccountsForAlias(entity.id)
            entity.toDomain(crypto, accounts)
        }
    }

    /**
     * Scans all alias accounts for usernames that appear across more than one alias.
     * Usernames that are shared between accounts of the same alias (impossible by design) are ignored.
     * Returns a structured list of warnings with the duplicate value and which aliases are affected.
     */
    suspend fun runHealthCheck(): List<HealthCheckWarning> {
        val aliases = getAllAliases()

        // Map of lowercase username -> list of "Alias Name (Platform)" strings
        val usernameToOwners = mutableMapOf<String, MutableList<Pair<String, String>>>()

        for (alias in aliases) {
            for (account in alias.accounts) {
                val key = account.username.trim().lowercase()
                if (key.isEmpty()) continue
                usernameToOwners
                    .getOrPut(key) { mutableListOf() }
                    .add(alias.name.full to account.platform.displayName)
            }
        }

        return usernameToOwners
            .filter { (_, owners) ->
                // Only warn if the username appears in accounts belonging to different aliases
                owners.map { it.first }.distinct().size > 1
            }
            .map { (username, owners) ->
                HealthCheckWarning(
                    duplicateValue = username,
                    affectedAliases = owners.map { (name, platform) -> "$name ($platform)" },
                )
            }
            .sortedBy { it.duplicateValue }
    }

    // --- Mapping helpers ---

    private fun AliasEntity.toDomain(crypto: CryptoManager, accountEntities: List<AccountEntity>): Alias =
        Alias(
            id = id,
            name = AliasName(firstName, lastName),
            dateOfBirth = dateOfBirth,
            nationality = nationality,
            address = AliasAddress(street, city, postcode, country),
            phoneNumber = phoneNumber,
            occupation = occupation,
            starSign = starSign,
            bloodType = bloodType,
            bio = runCatching { crypto.decrypt(bioEncrypted) }.getOrDefault(bioEncrypted),
            gender = gender,
            photoPath = photoPath,
            accentColorInt = accentColorInt,
            accounts = accountEntities.map { it.toDomain(crypto) },
            tags = tags,
            notes = runCatching { crypto.decrypt(notesEncrypted) }.getOrDefault(""),
            createdAt = createdAt,
        )

    private fun AccountEntity.toDomain(crypto: CryptoManager): Account =
        Account(
            id = id,
            aliasId = aliasId,
            platform = runCatching { Platform.valueOf(platformName) }.getOrDefault(Platform.EMAIL_PROTON),
            username = username,
            password = runCatching { crypto.decrypt(passwordEncrypted) }.getOrDefault(""),
            status = runCatching { AccountStatus.valueOf(status) }.getOrDefault(AccountStatus.PENDING),
            accountCreatedAt = accountCreatedAt,
        )

    private fun Alias.toEntity(crypto: CryptoManager): AliasEntity =
        AliasEntity(
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
            bioEncrypted = runCatching { crypto.encrypt(bio) }.getOrDefault(bio),
            gender = gender,
            photoPath = photoPath,
            accentColorInt = accentColorInt,
            tags = tags,
            notesEncrypted = runCatching { crypto.encrypt(notes) }.getOrDefault(notes),
            createdAt = createdAt,
        )

    private fun Account.toEntity(crypto: CryptoManager): AccountEntity =
        AccountEntity(
            id = id,
            aliasId = aliasId,
            platformName = platform.name,
            username = username,
            passwordEncrypted = runCatching { crypto.encrypt(password) }.getOrDefault(password),
            status = status.name,
            accountCreatedAt = accountCreatedAt,
        )

    suspend fun updateAccountStatus(accountId: String, status: AccountStatus, createdAt: Long?) {
        dao.updateAccountStatus(accountId, status.name, createdAt)
    }
}
