package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.data.AppReleaseEntity
import com.example.data.BangladeshData
import com.example.data.DirectoryConfigEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageLiveDirectoryConfigDialog(
    currentConfig: DirectoryConfigEntity,
    onDismiss: () -> Unit,
    onSave: (
        extraDesignations: List<String>,
        extraCourseNames: List<String>,
        extraSpecialties: List<String>,
        onError: (String) -> Unit
    ) -> Unit
) {
    var designations by remember(currentConfig) { mutableStateOf(currentConfig.extraDesignations) }
    var courseNames by remember(currentConfig) { mutableStateOf(currentConfig.extraCourseNames) }
    var specialties by remember(currentConfig) { mutableStateOf(currentConfig.extraSpecialties) }

    var newDesignation by remember { mutableStateOf("") }
    var newCourse by remember { mutableStateOf("") }
    var newSpecialty by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        icon = { Icon(Icons.Filled.CloudSync, contentDescription = null) },
        title = { Text("Manage Form Options") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "নতুন পদবি, কোর্স বা স্পেশালিটি যুক্ত করলে তাৎক্ষণিকভাবে সকল ব্যবহারকারীর ফর্ম ও ফিল্টারে প্রদর্শিত হবে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                DynamicOptionEditorBlock(
                    title = "Additional Designations",
                    placeholder = "e.g., Nursing Superintendent",
                    inputValue = newDesignation,
                    onInputChange = { newDesignation = it.take(60); errorText = null },
                    items = designations,
                    onAdd = {
                        val cleaned = newDesignation.trim()
                        if (cleaned.isNotBlank() && cleaned !in designations && cleaned !in BangladeshData.designations) {
                            designations = (designations + cleaned).take(40)
                        }
                        newDesignation = ""
                    },
                    onRemove = { item -> designations = designations - item },
                    tagPrefix = "config_designation"
                )

                DynamicOptionEditorBlock(
                    title = "Additional Course Names",
                    placeholder = "e.g., Post Basic ICU Nursing",
                    inputValue = newCourse,
                    onInputChange = { newCourse = it.take(80); errorText = null },
                    items = courseNames,
                    onAdd = {
                        val cleaned = newCourse.trim()
                        if (cleaned.isNotBlank() && cleaned !in courseNames && cleaned !in BangladeshData.courseNames) {
                            courseNames = (courseNames + cleaned).take(40)
                        }
                        newCourse = ""
                    },
                    onRemove = { item -> courseNames = courseNames - item },
                    tagPrefix = "config_course"
                )

                DynamicOptionEditorBlock(
                    title = "Additional Clinical Specialties",
                    placeholder = "e.g., Burn & Plastic Surgery",
                    inputValue = newSpecialty,
                    onInputChange = { newSpecialty = it.take(60); errorText = null },
                    items = specialties,
                    onAdd = {
                        val cleaned = newSpecialty.trim()
                        if (cleaned.isNotBlank() && cleaned !in specialties && cleaned !in BangladeshData.specialties) {
                            specialties = (specialties + cleaned).take(40)
                        }
                        newSpecialty = ""
                    },
                    onRemove = { item -> specialties = specialties - item },
                    tagPrefix = "config_specialty"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSaving = true
                    onSave(designations, courseNames, specialties) { msg ->
                        isSaving = false
                        errorText = msg
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.testTag("save_directory_config_button")
            ) {
                Text(if (isSaving) "Saving..." else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DynamicOptionEditorBlock(
    title: String,
    placeholder: String,
    inputValue: String,
    onInputChange: (String) -> Unit,
    items: List<String>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    tagPrefix: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = onInputChange,
                placeholder = { Text(placeholder) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("${tagPrefix}_input")
            )
            FilledTonalButton(
                onClick = onAdd,
                enabled = inputValue.trim().isNotEmpty(),
                modifier = Modifier.testTag("${tagPrefix}_add_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
            }
        }
        if (items.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEach { item ->
                    InputChip(
                        selected = true,
                        onClick = { onRemove(item) },
                        label = { Text(item) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove $item",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PublishAppReleaseDialog(
    currentRelease: AppReleaseEntity?,
    onDismiss: () -> Unit,
    onPublish: (
        latestVersionCode: Int,
        latestVersionName: String,
        releaseNotes: String,
        apkDownloadUrl: String,
        webPortalUrl: String,
        active: Boolean,
        onError: (String) -> Unit
    ) -> Unit
) {
    var versionCodeText by remember(currentRelease) {
        mutableStateOf((currentRelease?.latestVersionCode?.takeIf { it > 0 } ?: BuildConfig.VERSION_CODE).toString())
    }
    var versionName by remember(currentRelease) {
        mutableStateOf(currentRelease?.latestVersionName?.ifBlank { BuildConfig.VERSION_NAME } ?: BuildConfig.VERSION_NAME)
    }
    var releaseNotes by remember(currentRelease) {
        mutableStateOf(currentRelease?.releaseNotes ?: "")
    }
    var apkDownloadUrl by remember(currentRelease) {
        mutableStateOf(currentRelease?.apkDownloadUrl ?: "")
    }
    var webPortalUrl by remember(currentRelease) {
        mutableStateOf(currentRelease?.webPortalUrl ?: "")
    }
    var active by remember(currentRelease) {
        mutableStateOf(currentRelease?.active ?: false)
    }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        icon = { Icon(Icons.Filled.SystemUpdate, contentDescription = null) },
        title = { Text("Publish App Version") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "নতুন সংস্করণ যুক্ত করলে পুরোনো ভার্সন ব্যবহারকারীরা ডিরেক্টরি স্ক্রিনে আপডেটের নির্দেশনা দেখতে পাবেন (বর্তমান: v${BuildConfig.VERSION_NAME})।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Show Update Notice",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it },
                        modifier = Modifier.testTag("app_release_active_switch")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = versionCodeText,
                        onValueChange = { versionCodeText = it.filter { c -> c.isDigit() }.take(5); errorText = null },
                        label = { Text("Version Code *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("release_version_code")
                    )
                    OutlinedTextField(
                        value = versionName,
                        onValueChange = { versionName = it.take(20); errorText = null },
                        label = { Text("Version Name *") },
                        placeholder = { Text("e.g., 1.4") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("release_version_name")
                    )
                }

                OutlinedTextField(
                    value = apkDownloadUrl,
                    onValueChange = { apkDownloadUrl = it.take(300); errorText = null },
                    label = { Text("Download / Store URL (https://...)") },
                    placeholder = { Text("https://play.google.com/store/apps/...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("release_apk_url")
                )

                OutlinedTextField(
                    value = webPortalUrl,
                    onValueChange = { webPortalUrl = it.take(300); errorText = null },
                    label = { Text("Web Portal URL (Optional, https://...)") },
                    placeholder = { Text("https://nursesbd.org") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("release_web_url")
                )

                OutlinedTextField(
                    value = releaseNotes,
                    onValueChange = { releaseNotes = it.take(400); errorText = null },
                    label = { Text("What's New / Release Notes") },
                    placeholder = { Text("নতুন ফিচার ও আপডেটের বিবরণ লিখুন...") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("release_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedCode = versionCodeText.toIntOrNull()
                    if (parsedCode == null || parsedCode <= 0) {
                        errorText = "Enter a valid positive integer Version Code."
                        return@Button
                    }
                    if (versionName.isBlank()) {
                        errorText = "Enter Version Name (e.g., 1.3)."
                        return@Button
                    }
                    val cleanApk = apkDownloadUrl.trim()
                    val cleanWeb = webPortalUrl.trim()
                    if (cleanApk.isNotEmpty() && !cleanApk.startsWith("https://")) {
                        errorText = "APK URL must start with https://"
                        return@Button
                    }
                    if (cleanWeb.isNotEmpty() && !cleanWeb.startsWith("https://")) {
                        errorText = "Web Portal URL must start with https://"
                        return@Button
                    }
                    if (active && cleanApk.isEmpty() && cleanWeb.isEmpty()) {
                        errorText = "Provide at least an APK URL or Web Portal URL."
                        return@Button
                    }
                    isSaving = true
                    onPublish(
                        parsedCode,
                        versionName,
                        releaseNotes,
                        cleanApk,
                        cleanWeb,
                        active
                    ) { msg ->
                        isSaving = false
                        errorText = msg
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.testTag("publish_app_release_button")
            ) {
                Text(if (isSaving) "Publishing..." else "Save Release Info")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel")
            }
        }
    )
}
