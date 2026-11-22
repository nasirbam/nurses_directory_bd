package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class DirectoryConfigEntity(
    @DocumentId val id: String = "",
    val extraDesignations: List<String> = emptyList(),
    val extraCourseNames: List<String> = emptyList(),
    val extraSpecialties: List<String> = emptyList(),
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun mergedDesignations(): List<String> =
        (BangladeshData.designations + extraDesignations)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    fun mergedCourseNames(): List<String> =
        (BangladeshData.courseNames + extraCourseNames)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    fun mergedSpecialties(): List<String> =
        (BangladeshData.specialties + extraSpecialties)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
}

@Keep
@IgnoreExtraProperties
data class AppReleaseEntity(
    @DocumentId val id: String = "",
    val latestVersionCode: Int = 0,
    val latestVersionName: String = "",
    val releaseNotes: String = "",
    val apkDownloadUrl: String = "",
    val webPortalUrl: String = "",
    val active: Boolean = false,
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun hasNewerUpdate(currentVersionCode: Int): Boolean {
        return active &&
            latestVersionCode > currentVersionCode &&
            (apkDownloadUrl.isNotBlank() || webPortalUrl.isNotBlank())
    }
}
