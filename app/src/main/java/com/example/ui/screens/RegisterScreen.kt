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
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
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
import com.example.data.EducationEntry
import com.example.data.NurseCategory
import com.example.data.NurseEntity
import com.example.data.SecurityUtils
import com.example.data.TransferRequestEntity
import com.example.data.TransferStatus
import com.example.ui.auth.GoogleSignInButton
import com.example.ui.components.EducationInputSection
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRejected

private data class RegistrationFieldErrors(
    val fullNameError: String? = null,
    val fatherNameError: String? = null,
    val motherNameError: String? = null,
    val bnmcError: String? = null,
    val phoneError: String? = null,
    val whatsappError: String? = null,
    val emailError: String? = null,
    val addressError: String? = null,
    val hospitalError: String? = null
) {
    val hasAny: Boolean
        get() = fullNameError != null ||
            fatherNameError != null ||
            motherNameError != null ||
            bnmcError != null ||
            phoneError != null ||
            whatsappError != null ||
            emailError != null ||
            addressError != null ||
            hospitalError != null
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    isSignedIn: Boolean,
    signedInEmail: String = "",
    prefillName: String = "",
    isAdminUser: Boolean = false,
    mySubmissions: List<NurseEntity> = emptyList(),
    myTransferRequests: List<TransferRequestEntity> = emptyList(),
    onRequestTransfer: (NurseEntity) -> Unit = {},
    onGoogleAuthSuccess: () -> Unit = {},
    onSubmitRegistration: (NurseEntity, Boolean, (Result<Unit>) -> Unit) -> Unit,
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

    var selectedCategory by remember { mutableStateOf(NurseCategory.GOVT) }
    var fullName by remember(prefillName) { mutableStateOf(prefillName) }
    var fatherName by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var bnmcNumber by remember { mutableStateOf("") }
    var govtUid by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsappSameAsPhone by remember { mutableStateOf(true) }
    var whatsapp by remember { mutableStateOf("") }
    var email by remember(signedInEmail) { mutableStateOf(signedInEmail) }
    var address by remember { mutableStateOf("") }
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
    var educations by remember {
        mutableStateOf(listOf(EducationEntry(courseName = BangladeshData.courseNames.first())))
    }
    var selectedSpecialty by remember { mutableStateOf(BangladeshData.specialties.first()) }
    var selectedBloodGroup by remember { mutableStateOf("O+") }
    var selectedAvailability by remember { mutableStateOf(AvailabilityStatus.ON_DUTY) }
    var experienceYearsText by remember { mutableStateOf("3") }
    var bio by remember { mutableStateOf("") }
    var autoApproveAsAdmin by remember(isAdminUser) { mutableStateOf(isAdminUser) }

    var fieldErrors by remember { mutableStateOf(RegistrationFieldErrors()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastSubmittedWasAutoApproved by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val availableDistricts = remember(selectedDivision) {
        BangladeshData.districtsByDivision[selectedDivision] ?: listOf(selectedDivision)
    }
    val availableUpazilas = remember(selectedDistrict) {
        BangladeshData.upazilasForDistrict(selectedDistrict)
    }

    fun validateFields(): RegistrationFieldErrors {
        val cleanName = fullName.trim()
        val cleanFather = fatherName.trim()
        val cleanMother = motherName.trim()
        val cleanBnmc = bnmcNumber.trim()
        val cleanPhone = phone.replace(Regex("[\\s\\-]"), "")
        val cleanWhatsapp = whatsapp.replace(Regex("[\\s\\-]"), "")
        val cleanEmail = email.trim()
        val cleanAddress = address.trim()
        val cleanHospital = hospitalName.trim()

        val hospitalErr = when (selectedCategory) {
            NurseCategory.GOVT -> if (cleanHospital.length !in 3..120) {
                "Enter Govt. hospital / working place (3–120 chars)"
            } else null
            NurseCategory.STUDENT -> if (cleanHospital.length !in 3..120) {
                "Enter educational institute name (3–120 chars)"
            } else null
            NurseCategory.NON_GOVT -> if (cleanHospital.length > 120) {
                "Working place must be 120 chars or less"
            } else null
        }

        val addressErr = if (selectedCategory == NurseCategory.NON_GOVT && cleanAddress.length < 3) {
            "Present address (ঠিকানা) is mandatory for Non-Govt."
        } else null

        return RegistrationFieldErrors(
            fullNameError = if (cleanName.length !in 3..80) "Enter full name (3–80 characters)" else null,
            fatherNameError = if (cleanFather.length !in 2..80) "Enter father's name" else null,
            motherNameError = if (cleanMother.length !in 2..80) "Enter mother's name" else null,
            bnmcError = if (cleanBnmc.length !in 4..30) {
                if (selectedCategory == NurseCategory.STUDENT) "Enter valid Student Reg. No."
                else "Enter valid BNMC Reg. No."
            } else null,
            phoneError = if (!SecurityUtils.isValidBangladeshPhone(cleanPhone)) "Enter valid BD mobile (01XXXXXXXXX)" else null,
            whatsappError = if (!whatsappSameAsPhone && !SecurityUtils.isValidBangladeshPhone(cleanWhatsapp)) {
                "Enter valid BD WhatsApp number"
            } else null,
            emailError = if (cleanEmail.isNotEmpty() && (!SecurityUtils.isValidEmail(cleanEmail) || cleanEmail.length > 120)) {
                "Enter a valid email address"
            } else null,
            addressError = addressErr,
            hospitalError = hospitalErr
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

            if (mySubmissions.isNotEmpty() || myTransferRequests.isNotEmpty()) {
                SectionCard(title = "My Profiles & Transfer (প্রোফাইল ও বদলি আবেদন)") {
                    mySubmissions.forEach { profile ->
                        val hasPendingTransfer = myTransferRequests.any {
                            it.nurseId == profile.id && it.statusEnum == TransferStatus.PENDING
                        }
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("my_profile_card_${profile.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = profile.fullName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${profile.categoryEnum.shortLabel} • ${profile.hospitalName.ifBlank { profile.address.ifBlank { profile.district } }} • ${profile.district}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = "${profile.registrationNumberLabel}: ${profile.bnmcNumber}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (profile.approvalEnum == ApprovalStatus.APPROVED) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Verified,
                                                        contentDescription = "Verified",
                                                        tint = StatusApproved,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(Modifier.width(3.dp))
                                                    Text(
                                                        text = "Verified",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = StatusApproved
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    val statusColor = when (profile.approvalEnum) {
                                        ApprovalStatus.APPROVED -> StatusApproved
                                        ApprovalStatus.PENDING -> StatusPending
                                        ApprovalStatus.REJECTED -> StatusRejected
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = statusColor.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = profile.approvalEnum.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (profile.approvalEnum == ApprovalStatus.APPROVED) {
                                    if (hasPendingTransfer) {
                                        Text(
                                            text = "বদলির আবেদনটি অ্যাডমিনের অনুমোদনের অপেক্ষায় আছে।",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = StatusPending,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        FilledTonalButton(
                                            onClick = { onRequestTransfer(profile) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("my_profile_transfer_btn_${profile.id}")
                                        ) {
                                            Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                if (profile.categoryEnum == NurseCategory.NON_GOVT) {
                                                    "Transfer to Govt. Service / New Workplace (বদলি)"
                                                } else {
                                                    "Request Hospital Transfer (বদলি আবেদন)"
                                                }
                                            )
                                        }
                                    }
                                } else if (profile.approvalEnum == ApprovalStatus.REJECTED && profile.rejectionReason.isNotBlank()) {
                                    Text(
                                        text = "Reason: ${profile.rejectionReason}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusRejected
                                    )
                                }
                            }
                        }
                    }

                    if (myTransferRequests.isNotEmpty()) {
                        Text(
                            text = "Transfer Request Status",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        myTransferRequests.forEach { req ->
                            val reqColor = when (req.statusEnum) {
                                TransferStatus.APPROVED -> StatusApproved
                                TransferStatus.PENDING -> StatusPending
                                TransferStatus.REJECTED -> StatusRejected
                            }
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("my_transfer_card_${req.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = req.nurseName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = reqColor.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = req.statusEnum.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = reqColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${req.currentHospitalName.ifBlank { req.currentCategoryEnum.shortLabel }} ➔ ${req.newHospitalName} (${req.newCategoryEnum.shortLabel})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${req.newUpazila}, ${req.newDistrict}, ${req.newDivision}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (req.statusEnum == TransferStatus.REJECTED && req.rejectionReason.isNotBlank()) {
                                        Text(
                                            text = "Reason: ${req.rejectionReason}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusRejected
                                        )
                                    }
                                }
                            }
                        }
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

            SectionCard(title = "Category & Personal Credentials") {
                Text(
                    text = "Select Category * (ক্যাটাগরি নির্বাচন করুন)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NurseCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                                fieldErrors = fieldErrors.copy(hospitalError = null, addressError = null, bnmcError = null)
                                errorMessage = null
                                if (cat == NurseCategory.STUDENT && !selectedDesignation.startsWith("Student", ignoreCase = true)) {
                                    selectedDesignation = "Student Nurse"
                                } else if (cat != NurseCategory.STUDENT && selectedDesignation.startsWith("Student", ignoreCase = true)) {
                                    selectedDesignation = "Senior Staff Nurse"
                                }
                            },
                            label = { Text(cat.label) },
                            modifier = Modifier.testTag("reg_category_${cat.name.lowercase()}")
                        )
                    }
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        fieldErrors = fieldErrors.copy(fullNameError = null)
                        errorMessage = null
                    },
                    label = { Text("Name (Full Name) *") },
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
                    value = fatherName,
                    onValueChange = {
                        fatherName = it.take(80)
                        fieldErrors = fieldErrors.copy(fatherNameError = null)
                        errorMessage = null
                    },
                    label = { Text("Father's Name *") },
                    placeholder = { Text("পিতার নাম লিখুন") },
                    leadingIcon = { Icon(Icons.Filled.FamilyRestroom, contentDescription = null) },
                    isError = fieldErrors.fatherNameError != null,
                    supportingText = fieldErrors.fatherNameError?.let { err -> { Text(err) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_father_name")
                )

                OutlinedTextField(
                    value = motherName,
                    onValueChange = {
                        motherName = it.take(80)
                        fieldErrors = fieldErrors.copy(motherNameError = null)
                        errorMessage = null
                    },
                    label = { Text("Mother's Name *") },
                    placeholder = { Text("মাতার নাম লিখুন") },
                    leadingIcon = { Icon(Icons.Filled.FamilyRestroom, contentDescription = null) },
                    isError = fieldErrors.motherNameError != null,
                    supportingText = fieldErrors.motherNameError?.let { err -> { Text(err) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_mother_name")
                )

                OutlinedTextField(
                    value = bnmcNumber,
                    onValueChange = {
                        bnmcNumber = it
                        fieldErrors = fieldErrors.copy(bnmcError = null)
                        errorMessage = null
                    },
                    label = {
                        Text(
                            if (selectedCategory == NurseCategory.STUDENT) "Student Registration No. *"
                            else "BNMC Registration No. *"
                        )
                    },
                    placeholder = {
                        Text(
                            if (selectedCategory == NurseCategory.STUDENT) "e.g., STU-2025-1042"
                            else "e.g., BNMC-48920"
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                    isError = fieldErrors.bnmcError != null,
                    supportingText = fieldErrors.bnmcError?.let { err -> { Text(err) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_bnmc")
                )

                if (selectedCategory != NurseCategory.STUDENT) {
                    OutlinedTextField(
                        value = govtUid,
                        onValueChange = { govtUid = it.take(40) },
                        label = {
                            Text(
                                if (selectedCategory == NurseCategory.GOVT) {
                                    "Govt. UID (For Govt. Job Holders / চাকরিজীবী)"
                                } else {
                                    "Govt. UID (If Employed / Optional)"
                                }
                            )
                        },
                        placeholder = { Text("e.g., 199512345678") },
                        leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_govt_uid")
                    )
                }

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

            SectionCard(title = "Location, Workplace & Education") {
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
                    value = address,
                    onValueChange = {
                        address = it.take(200)
                        fieldErrors = fieldErrors.copy(addressError = null)
                        errorMessage = null
                    },
                    label = {
                        Text(
                            if (selectedCategory == NurseCategory.NON_GOVT) {
                                "Present Address (ঠিকানা) *"
                            } else {
                                "Present Address (ঠিকানা / Optional)"
                            }
                        )
                    },
                    placeholder = { Text("House, Road, Village / Ward, Post Office") },
                    leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    isError = fieldErrors.addressError != null,
                    supportingText = fieldErrors.addressError?.let { err -> { Text(err) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_address")
                )

                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = {
                        hospitalName = it
                        fieldErrors = fieldErrors.copy(hospitalError = null)
                        errorMessage = null
                    },
                    label = {
                        Text(
                            when (selectedCategory) {
                                NurseCategory.GOVT -> "Working Place / Govt. Hospital *"
                                NurseCategory.NON_GOVT -> "Working Place / Clinic (Optional)"
                                NurseCategory.STUDENT -> "Educational Institute (শিক্ষাপ্রতিষ্ঠান) *"
                            }
                        )
                    },
                    placeholder = {
                        Text(
                            when (selectedCategory) {
                                NurseCategory.GOVT -> "e.g., Dhaka Medical College Hospital"
                                NurseCategory.NON_GOVT -> "e.g., Square Hospital / Leave blank if unemployed"
                                NurseCategory.STUDENT -> "e.g., Dhaka Nursing College"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (selectedCategory == NurseCategory.STUDENT) Icons.Filled.School
                            else Icons.Filled.LocalHospital,
                            contentDescription = null
                        )
                    },
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

                EducationInputSection(
                    educations = educations,
                    onEducationsChange = { educations = it }
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

            if (isAdminUser) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Publish Immediately as Regular Nurse",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "অ্যাডমিন হিসেবে সাধারণ নার্স ডিরেক্টরিতে সরাসরি যুক্ত করুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = autoApproveAsAdmin,
                            onCheckedChange = { autoApproveAsAdmin = it }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val errors = validateFields()
                    fieldErrors = errors
                    if (errors.hasAny) {
                        errorMessage = "Please fix the highlighted fields before submitting."
                        return@Button
                    }
                    val validEducations = educations.filter { it.courseName.isNotBlank() }
                    if (validEducations.isEmpty()) {
                        errorMessage = "Select at least one educational qualification course."
                        return@Button
                    }

                    isSubmitting = true
                    errorMessage = null
                    val draft = NurseEntity(
                        fullName = fullName,
                        fatherName = fatherName,
                        motherName = motherName,
                        nurseCategory = selectedCategory.name,
                        bnmcNumber = bnmcNumber,
                        govtUid = if (selectedCategory == NurseCategory.STUDENT) "" else govtUid,
                        designation = selectedDesignation,
                        educations = validEducations,
                        specialty = selectedSpecialty,
                        division = selectedDivision,
                        district = selectedDistrict,
                        upazila = selectedUpazila,
                        address = address,
                        hospitalName = hospitalName,
                        phone = phone,
                        whatsapp = if (whatsappSameAsPhone) phone else whatsapp,
                        email = email,
                        bloodGroup = selectedBloodGroup,
                        availabilityStatus = selectedAvailability.name,
                        experienceYears = (experienceYearsText.toIntOrNull() ?: 1).coerceIn(0, 60),
                        bio = bio.trim(),
                        approvalStatus = if (isAdminUser && autoApproveAsAdmin) {
                            ApprovalStatus.APPROVED.name
                        } else {
                            ApprovalStatus.PENDING.name
                        }
                    )
                    val entity = draft.copy(
                        qualification = draft.educationSummaryText()
                            .ifBlank { validEducations.first().courseName }
                            .take(200)
                    )
                    val shouldAutoApprove = isAdminUser && autoApproveAsAdmin
                    onSubmitRegistration(entity, shouldAutoApprove) { result ->
                        isSubmitting = false
                        result.onSuccess {
                            lastSubmittedWasAutoApproved = shouldAutoApprove
                            fullName = ""
                            fatherName = ""
                            motherName = ""
                            bnmcNumber = ""
                            govtUid = ""
                            phone = ""
                            whatsapp = ""
                            address = ""
                            hospitalName = ""
                            educations = listOf(EducationEntry(courseName = BangladeshData.courseNames.first()))
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
                    text = when {
                        isSubmitting -> "Submitting to Cloud..."
                        isAdminUser && autoApproveAsAdmin -> "Publish Profile to Nurse Directory"
                        else -> "Submit for Admin Approval"
                    },
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
            title = {
                Text(
                    if (lastSubmittedWasAutoApproved) "Profile Published!"
                    else "Registration Submitted!"
                )
            },
            text = {
                Text(
                    if (lastSubmittedWasAutoApproved) {
                        "প্রোফাইলটি সফলভাবে অনুমোদিত ও সাধারণ নার্স ডিরেক্টরিতে প্রকাশিত হয়েছে।"
                    } else {
                        "আপনার নিবন্ধন সফলভাবে লাইভ Cloud Firestore-এ জমা হয়েছে (Status: Pending)। অ্যাডমিন অনুমোদনের পর ডিরেক্টরিতে আপনার প্রোফাইল প্রকাশিত হবে।"
                    }
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
