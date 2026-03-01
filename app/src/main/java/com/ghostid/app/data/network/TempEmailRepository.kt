package com.ghostid.app.data.network

import com.ghostid.app.data.crypto.CryptoManager
import com.ghostid.app.data.db.AliasDao
import com.ghostid.app.data.db.TempEmailEntity
import com.ghostid.app.domain.generator.PasswordGenerator
import com.ghostid.app.domain.model.Alias
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TempEmailRepository @Inject constructor(
    private val mailTmService: MailTmService,
    private val dao: AliasDao,
    private val crypto: CryptoManager,
    private val passwordGenerator: PasswordGenerator,
) {

    suspend fun createInboxForAlias(alias: Alias): TempEmailEntity? = runCatching {
        val domains = mailTmService.getDomains().domains
        val domain = domains.firstOrNull()?.domain ?: return@runCatching null

        val first = alias.name.firstName.lowercase().replace(Regex("[^a-z]"), "")
        val last = alias.name.lastName.lowercase().replace(Regex("[^a-z]"), "")
        val address = "$first.$last@$domain"

        val inboxPassword = passwordGenerator.generate()
        val account = mailTmService.createAccount(CreateAccountRequest(address, inboxPassword))
        val tokenResponse = mailTmService.getToken(TokenRequest(address, inboxPassword))

        val entity = TempEmailEntity(
            aliasId = alias.id,
            accountId = account.id,
            address = address,
            encryptedPassword = crypto.encrypt(inboxPassword),
            encryptedToken = crypto.encrypt(tokenResponse.token),
        )
        dao.insertTempEmail(entity)
        entity
    }.getOrNull()

    suspend fun getInboxAddress(aliasId: String): String? =
        dao.getTempEmail(aliasId)?.address

    suspend fun fetchMessages(aliasId: String): List<MessageSummary> =
        withTokenRefresh(aliasId) { token ->
            mailTmService.getMessages(token).messages
        } ?: emptyList()

    suspend fun fetchMessageDetail(aliasId: String, messageId: String): MessageDetail? =
        withTokenRefresh(aliasId) { token ->
            mailTmService.getMessage(token, messageId)
        }

    suspend fun deleteMessage(aliasId: String, messageId: String) {
        withTokenRefresh(aliasId) { token ->
            mailTmService.deleteMessage(token, messageId)
        }
    }

    suspend fun deleteInbox(aliasId: String) {
        val entity = dao.getTempEmail(aliasId) ?: return
        runCatching {
            val token = crypto.decrypt(entity.encryptedToken)
            mailTmService.deleteAccount("Bearer $token", entity.accountId)
        }
        dao.deleteTempEmail(aliasId)
    }

    private suspend fun <T> withTokenRefresh(aliasId: String, block: suspend (String) -> T): T? {
        val entity = dao.getTempEmail(aliasId) ?: return null
        return try {
            val token = crypto.decrypt(entity.encryptedToken)
            block("Bearer $token")
        } catch (e: HttpException) {
            if (e.code() == 401) {
                val freshToken = refreshToken(entity) ?: return null
                runCatching { block("Bearer $freshToken") }.getOrNull()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun refreshToken(entity: TempEmailEntity): String? = runCatching {
        val password = crypto.decrypt(entity.encryptedPassword)
        val response = mailTmService.getToken(TokenRequest(entity.address, password))
        dao.updateTempEmailToken(entity.aliasId, crypto.encrypt(response.token))
        response.token
    }.getOrNull()
}
