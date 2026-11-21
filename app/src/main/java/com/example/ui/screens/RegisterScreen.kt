package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import com.example.data.ApprovalStatus
import com.example.data.AvailabilityStatus
import com.example.data.BangladeshData
import com.example.data.NurseEntity
import com.example.data.SecurityUtils
import com.example.ui.auth.GoogleSignInButton

private data class RegistrationFieldErrors(
    val fullNameError: String? = null,
    val bnmcError: String? = null,
    val phoneError: String? = null,
    val whatsappError: String? = null,
    val emailError: String? = null,
    val hospitalError: String? = null
) {
    val hasAny: Boolean
        get() = fullNameError != null ||
            bnmcError != null ||
            phoneError != null ||
            whatsappError != null ||
            emailError != null ||
            hospitalError != null
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    isSignedIn: Boolean,
    signedInEmail: String = "",
    onGoogleAuthSuccess: () -> Unit = {},
    onSubmitRegistration: (NurseEntity, (Result<Unit>) -> Unit) -> Unit,
    onNavigateAfterSuccess: () -> Unit,
    successActionLabel: String = "View Directory",
    modifier: Modifier = Modifier
) {
    if (!isSignedIn) {
        var authError by remember { mutableStateOf<String?>(null) }
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.widthIn(max = 460.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HowToReg,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Verify Google Account First",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "লাইভ Cloud Firestore-এ আবেদন জমা দিতে Google Sign-In করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "ফর্ম পূরণ করে জমা দেয়ার পর তা Pending স্ট্যাটাসে সংরক্ষিত হবে এবং অ্যাডমিন অনুমোদনের পর লাইভ ডিরেক্টরিতে দেখাবে।",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (authError != null) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = authError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    GoogleSignInButton(
                        label = "Sign in with Google to Register",
                        onAuthSuccess = {
                            authError = null
                            onGoogleAuthSuccess()
                        },
                        onAuthError = { authError = it },
                        modifier = Modifier.testTag("register_google_signin_button")
                    )
                }
            }
        }
        return
    }

    var fullName by remember { mutableStateOf("") }
    var bnmcNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsappSameAsPhone by remember { mutableStateOf(true) }
    var whatsapp by remember { mutableStateOf("") }
    var email by remember(signedInEmail) { mutableStateOf(signedInEmail) }
    var hospitalName by remember { mutableStateOf("") }
    var selectedDivision by remember { mutableStateOf(BangladeshData.divisions.first()) }
    var selectedDistrict by remember {
        mutableStateOf(
            BangladeshData.districtsByDivision[BangladeshData.divisions.first()]?.firstOrNull() ?: "Dhaka"
        )
    }
    var selectedUpazila by remember {
        mutableStateOf(
            BangladeshData.upazilasForDistrict("Dhaka").firstOrNull() ?: "Shahbagh"
        )
    }
    var selectedDesignation by remember { mutableStateOf(BangladeshData.designations.first()) }
    var selectedQualification by remember { mutableStateOf(BangladeshData.qualifications.first()) }
    var selectedSpecialty by remember { mutableStateOf(BangladeshData.specialties.first()) }
    var selectedBloodGroup by remember { mutableStateOf("O+") }
    var selectedAvailability by remember { mutableStateOf(AvailabilityStatus.ON_DUTY) }
    var experienceYearsText by remember { mutableStateOf("3") }
    var bio by remember { mutableStateOf("") }

    var fieldErrors by remember { mutableStateOf(RegistrationFieldErrors()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val availableDistricts = remember(selectedDivision) {
        BangladeshData.districtsByDivision[selectedDivision] ?: listOf(selectedDivision)
    }
    val availableUpazilas = remember(selectedDistrict) {
        BangladeshData.upazilasForDistrict(selectedDistrict)
    }

    fun validateFields(): RegistrationFieldErrors {
        val cleanName = fullName.trim()
        val cleanBnmc = bnmcNumber.trim()
        val cleanPhone = phone.replace(Regex("[\\s\\-]"), "")
        val cleanWhatsapp = whatsapp.replace(Regex("[\\s\\-]"), "")
        val cleanEmail = email.trim()
        val cleanHospital = hospitalName.trim()

        return RegistrationFieldErrors(
            fullNameError = if (cleanName.length !in 3..80) "Enter full name (3–80 characters)" else null,
            bnmcError = if (cleanBnmc.length !in 4..30) "Enter valid BNMC reg. no." else null,
            phoneError = if (!SecurityUtils.isValidBangladeshPhone(cleanPhone)) "Enter valid BD mobile (01XXXXXXXXX)" else null,
            whatsappError = if (!whatsappSameAsPhone && !SecurityUtils.isValidBangladeshPhone(cleanWhatsapp)) {
                "Enter valid BD WhatsApp number"
            } else null,
            emailError = if (cleanEmail.isNotEmpty() && (!SecurityUtils.isValidEmail(cleanEmail) || cleanEmail.length > 120)) {
                "Enter a valid email address"
            } else null,
            hospitalError = if (cleanHospital.length !in 3..120) "Enter hospital or workplace name" else null
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 640.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HowToReg,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Nurse Registration Form",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "ফর্ম পূরণ করুন — অ্যাডমিন অনুমোদনের পর আপনার তথ্য লাইভ ডিরেক্টরিতে যুক্ত হবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_error_banner")
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            SectionCard(title = "Personal & BNMC Credentials") {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        fieldErrors = fieldErrors.copy(fullNameError = null)
                        errorMessage = null
                    },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g., Nusrat Jahan") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    isError = fieldErrors.fullNameError != null,
                    supportingText = fieldErrors.fullNameError?.let { err -> { Text(err) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_full_name")
                )

                OutlinedTextField(
                    value = bnmcNumber,
                    onValueChange = {
                        bnmcNumber = it
                        fieldErrors = fieldErrors.copy(bnmcError = null)
                        errorMessage = null
                    },
                    label = { Text("BNMC Registration No. *") },
                    placeholder = { Text("e.g., BNMC-48920") },
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                    isError = fieldErrors.bnmcError != null,
                    supportingText = fieldErrors.bnmcError?.let { err -> { Text(err) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_bnmc")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            fieldErrors = fieldErrors.copy(phoneError = null)
                            errorMessage = null
                        },
                        label = { Text("Mobile (BD) *") },
                        placeholder = { Text("017XXXXXXXX") },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                        isError = fieldErrors.phoneError != null,
                        supportingText = fieldErrors.phoneError?.let { err -> { Text(err) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("reg_phone")
                    )

                    OutlinedTextField(
                        value = experienceYearsText,
                        onValueChange = { input ->
                            experienceYearsText = input.filter { it.isDigit() }.take(2)
                        },
                        label = { Text("Exp (Yrs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("reg_experience")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "WhatsApp number is same as Mobile",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = whatsappSameAsPhone,
                        onCheckedChange = {
                            whatsappSameAsPhone = it
                            if (it) fieldErrors = fieldErrors.copy(whatsappError = null)
                        }
                    )
                }

                if (!whatsappSameAsPhone) {
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = {
                            whatsapp = it
                            fieldErrors = fieldErrors.copy(whatsappError = null)
                            errorMessage = null
                        },
                        label = { Text("WhatsApp Number *") },
                        placeholder = { Text("01XXXXXXXXX") },
                        isError = fieldErrors.whatsappError != null,
                        supportingText = fieldErrors.whatsappError?.let { err -> { Text(err) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_whatsapp")
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        fieldErrors = fieldErrors.copy(emailError = null)
                        errorMessage = null
                    },
                    label = { Text("Email Address (Optional)") },
                    placeholder = { Text("nurse@hospital.org") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                    isError = fieldErrors.emailError != null,
                    supportingText = fieldErrors.emailError?.let { err -> { Text(err) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_email")
                )
            }

            SectionCard(title = "Workplace & Bangladesh Location") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                        modifier = Modifier.weight(1f)
                    )

                    FormDropdownField(
                        label = "District *",
                        selected = selectedDistrict,
                        options = availableDistricts,
                        onSelect = { dist ->
                            selectedDistrict = dist
                            selectedUpazila = BangladeshData.upazilasForDistrict(dist).firstOrNull() ?: "$dist Sadar"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                FormDropdownField(
                    label = "Upazila / Thana *",
                    selected = selectedUpazila,
                    options = availableUpazilas,
                    onSelect = { selectedUpazila = it },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = {
                        hospitalName = it
                        fieldErrors = fieldErrors.copy(hospitalError = null)
                        errorMessage = null
                    },
                    label = { Text("Hospital / Clinic / Institution *") },
                    placeholder = { Text("e.g., Dhaka Medical College Hospital") },
                    leadingIcon = { Icon(Icons.Filled.LocalHospital, contentDescription = null) },
                    isError = fieldErrors.hospitalError != null,
                    supportingText = fieldErrors.hospitalError?.let { err -> { Text(err) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_hospital")
                )

                FormDropdownField(
                    label = "Designation",
                    selected = selectedDesignation,
                    options = BangladeshData.designations,
                    onSelect = { selectedDesignation = it },
                    modifier = Modifier.fillMaxWidth()
                )

                FormDropdownField(
                    label = "Qualification",
                    selected = selectedQualification,
                    options = BangladeshData.qualifications,
                    onSelect = { selectedQualification = it },
                    modifier = Modifier.fillMaxWidth()
                )

                FormDropdownField(
                    label = "Clinical Specialty",
                    selected = selectedSpecialty,
                    options = BangladeshData.specialties,
                    onSelect = { selectedSpecialty = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard(title = "Blood Group & Availability") {
                Text(
                    text = "Blood Group",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BangladeshData.bloodGroups.forEach { bg ->
                        FilterChip(
                            selected = selectedBloodGroup == bg,
                            onClick = { selectedBloodGroup = bg },
                            label = { Text(bg, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Current Shift Status",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AvailabilityStatus.entries.forEach { status ->
                        FilterChip(
                            selected = selectedAvailability == status,
                            onClick = { selectedAvailability = status },
                            label = { Text(status.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it.take(400) },
                    label = { Text("Short Note / Skills (Optional)") },
                    placeholder = { Text("Ward experience, emergency contact note...") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    val errors = validateFields()
                    fieldErrors = errors
                    if (errors.hasAny) {
                        errorMessage = "Please fix the highlighted fields before submitting."
                        return@Button
                    }

                    isSubmitting = true
                    errorMessage = null
                    val entity = NurseEntity(
                        fullName = fullName,
                        bnmcNumber = bnmcNumber,
                        designation = selectedDesignation,
                        qualification = selectedQualification,
                        specialty = selectedSpecialty,
                        division = selectedDivision,
                        district = selectedDistrict,
                        upazila = selectedUpazila,
                        hospitalName = hospitalName,
                        phone = phone,
                        whatsapp = if (whatsappSameAsPhone) phone else whatsapp,
                        email = email,
                        bloodGroup = selectedBloodGroup,
                        availabilityStatus = selectedAvailability.name,
                        experienceYears = (experienceYearsText.toIntOrNull() ?: 1).coerceIn(0, 60),
                        bio = bio.trim(),
                        approvalStatus = ApprovalStatus.PENDING.name
                    )
                    onSubmitRegistration(entity) { result ->
                        isSubmitting = false
                        result.onSuccess {
                            fullName = ""
                            bnmcNumber = ""
                            phone = ""
                            whatsapp = ""
                            hospitalName = ""
                            bio = ""
                            fieldErrors = RegistrationFieldErrors()
                            showSuccessDialog = true
                        }.onFailure { err ->
                            errorMessage = err.message ?: "Could not submit registration."
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_registration_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isSubmitting) "Submitting to Cloud..." else "Submit for Admin Approval",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = { Text("Registration Submitted!") },
            text = {
                Text(
                    "আপনার নিবন্ধন সফলভাবে লাইভ Cloud Firestore-এ জমা হয়েছে (Status: Pending)। অ্যাডমিন অনুমোদনের পর ডিরেক্টরিতে আপনার প্রোফাইল প্রকাশিত হবে।"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateAfterSuccess()
                    }
                ) {
                    Text(successActionLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("Add Another")
                }
            }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
