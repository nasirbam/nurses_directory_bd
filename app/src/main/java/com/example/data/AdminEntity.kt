package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AdminRole(val displayName: String) {
    SUPER_ADMIN("Super Admin"),
    MODERATOR("Division Admin")
}

@Keep
@IgnoreExtraProperties
data class AdminEntity(
    @DocumentId val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = AdminRole.MODERATOR.name,
    val assignedDivision: String = "All Bangladesh",
    val addedBy: String = "System",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val roleEnum: AdminRole
        get() = AdminRole.entries.firstOrNull { it.name == role }
            ?: AdminRole.MODERATOR

    val isDivisionRestricted: Boolean
        get() = roleEnum != AdminRole.SUPER_ADMIN &&
            assignedDivision.isNotBlank() &&
            !assignedDivision.equals("All Bangladesh", ignoreCase = true)

    val effectiveDivisionScope: String?
        get() = if (isDivisionRestricted) assignedDivision.trim() else null
}
