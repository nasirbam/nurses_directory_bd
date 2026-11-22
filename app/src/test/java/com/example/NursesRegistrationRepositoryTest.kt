package com.example

import com.example.data.AdminEntity
import com.example.data.AdminRole
import com.example.data.AppReleaseEntity
import com.example.data.ApprovalStatus
import com.example.data.AvailabilityStatus
import com.example.data.BangladeshData
import com.example.data.DirectoryConfigEntity
import com.example.data.EducationEntry
import com.example.data.LiveNoticeEntity
import com.example.data.NurseCategory
import com.example.data.NurseEntity
import com.example.data.NursesRepository
import com.example.data.SecurityUtils
import com.example.data.TransferRequestEntity
import com.example.data.TransferStatus
import com.example.ui.PrintColumn
import com.example.ui.PrintReportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NursesRegistrationRepositoryTest {

    @Test
    fun nurseEntity_safeEnumFallbacksHandleSchemaDriftWithoutCrashing() {
        val unknownNurse = NurseEntity(
            fullName = "Tasnim Tabassum",
            bnmcNumber = "BNMC-99001",
            nurseCategory = "LEGACY_TYPE",
            approvalStatus = "UNKNOWN_STATUS",
            availabilityStatus = "UNKNOWN_SHIFT"
        )
        assertEquals(ApprovalStatus.PENDING, unknownNurse.approvalEnum)
        assertEquals(AvailabilityStatus.ON_DUTY, unknownNurse.availabilityEnum)
        assertEquals(NurseCategory.GOVT, unknownNurse.categoryEnum)
    }

    @Test
    fun nurseCategory_and_multiDegreeEducationSummary_workCorrectly() {
        val studentNurse = NurseEntity(
            fullName = "Ruma Akter",
            nurseCategory = NurseCategory.STUDENT.name,
            bnmcNumber = "STU-2025-11",
            hospitalName = "Dhaka Nursing College",
            educations = listOf(
                EducationEntry(
                    courseName = "BSc in Nursing (Basic)",
                    major = "Midwifery",
                    university = "DU",
                    passedYear = "2024"
                ),
                EducationEntry(
                    courseName = "MSN",
                    major = "Critical Care",
                    university = "BSMMU",
                    passedYear = "2026"
                )
            )
        )
        assertEquals("Student Reg No.", studentNurse.registrationNumberLabel)
        assertEquals("Educational Institute", studentNurse.workplaceLabel)
        assertTrue(studentNurse.educationSummaryText().contains("BSc in Nursing (Basic) (Midwifery) — DU, 2024"))
        assertTrue(studentNurse.educationSummaryText().contains("MSN (Critical Care) — BSMMU, 2026"))

        val legacyNurse = NurseEntity(
            fullName = "Legacy Nurse",
            qualification = "Diploma in Nursing Science & Midwifery"
        )
        assertEquals(1, legacyNurse.effectiveEducations().size)
        assertEquals("Diploma in Nursing Science & Midwifery", legacyNurse.educationSummaryText())
    }

    @Test
    fun adminEntity_divisionalScopeAndRoleFallbackWorkCorrectly() {
        val unknownAdmin = AdminEntity(
            fullName = "Test Admin",
            email = "test@gmail.com",
            role = "LEGACY_ROLE",
            assignedDivision = "All Bangladesh"
        )
        assertEquals(AdminRole.MODERATOR, unknownAdmin.roleEnum)
        assertFalse(unknownAdmin.isDivisionRestricted)
        assertNull(unknownAdmin.effectiveDivisionScope)
        assertEquals("mdnasirnaznin@gmail.com", NursesRepository.BOOTSTRAP_ADMIN_EMAIL)

        val divisionalAdmin = AdminEntity(
            fullName = "Dhaka Admin",
            email = "dhaka.admin@gmail.com",
            role = AdminRole.MODERATOR.name,
            assignedDivision = "Dhaka"
        )
        assertTrue(divisionalAdmin.isDivisionRestricted)
        assertEquals("Dhaka", divisionalAdmin.effectiveDivisionScope)

        val superAdminWithDiv = AdminEntity(
            fullName = "Super Admin",
            email = "super@gmail.com",
            role = AdminRole.SUPER_ADMIN.name,
            assignedDivision = "Dhaka"
        )
        assertFalse(superAdminWithDiv.isDivisionRestricted)
        assertNull(superAdminWithDiv.effectiveDivisionScope)
    }

    @Test
    fun transferRequestAndLiveNotice_safeDefaultsAndEnumFallback() {
        val unknownTransfer = TransferRequestEntity(
            nurseId = "nurse_1",
            nurseName = "Nusrat Jahan",
            currentHospitalName = "DMCH",
            newHospitalName = "CMCH",
            currentCategory = NurseCategory.NON_GOVT.name,
            newCategory = NurseCategory.GOVT.name,
            status = "UNEXPECTED_STATUS"
        )
        assertEquals(TransferStatus.PENDING, unknownTransfer.statusEnum)
        assertEquals(NurseCategory.NON_GOVT, unknownTransfer.currentCategoryEnum)
        assertEquals(NurseCategory.GOVT, unknownTransfer.newCategoryEnum)

        val approvedTransfer = unknownTransfer.copy(status = TransferStatus.APPROVED.name)
        assertEquals(TransferStatus.APPROVED, approvedTransfer.statusEnum)

        val defaultNotice = LiveNoticeEntity()
        assertFalse(defaultNotice.active)
        assertEquals("", defaultNotice.title)
    }

    @Test
    fun bangladeshData_includesMidwifeStudentCoursesAndPrintEscapesHtml() {
        assertTrue("Midwife" in BangladeshData.designations)
        assertTrue("Student Nurse" in BangladeshData.designations)
        assertTrue("PhD" in BangladeshData.courseNames)
        assertTrue("MSN" in BangladeshData.courseNames)
        assertTrue("BSc in Nursing (Basic)" in BangladeshData.courseNames)
        assertTrue("BSc in Nursing (Post Basic)" in BangladeshData.courseNames)
        assertTrue("BSc in Public Health Nursing (Post Basic)" in BangladeshData.courseNames)
        assertTrue("BSc in Midwifery" in BangladeshData.courseNames)
        assertTrue("Diploma in Nursing Science & Midwifery" in BangladeshData.courseNames)
        assertTrue("Diploma in Nursing" in BangladeshData.courseNames)
        assertTrue("Diploma in Midwifery" in BangladeshData.courseNames)

        val nurse = NurseEntity(
            fullName = "Nusrat <script>alert(1)</script>",
            fatherName = "Karim Mia",
            motherName = "Amena Begum",
            bnmcNumber = "BNMC-101",
            govtUid = "UID-8899",
            approvalStatus = ApprovalStatus.APPROVED.name,
            phone = "01711223344",
            division = "Dhaka",
            district = "Dhaka"
        )
        val html = PrintReportHelper.buildReportHtml(
            nurses = listOf(nurse),
            selectedColumns = listOf(PrintColumn.NAME, PrintColumn.FATHER_NAME, PrintColumn.REG_NO, PrintColumn.GOVT_UID),
            reportTitle = "Test Report",
            scopeSubtitle = "Dhaka Division",
            generatedBy = "Admin"
        )
        assertFalse(html.contains("<script>alert(1)</script>"))
        assertTrue(html.contains("Verified"))
        assertTrue(html.contains("Karim Mia"))
        assertTrue(html.contains("UID-8899"))
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
        val allDistricts = BangladeshData.districtsByDivision.values.flatten()
        assertTrue(allDistricts.all { BangladeshData.upazilasForDistrict(it).isNotEmpty() })
        assertTrue("Shahbagh" in BangladeshData.upazilasForDistrict("Dhaka"))
    }

    @Test
    fun directoryConfig_mergesDeduplicatesAndFiltersBlankLiveOptions() {
        val config = DirectoryConfigEntity(
            extraDesignations = listOf("  OT Nurse  ", "", "Midwife", "ICU Charge Nurse", "OT Nurse"),
            extraCourseNames = listOf("  BSc in Critical Care ", "   ", "MSN", "Diploma in Cardiac Nursing"),
            extraSpecialties = listOf("Neonatal ICU", "Emergency & Trauma", "  ")
        )

        val designations = config.mergedDesignations()
        assertTrue("OT Nurse" in designations)
        assertTrue("ICU Charge Nurse" in designations)
        assertEquals(1, designations.count { it == "Midwife" })
        assertEquals(1, designations.count { it == "OT Nurse" })
        assertFalse(designations.any { it.isBlank() })

        val courses = config.mergedCourseNames()
        assertTrue("BSc in Critical Care" in courses)
        assertTrue("Diploma in Cardiac Nursing" in courses)
        assertEquals(1, courses.count { it == "MSN" })
        assertFalse(courses.any { it.isBlank() })

        val specialties = config.mergedSpecialties()
        assertTrue("Neonatal ICU" in specialties)
        assertEquals(1, specialties.count { it == "Emergency & Trauma" })
        assertFalse(specialties.any { it.isBlank() })
    }

    @Test
    fun appRelease_detectsNewerUpdateOnlyWhenActiveHigherVersionAndUrlPresent() {
        val inactiveRelease = AppReleaseEntity(
            latestVersionCode = 10,
            latestVersionName = "2.0",
            apkDownloadUrl = "https://github.com/example/releases/latest",
            active = false
        )
        assertFalse(inactiveRelease.hasNewerUpdate(currentVersionCode = 4))

        val olderOrEqualRelease = AppReleaseEntity(
            latestVersionCode = 4,
            latestVersionName = "1.3",
            apkDownloadUrl = "https://github.com/example/releases/latest",
            active = true
        )
        assertFalse(olderOrEqualRelease.hasNewerUpdate(currentVersionCode = 4))

        val missingUrlRelease = AppReleaseEntity(
            latestVersionCode = 5,
            latestVersionName = "1.4",
            apkDownloadUrl = "",
            webPortalUrl = "   ",
            active = true
        )
        assertFalse(missingUrlRelease.hasNewerUpdate(currentVersionCode = 4))

        val validApkRelease = AppReleaseEntity(
            latestVersionCode = 5,
            latestVersionName = "1.4",
            apkDownloadUrl = "https://github.com/example/releases/latest",
            active = true
        )
        assertTrue(validApkRelease.hasNewerUpdate(currentVersionCode = 4))

        val validWebOnlyRelease = AppReleaseEntity(
            latestVersionCode = 5,
            latestVersionName = "1.4",
            webPortalUrl = "https://nursesbd.example.com",
            active = true
        )
        assertTrue(validWebOnlyRelease.hasNewerUpdate(currentVersionCode = 4))
    }
}
