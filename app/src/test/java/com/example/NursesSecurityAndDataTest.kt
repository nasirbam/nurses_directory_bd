package com.example

import com.example.data.BangladeshData
import com.example.data.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NursesSecurityAndDataTest {

    @Test
    fun bangladeshDivisionsAndDistricts_coverAll8DivisionsAnd64Districts() {
        assertEquals(8, BangladeshData.divisions.size)
        val totalDistricts = BangladeshData.districtsByDivision.values.sumOf { it.size }
        assertEquals(64, totalDistricts)
    }

    @Test
    fun phoneValidation_acceptsValidBangladeshFormats() {
        assertTrue(SecurityUtils.isValidBangladeshPhone("01711450218"))
        assertTrue(SecurityUtils.isValidBangladeshPhone("+8801819334510"))
        assertTrue(SecurityUtils.isValidBangladeshPhone("019 1277-8904"))
        assertFalse(SecurityUtils.isValidBangladeshPhone("12345"))
        assertFalse(SecurityUtils.isValidBangladeshPhone("01211450218"))
    }

    @Test
    fun whatsappFormatting_normalizesToInternationalDigits() {
        assertEquals("8801711450218", SecurityUtils.formatWhatsAppDigits("01711450218"))
        assertEquals("8801819334510", SecurityUtils.formatWhatsAppDigits("+8801819334510"))
    }

    @Test
    fun saltedPinHashing_verifiesMatchingPinAndRejectsWrongPin() {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPin("4829", salt)
        assertTrue(SecurityUtils.verifyPin("4829", salt, hash))
        assertFalse(SecurityUtils.verifyPin("9999", salt, hash))
    }
}
