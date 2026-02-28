package com.ghostid.app

import com.ghostid.app.domain.generator.PasswordGenerator
import com.ghostid.app.domain.generator.PasswordStrength
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordGeneratorTest {

    private val generator = PasswordGenerator()

    @Test
    fun `generated password meets minimum length`() {
        val password = generator.generate()
        assertTrue("Password should be at least 24 chars", password.length >= 24)
    }

    @Test
    fun `generated password contains required character classes`() {
        val password = generator.generate()
        assertTrue("Must contain uppercase", password.any { it.isUpperCase() })
        assertTrue("Must contain lowercase", password.any { it.isLowerCase() })
        assertTrue("Must contain digit", password.any { it.isDigit() })
        assertTrue("Must contain symbol", password.any { it in "!@#\$%^&*()-_=+[]{}|;:,.<>?" })
    }

    @Test
    fun `generated passwords are unique`() {
        val passwords = (1..100).map { generator.generate() }.toSet()
        assertTrue("Passwords should be unique", passwords.size > 95)
    }

    @Test
    fun `strength of generated password is STRONG`() {
        val password = generator.generate(28)
        assertEquals(PasswordStrength.STRONG, generator.passwordStrength(password))
    }
}
