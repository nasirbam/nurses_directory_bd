package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ApprovalStatus {
    APPROVED,
    PENDING,
    REJECTED
}

enum class AvailabilityStatus(val label: String) {
    ON_DUTY("On Duty"),
    EMERGENCY_AVAILABLE("Emergency Ready"),
    OFF_DUTY("Off Duty")
}

enum class NurseCategory(
    val label: String,
    val shortLabel: String,
    val bnLabel: String
) {
    GOVT("Govt. Nurse / Midwife", "Govt.", "সরকারি নার্স / মিডওয়াইফ"),
    NON_GOVT("Non-Govt. Nurse / Midwife", "Non-Govt.", "বেসরকারি নার্স / মিডওয়াইফ"),
    STUDENT("Student Nurse / Midwife", "Student", "স্টুডেন্ট নার্স / মিডওয়াইফ")
}

@Keep
@IgnoreExtraProperties
data class EducationEntry(
    val courseName: String = "",
    val major: String = "",
    val university: String = "",
    val passedYear: String = ""
) {
    fun formattedSummary(): String {
        val base = courseName.trim()
        if (base.isBlank()) return ""
        val majorPart = major.trim().takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
        val tailParts = listOfNotNull(
            university.trim().takeIf { it.isNotBlank() },
            passedYear.trim().takeIf { it.isNotBlank() }
        ).joinToString(", ")
        return if (tailParts.isNotBlank()) "$base$majorPart — $tailParts" else "$base$majorPart"
    }

    fun toFirestoreMap(): Map<String, String> = mapOf(
        "courseName" to courseName.trim().take(80),
        "major" to major.trim().take(60),
        "university" to university.trim().take(100),
        "passedYear" to passedYear.trim().take(10)
    )
}

@Keep
@IgnoreExtraProperties
data class NurseEntity(
    @DocumentId val id: String = "",
    val submittedByUid: String = "",
    val fullName: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val nurseCategory: String = NurseCategory.GOVT.name,
    val bnmcNumber: String = "",
    val govtUid: String = "",
    val designation: String = "",
    val qualification: String = "",
    val educations: List<EducationEntry> = emptyList(),
    val specialty: String = "",
    val division: String = "",
    val district: String = "",
    val upazila: String = "",
    val address: String = "",
    val hospitalName: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val bloodGroup: String = "O+",
    val availabilityStatus: String = AvailabilityStatus.ON_DUTY.name,
    val experienceYears: Int = 1,
    val bio: String = "",
    val approvalStatus: String = ApprovalStatus.PENDING.name,
    val rejectionReason: String = "",
    val reviewedBy: String? = null,
    @ServerTimestamp val reviewedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val approvalEnum: ApprovalStatus
        get() = ApprovalStatus.entries.firstOrNull { it.name == approvalStatus }
            ?: ApprovalStatus.PENDING

    val availabilityEnum: AvailabilityStatus
        get() = AvailabilityStatus.entries.firstOrNull { it.name == availabilityStatus }
            ?: AvailabilityStatus.ON_DUTY

    val categoryEnum: NurseCategory
        get() = NurseCategory.entries.firstOrNull { it.name == nurseCategory }
            ?: NurseCategory.GOVT

    val registrationNumberLabel: String
        get() = if (categoryEnum == NurseCategory.STUDENT) "Student Reg No." else "BNMC Reg No."

    val workplaceLabel: String
        get() = when (categoryEnum) {
            NurseCategory.STUDENT -> "Educational Institute"
            NurseCategory.NON_GOVT -> "Working Place / Clinic"
            NurseCategory.GOVT -> "Hospital / Workplace"
        }

    fun effectiveEducations(): List<EducationEntry> {
        val cleaned = educations.filter { it.courseName.isNotBlank() }
        if (cleaned.isNotEmpty()) return cleaned
        return if (qualification.isNotBlank()) {
            listOf(EducationEntry(courseName = qualification))
        } else {
            emptyList()
        }
    }

    fun educationSummaryText(): String {
        val list = effectiveEducations()
        if (list.isEmpty()) return qualification
        return list.mapNotNull { it.formattedSummary().takeIf(String::isNotBlank) }
            .joinToString(" | ")
            .ifBlank { qualification }
    }
}
