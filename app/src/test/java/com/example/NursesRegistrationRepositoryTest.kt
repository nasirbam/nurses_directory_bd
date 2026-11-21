package com.example

import com.example.data.AdminEntity
import com.example.data.AdminRole
import com.example.data.ApprovalStatus
import com.example.data.AvailabilityStatus
import com.example.data.BangladeshData
import com.example.data.NurseEntity
import com.example.data.NursesRepository
import com.example.data.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NursesRegistrationRepositoryTest {

    @Test
    fun nurseEntity_safeEnumFallbacksHandleSchemaDriftWithoutCrashing() {
        val unknownNurse = NurseEntity(
            fullName = "Tasnim Tabassum",
            bnmcNumber = "BNMC-99001",
            approvalStatus = "UNKNOWN_STATUS",
            availabilityStatus = "UNKNOWN_SHIFT"
        )
        assertEquals(ApprovalStatus.PENDING, unknownNurse.approvalEnum)
        assertEquals(AvailabilityStatus.ON_DUTY, unknownNurse.availabilityEnum)
    }

    @Test
    fun adminEntity_safeRoleFallbackAndBootstrapEmailAreConfigured() {
        val unknownAdmin = AdminEntity(
            fullName = "Test Admin",
            email = "test@gmail.com",
            role = "LEGACY_ROLE"
        )
        assertEquals(AdminRole.MODERATOR, unknownAdmin.roleEnum)
        assertEquals("mdnasirnaznin@gmail.com", NursesRepository.BOOTSTRAP_ADMIN_EMAIL)
    }

    @Test
    fun registrationValidationHelpers_validateBangladeshContactsAndEmails() {
        assertTrue(SecurityUtils.isValidBangladeshPhone("01711223344"))
        assertTrue(SecurityUtils.isValidBangladeshPhone("+8801819334510"))
        assertFalse(SecurityUtils.isValidBangladeshPhone("12345"))

        assertTrue(SecurityUtils.isValidEmail("nurse@dmch.gov.bd"))
        assertFalse(SecurityUtils.isValidEmail("invalid-email"))

        assertEquals(8, BangladeshData.divisions.size)
        assertEquals(64, BangladeshData.districtsByDivision.values.sumOf { it.size })
    }
}
