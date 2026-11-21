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

@Keep
@IgnoreExtraProperties
data class NurseEntity(
    @DocumentId val id: String = "",
    val submittedByUid: String = "",
    val fullName: String = "",
    val bnmcNumber: String = "",
    val designation: String = "",
    val qualification: String = "",
    val specialty: String = "",
    val division: String = "",
    val district: String = "",
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
}
