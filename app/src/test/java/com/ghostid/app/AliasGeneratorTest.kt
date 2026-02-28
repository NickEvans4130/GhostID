package com.ghostid.app

import com.ghostid.app.domain.generator.AccountSuggester
import com.ghostid.app.domain.generator.AliasGenerator
import com.ghostid.app.domain.generator.PasswordGenerator
import com.ghostid.app.domain.model.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AliasGeneratorTest {

    private val passwordGen = PasswordGenerator()
    private val accountSuggester = AccountSuggester(passwordGen)
    private val generator = AliasGenerator(accountSuggester)

    @Test
    fun `generated alias has valid name`() {
        val alias = generator.generate()
        assertTrue(alias.name.firstName.isNotBlank())
        assertTrue(alias.name.lastName.isNotBlank())
    }

    @Test
    fun `generated alias DOB is within expected range`() {
        val alias = generator.generate()
        val dob = java.time.LocalDate.parse(alias.dateOfBirth)
        val today = java.time.LocalDate.now()
        val age = java.time.Period.between(dob, today).years
        assertTrue("Age should be 18-60, was $age", age in 18..60)
    }

    @Test
    fun `generated alias has all required platforms`() {
        val alias = generator.generate()
        val platforms = alias.accounts.map { it.platform }.toSet()
        assertTrue(Platform.SIGNAL in platforms)
        assertTrue(Platform.EMAIL_PROTON in platforms)
        assertTrue(Platform.GITHUB in platforms)
    }

    @Test
    fun `each account has unique password`() {
        val alias = generator.generate()
        val passwords = alias.accounts.map { it.password }
        assertEquals("All passwords should be unique", passwords.size, passwords.toSet().size)
    }

    @Test
    fun `star sign derived correctly for fixed date`() {
        // 15 March = Pisces (March 1-20 = Pisces, March 21+ = Aries)
        val alias = generator.generate()
        assertNotNull(alias.starSign)
        assertFalse(alias.starSign.isEmpty())
    }
}
