package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.AvailabilityStatus
import com.example.data.BangladeshData
import com.example.data.EducationEntry
import com.example.data.LiveNoticeEntity
import com.example.data.NurseCategory
import com.example.data.NurseEntity
import com.example.data.SecurityUtils
import com.example.ui.screens.FormDropdownField

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditNurseDialog(
    nurse: NurseEntity,
    designationOptions: List<String> = BangladeshData.designations,
    courseNameOptions: List<String> = BangladeshData.courseNames,
    specialtyOptions: List<String> = BangladeshData.specialties,
    onDismiss: () -> Unit,
    onSave: (updatedNurse: NurseEntity, onError: (String) -> Unit) -> Unit
) {
    var selectedCategory by remember(nurse.id) { mutableStateOf(nurse.categoryEnum) }
    var fullName by remember(nurse.id) { mutableStateOf(nurse.fullName) }
    var fatherName by remember(nurse.id) { mutableStateOf(nurse.fatherName) }
    var motherName by remember(nurse.id) { mutableStateOf(nurse.motherName) }
    var bnmcNumber by remember(nurse.id) { mutableStateOf(nurse.bnmcNumber) }
    var govtUid by remember(nurse.id) { mutableStateOf(nurse.govtUid) }
    var phone by remember(nurse.id) { mutableStateOf(nurse.phone) }
    var whatsapp by remember(nurse.id) { mutableStateOf(nurse.whatsapp.ifBlank { nurse.phone }) }
    var email by remember(nurse.id) { mutableStateOf(nurse.email) }
    var selectedDivision by remember(nurse.id) {
        mutableStateOf(
            if (nurse.division in BangladeshData.divisions) nurse.division
            else BangladeshData.divisions.first()
        )
    }
    var selectedDistrict by remember(nurse.id) {
        mutableStateOf(
            nurse.district.ifBlank {
                BangladeshData.districtsByDivision[selectedDivision]?.firstOrNull() ?: "Dhaka"
            }
        )
    }
    var selectedUpazila by remember(nurse.id) {
        mutableStateOf(
            nurse.upazila.ifBlank {
                BangladeshData.upazilasForDistrict(selectedDistrict).firstOrNull() ?: "$selectedDistrict Sadar"
            }
        )
    }
    var address by remember(nurse.id) { mutableStateOf(nurse.address) }
    var hospitalName by remember(nurse.id) { mutableStateOf(nurse.hospitalName) }
    var designation by remember(nurse.id) {
        mutableStateOf(nurse.designation.ifBlank { BangladeshData.designations.first() })
    }
    var educations by remember(nurse.id) {
        mutableStateOf(
            nurse.effectiveEducations().ifEmpty {
                listOf(EducationEntry(courseName = BangladeshData.courseNames.first()))
            }
        )
    }
    var specialty by remember(nurse.id) {
        mutableStateOf(nurse.specialty.ifBlank { BangladeshData.specialties.first() })
    }
    var bloodGroup by remember(nurse.id) {
        mutableStateOf(
            if (nurse.bloodGroup in BangladeshData.bloodGroups) nurse.bloodGroup else "O+"
        )
    }
    var availability by remember(nurse.id) { mutableStateOf(nurse.availabilityEnum) }
    var experienceYearsText by remember(nurse.id) { mutableStateOf(nurse.experienceYears.toString()) }
    var bio by remember(nurse.id) { mutableStateOf(nurse.bio) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val availableDistricts = remember(selectedDivision) {
        BangladeshData.districtsByDivision[selectedDivision] ?: listOf(selectedDivision)
    }
    val availableUpazilas = remember(selectedDistrict, nurse.upazila) {
        val base = BangladeshData.upazilasForDistrict(selectedDistrict)
        if (selectedUpazila.isNotBlank() && selectedUpazila !in base && selectedDistrict == nurse.district) {
            (listOf(selectedUpazila) + base).distinct()
        } else {
            base
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
        title = { Text("Edit Nurse Profile") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorText != null) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorText!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Text(
                    text = "Service Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NurseCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat; errorText = null },
                            label = { Text(cat.shortLabel) }
                        )
                    }
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errorText = null },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_nurse_name")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it.take(80) },
                        label = { Text("Father's Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = motherName,
                        onValueChange = { motherName = it.take(80) },
                        label = { Text("Mother's Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = bnmcNumber,
                    onValueChange = { bnmcNumber = it; errorText = null },
                    label = {
                        Text(
                            if (selectedCategory == NurseCategory.STUDENT) "Student Registration No. *"
                            else "BNMC Registration No. *"
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_nurse_bnmc")
                )

                if (selectedCategory != NurseCategory.STUDENT) {
                    OutlinedTextField(
                        value = govtUid,
                        onValueChange = { govtUid = it.take(40) },
                        label = { Text("Govt. UID (Optional for Job Holders)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; errorText = null },
                        label = { Text("Mobile *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("edit_nurse_phone")
                    )
                    OutlinedTextField(
                        value = experienceYearsText,
                        onValueChange = { experienceYearsText = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Exp Yrs") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it; errorText = null },
                    label = { Text("WhatsApp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_nurse_whatsapp")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorText = null },
                    label = { Text("Email (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_nurse_email")
                )

                FormDropdownField(
                    label = "Division *",
                    selected = selectedDivision,
                    options = BangladeshData.divisions,
                    onSelect = { div ->
                        selectedDivision = div
                        val nextDist = BangladeshData.districtsByDivision[div]?.firstOrNull() ?: div
                        selectedDistrict = nextDist
                        selectedUpazila = BangladeshData.upazilasForDistrict(nextDist).firstOrNull() ?: "$nextDist Sadar"
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                FormDropdownField(
                    label = "District (Jela) *",
                    selected = selectedDistrict,
                    options = availableDistricts,
                    onSelect = { dist ->
                        selectedDistrict = dist
                        selectedUpazila = BangladeshData.upazilasForDistrict(dist).firstOrNull() ?: "$dist Sadar"
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                FormDropdownField(
                    label = "Upazila / Thana *",
                    selected = selectedUpazila,
                    options = availableUpazilas,
                    onSelect = { selectedUpazila = it },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it.take(200); errorText = null },
                    label = {
                        Text(
                            if (selectedCategory == NurseCategory.NON_GOVT) "Present Address (ঠিকানা) *"
                            else "Present Address (Optional)"
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it; errorText = null },
                    label = {
                        Text(
                            when (selectedCategory) {
                                NurseCategory.GOVT -> "Govt. Hospital / Workplace *"
                                NurseCategory.NON_GOVT -> "Working Place / Clinic (Optional)"
                                NurseCategory.STUDENT -> "Educational Institute *"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_nurse_hospital")
                )

                FormDropdownField(
                    label = "Designation",
                    selected = designation,
                    options = (designationOptions + designation).distinct(),
                    onSelect = { designation = it },
                    modifier = Modifier.fillMaxWidth()
                )

                EducationInputSection(
                    educations = educations,
                    onEducationsChange = { educations = it },
                    courseOptionsList = courseNameOptions
                )

                FormDropdownField(
                    label = "Clinical Specialty",
                    selected = specialty,
                    options = (specialtyOptions + specialty).distinct(),
                    onSelect = { specialty = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormDropdownField(
                        label = "Blood",
                        selected = bloodGroup,
                        options = BangladeshData.bloodGroups,
                        onSelect = { bloodGroup = it },
                        modifier = Modifier.weight(0.9f)
                    )
                    FormDropdownField(
                        label = "Shift Status",
                        selected = availability.label,
                        options = AvailabilityStatus.entries.map { it.label },
                        onSelect = { label ->
                            availability = AvailabilityStatus.entries.first { it.label == label }
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it.take(400) },
                    label = { Text("Profile Bio / Note") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = fullName.trim()
                    val cleanBnmc = bnmcNumber.trim()
                    val cleanPhone = phone.replace(Regex("[\\s\\-]"), "")
                    val cleanWhatsapp = whatsapp.replace(Regex("[\\s\\-]"), "").ifBlank { cleanPhone }
                    val cleanEmail = email.trim()
                    val cleanHospital = hospitalName.trim()
                    val cleanAddress = address.trim()
                    val validEducations = educations.filter { it.courseName.isNotBlank() }

                    when {
                        cleanName.length !in 3..80 -> errorText = "Name must be 3–80 characters."
                        cleanBnmc.length !in 4..30 -> errorText = "Enter valid registration number."
                        !SecurityUtils.isValidBangladeshPhone(cleanPhone) -> errorText = "Enter valid BD mobile (01XXXXXXXXX)."
                        !SecurityUtils.isValidBangladeshPhone(cleanWhatsapp) -> errorText = "Enter valid BD WhatsApp number."
                        cleanEmail.isNotEmpty() && !SecurityUtils.isValidEmail(cleanEmail) -> errorText = "Enter valid email."
                        selectedCategory == NurseCategory.GOVT && cleanHospital.length !in 3..120 ->
                            errorText = "Govt. hospital name must be 3–120 characters."
                        selectedCategory == NurseCategory.STUDENT && cleanHospital.length !in 3..120 ->
                            errorText = "Educational institute name must be 3–120 characters."
                        selectedCategory == NurseCategory.NON_GOVT && cleanAddress.length < 3 && cleanHospital.length < 3 ->
                            errorText = "Present address (thikana) is required for Non-Govt. nurses."
                        validEducations.isEmpty() ->
                            errorText = "Select at least one educational course."
                        else -> {
                            isSaving = true
                            val draft = nurse.copy(
                                nurseCategory = selectedCategory.name,
                                fullName = cleanName,
                                fatherName = fatherName.trim(),
                                motherName = motherName.trim(),
                                bnmcNumber = cleanBnmc,
                                govtUid = govtUid.trim(),
                                phone = cleanPhone,
                                whatsapp = cleanWhatsapp,
                                email = cleanEmail,
                                division = selectedDivision,
                                district = selectedDistrict,
                                upazila = selectedUpazila,
                                address = cleanAddress,
                                hospitalName = cleanHospital,
                                designation = designation,
                                educations = validEducations,
                                specialty = specialty,
                                bloodGroup = bloodGroup,
                                availabilityStatus = availability.name,
                                experienceYears = (experienceYearsText.toIntOrNull() ?: nurse.experienceYears).coerceIn(0, 60),
                                bio = bio.trim()
                            )
                            val updated = draft.copy(
                                qualification = draft.educationSummaryText()
                                    .ifBlank { validEducations.first().courseName }
                                    .take(200)
                            )
                            onSave(updated) { msg ->
                                isSaving = false
                                errorText = msg
                            }
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.testTag("confirm_edit_nurse")
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
fun RequestTransferDialog(
    nurse: NurseEntity,
    designationOptions: List<String> = BangladeshData.designations,
    onDismiss: () -> Unit,
    onSubmit: (
        newCategory: NurseCategory,
        newGovtUid: String,
        newDesignation: String,
        newDivision: String,
        newDistrict: String,
        newUpazila: String,
        newHospitalName: String,
        transferNote: String,
        onError: (String) -> Unit
    ) -> Unit
) {
    var targetCategory by remember(nurse.id) {
        mutableStateOf(
            if (nurse.categoryEnum == NurseCategory.NON_GOVT) NurseCategory.GOVT
            else nurse.categoryEnum
        )
    }
    var newGovtUid by remember(nurse.id) { mutableStateOf(nurse.govtUid) }
    var newDesignation by remember(nurse.id) {
        mutableStateOf(
            if (nurse.categoryEnum == NurseCategory.NON_GOVT) "Senior Staff Nurse"
            else nurse.designation.ifBlank { BangladeshData.designations.first() }
        )
    }
    var selectedDivision by remember(nurse.id) {
        mutableStateOf(
            if (nurse.division in BangladeshData.divisions) nurse.division
            else BangladeshData.divisions.first()
        )
    }
    var selectedDistrict by remember(nurse.id) {
        mutableStateOf(
            nurse.district.ifBlank {
                BangladeshData.districtsByDivision[selectedDivision]?.firstOrNull() ?: "Dhaka"
            }
        )
    }
    var selectedUpazila by remember(nurse.id) {
        mutableStateOf(
            nurse.upazila.ifBlank {
                BangladeshData.upazilasForDistrict(selectedDistrict).firstOrNull() ?: "$selectedDistrict Sadar"
            }
        )
    }
    var newHospitalName by remember(nurse.id) { mutableStateOf("") }
    var transferNote by remember(nurse.id) { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val availableDistricts = remember(selectedDivision) {
        BangladeshData.districtsByDivision[selectedDivision] ?: listOf(selectedDivision)
    }
    val availableUpazilas = remember(selectedDistrict) {
        BangladeshData.upazilasForDistrict(selectedDistrict)
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        icon = { Icon(Icons.Filled.SwapHoriz, contentDescription = null) },
        title = {
            Text(
                if (nurse.categoryEnum == NurseCategory.NON_GOVT) {
                    "Govt. Joining / Workplace Transfer"
                } else {
                    "Request Hospital Transfer"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Current Profile (${nurse.fullName} • ${nurse.categoryEnum.shortLabel})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = nurse.hospitalName.ifBlank { "Workplace not set • ${nurse.address}" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = listOfNotNull(
                                nurse.upazila.takeIf { it.isNotBlank() },
                                nurse.district.takeIf { it.isNotBlank() },
                                nurse.division.takeIf { it.isNotBlank() }
                            ).joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (errorText != null) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorText!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Text(
                    text = "Target Category (নতুন ক্যাটাগরি)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(NurseCategory.GOVT, NurseCategory.NON_GOVT).forEach { cat ->
                        FilterChip(
                            selected = targetCategory == cat,
                            onClick = { targetCategory = cat; errorText = null },
                            label = {
                                Text(
                                    if (cat == NurseCategory.GOVT) "Govt. Service (সরকারি)"
                                    else "Non-Govt. (বেসরকারি)"
                                )
                            }
                        )
                    }
                }

                if (targetCategory == NurseCategory.GOVT) {
                    OutlinedTextField(
                        value = newGovtUid,
                        onValueChange = { newGovtUid = it.take(40) },
                        label = { Text("Govt. UID (If Assigned)") },
                        placeholder = { Text("e.g., GOVT-UID-10492") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_govt_uid")
                    )
                }

                FormDropdownField(
                    label = "New Designation",
                    selected = newDesignation,
                    options = (designationOptions + newDesignation).distinct(),
                    onSelect = { newDesignation = it },
                    modifier = Modifier.fillMaxWidth()
                )

                FormDropdownField(
                    label = "New Division *",
                    selected = selectedDivision,
                    options = BangladeshData.divisions,
                    onSelect = { div ->
                        selectedDivision = div
                        val nextDist = BangladeshData.districtsByDivision[div]?.firstOrNull() ?: div
                        selectedDistrict = nextDist
                        selectedUpazila = BangladeshData.upazilasForDistrict(nextDist).firstOrNull() ?: "$nextDist Sadar"
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                FormDropdownField(
                    label = "New District (Jela) *",
                    selected = selectedDistrict,
                    options = availableDistricts,
                    onSelect = { dist ->
                        selectedDistrict = dist
                        selectedUpazila = BangladeshData.upazilasForDistrict(dist).firstOrNull() ?: "$dist Sadar"
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                FormDropdownField(
                    label = "New Upazila / Thana *",
                    selected = selectedUpazila,
                    options = availableUpazilas,
                    onSelect = { selectedUpazila = it },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newHospitalName,
                    onValueChange = { newHospitalName = it; errorText = null },
                    label = {
                        Text(
                            if (targetCategory == NurseCategory.GOVT) {
                                "New Govt. Working Place / Hospital *"
                            } else {
                                "New Working Place / Hospital Name *"
                            }
                        )
                    },
                    placeholder = { Text("e.g., Chittagong Medical College Hospital") },
                    leadingIcon = { Icon(Icons.Filled.LocalHospital, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_new_hospital")
                )

                OutlinedTextField(
                    value = transferNote,
                    onValueChange = { transferNote = it.take(250) },
                    label = { Text("Transfer / Joining Order Note (Optional)") },
                    placeholder = { Text("e.g., Joined Govt. service / DGHS memo #...") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_note")
                )

                Text(
                    text = "অ্যাডমিন অনুমোদন করলে আপনার ক্যাটাগরি ও নতুন কর্মস্থল স্বয়ংক্রিয়ভাবে আপডেট হয়ে যাবে।",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanHospital = newHospitalName.trim()
                    if (cleanHospital.length !in 3..120) {
                        errorText = if (targetCategory == NurseCategory.GOVT) {
                            "Govt. working place / hospital name is mandatory (3–120 characters)."
                        } else {
                            "Enter new workplace / hospital name (3–120 characters)."
                        }
                        return@Button
                    }
                    isSubmitting = true
                    onSubmit(
                        targetCategory,
                        newGovtUid.trim(),
                        newDesignation,
                        selectedDivision,
                        selectedDistrict,
                        selectedUpazila,
                        cleanHospital,
                        transferNote.trim()
                    ) { msg ->
                        isSubmitting = false
                        errorText = msg
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier.testTag("submit_transfer_request_button")
            ) {
                Text(if (isSubmitting) "Submitting..." else "Submit Transfer Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PublishLiveNoticeDialog(
    currentNotice: LiveNoticeEntity?,
    onDismiss: () -> Unit,
    onPublish: (title: String, message: String, active: Boolean, onError: (String) -> Unit) -> Unit
) {
    var title by remember(currentNotice) { mutableStateOf(currentNotice?.title ?: "") }
    var message by remember(currentNotice) { mutableStateOf(currentNotice?.message ?: "") }
    var active by remember(currentNotice) { mutableStateOf(currentNotice?.active ?: true) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        icon = { Icon(Icons.Filled.Campaign, contentDescription = null) },
        title = { Text("Live Broadcast Update") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "এখান থেকে কোনো নোটিশ বা তথ্য আপডেট করলে অ্যাপ ডাউনলোড করা ছাড়াই সবার ফোনে তাৎক্ষণিকভাবে দেখা যাবে।",
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
                        text = "Show Live Banner to All Users",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it },
                        modifier = Modifier.testTag("live_notice_active_switch")
                    )
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(100); errorText = null },
                    label = { Text("Notice Title") },
                    placeholder = { Text("e.g., জরুরি বিজ্ঞপ্তি / ডিরেক্টরি আপডেট") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_notice_title")
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it.take(400); errorText = null },
                    label = { Text("Notice Message") },
                    placeholder = { Text("নতুন তথ্য বা নির্দেশনা লিখুন...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_notice_message")
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    if (active && title.isBlank() && message.isBlank()) {
                        errorText = "Enter a title or message before publishing."
                        return@FilledTonalButton
                    }
                    isSaving = true
                    onPublish(title, message, active) { msg ->
                        isSaving = false
                        errorText = msg
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.testTag("publish_live_notice_button")
            ) {
                Text(if (isSaving) "Publishing..." else "Publish Live")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel")
            }
        }
    )
}
