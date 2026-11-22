package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class TransferStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Keep
@IgnoreExtraProperties
data class TransferRequestEntity(
    @DocumentId val id: String = "",
    val nurseId: String = "",
    val nurseName: String = "",
    val bnmcNumber: String = "",
    val requestedByUid: String = "",
    val currentHospitalName: String = "",
    val currentDivision: String = "",
    val currentDistrict: String = "",
    val currentUpazila: String = "",
    val newHospitalName: String = "",
    val newDivision: String = "",
    val newDistrict: String = "",
    val newUpazila: String = "",
    val transferNote: String = "",
    val status: String = TransferStatus.PENDING.name,
    val rejectionReason: String = "",
    val reviewedBy: String? = null,
    @ServerTimestamp val reviewedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: TransferStatus
        get() = TransferStatus.entries.firstOrNull { it.name == status }
            ?: TransferStatus.PENDING
}

@Keep
@IgnoreExtraProperties
data class LiveNoticeEntity(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val active: Boolean = false,
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
