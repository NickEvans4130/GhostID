package com.ghostid.app.domain.generator

import com.ghostid.app.domain.model.Account
import com.ghostid.app.domain.model.AliasName
import com.ghostid.app.domain.model.Platform
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountSuggester @Inject constructor(
    private val passwordGenerator: PasswordGenerator,
) {
    private val secureRandom = SecureRandom()

    fun suggestAccounts(aliasId: String, name: AliasName, phoneNumber: String): List<Account> {
        val first = name.firstName.lowercase().replace(" ", "")
        val last = name.lastName.lowercase().replace(" ", "")
        val suffix = randomNumericSuffix(4)

        return Platform.entries.map { platform ->
            Account(
                aliasId = aliasId,
                platform = platform,
                username = generateUsername(platform, first, last, suffix, phoneNumber),
                password = passwordGenerator.generate(),
            )
        }
    }

    private fun generateUsername(
        platform: Platform,
        first: String,
        last: String,
        suffix: String,
        phone: String,
    ): String = when (platform) {
        Platform.SIGNAL -> "@$first.$last$suffix"
        Platform.TELEGRAM -> "@${first}_${last}_$suffix"
        Platform.DISCORD -> "${first}${last}#${randomNumericSuffix(4)}"
        Platform.WHATSAPP -> phone
        Platform.MATRIX -> "@${first}.${last}_$suffix:matrix.org"

        Platform.INSTAGRAM -> "${first}.${last}.$suffix"
        Platform.TWITTER -> "@${first}_${last}_$suffix"
        Platform.BLUESKY -> "${first}${last}$suffix.bsky.social"
        Platform.MASTODON -> "@${first}.${last}_$suffix@mastodon.social"
        Platform.TIKTOK -> "@${first}.${last}.$suffix"
        Platform.REDDIT -> "u/${first}_${last}_$suffix"
        Platform.TUMBLR -> "${first}-${last}-$suffix"
        Platform.PINTEREST -> "${first}${last}$suffix"

        Platform.LINKEDIN -> "$first-$last-$suffix"
        Platform.GITHUB -> "${first}-${last}-$suffix"

        Platform.EMAIL_PROTON -> "${first}.${last}${suffix}@proton.me"
        Platform.EMAIL_TUTA -> "${first[0]}${last}${suffix}@tuta.com"
        Platform.EMAIL_DISROOT -> "${first}${last}${suffix}@disroot.org"
    }

    private fun randomNumericSuffix(length: Int): String =
        (1..length).map { secureRandom.nextInt(10) }.joinToString("")
}
