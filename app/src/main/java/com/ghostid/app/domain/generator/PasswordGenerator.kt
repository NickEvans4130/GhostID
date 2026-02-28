package com.ghostid.app.domain.generator

import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordGenerator @Inject constructor() {

    private val secureRandom = SecureRandom()

    private val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val lowercase = "abcdefghijklmnopqrstuvwxyz"
    private val digits = "0123456789"
    private val symbols = "!@#\$%^&*()-_=+[]{}|;:,.<>?"

    private val fullAlphabet = uppercase + lowercase + digits + symbols

    /**
     * Generate a cryptographically secure password of at least [length] characters.
     * Guarantees at least one character from each character class.
     */
    fun generate(length: Int = 28): String {
        require(length >= 24) { "Password length must be at least 24 characters" }

        val passwordChars = CharArray(length)

        // Ensure at least one of each class
        passwordChars[0] = uppercase[secureRandom.nextInt(uppercase.length)]
        passwordChars[1] = lowercase[secureRandom.nextInt(lowercase.length)]
        passwordChars[2] = digits[secureRandom.nextInt(digits.length)]
        passwordChars[3] = symbols[secureRandom.nextInt(symbols.length)]

        for (i in 4 until length) {
            passwordChars[i] = fullAlphabet[secureRandom.nextInt(fullAlphabet.length)]
        }

        // Fisher-Yates shuffle to avoid predictable positions
        for (i in length - 1 downTo 1) {
            val j = secureRandom.nextInt(i + 1)
            val tmp = passwordChars[i]
            passwordChars[i] = passwordChars[j]
            passwordChars[j] = tmp
        }

        return String(passwordChars)
    }

    fun passwordStrength(password: String): PasswordStrength {
        var score = 0
        if (password.length >= 16) score++
        if (password.length >= 24) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { it in symbols }) score++
        return when {
            score <= 2 -> PasswordStrength.WEAK
            score <= 4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
}

enum class PasswordStrength { WEAK, MEDIUM, STRONG }
