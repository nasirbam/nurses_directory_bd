package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ApprovalStatus
import com.example.data.NurseCategory
import com.example.data.NurseEntity
import com.example.ui.ContactActions
import com.example.ui.theme.StatusApproved

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NurseDetailBottomSheet(
    nurse: NurseEntity,
    onDismiss: () -> Unit,
    canEditAsAdmin: Boolean = false,
    canRequestTransfer: Boolean = false,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onRequestTransferClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val initials = nurse.fullName
                    .split(" ")
                    .filter { it.isNotBlank() && !it.startsWith("Md", ignoreCase = true) }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                    .ifBlank { nurse.fullName.take(2).uppercase() }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = nurse.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${nurse.designation} • ${nurse.categoryEnum.shortLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${nurse.registrationNumberLabel}: ${nurse.bnmcNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        if (nurse.approvalEnum == ApprovalStatus.APPROVED) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusApproved.copy(alpha = 0.14f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = "Verified",
                                        tint = StatusApproved,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
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
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = nurse.bloodGroup,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Blood",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Contact Action Bar
            Text(
                text = "দ্রুত যোগাযোগ (Quick Contact)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { ContactActions.dialPhone(context, nurse.phone) },
                    modifier = Modifier.testTag("detail_call_button")
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Call Now")
                }

                FilledTonalButton(
                    onClick = { ContactActions.openWhatsApp(context, nurse.whatsapp.ifBlank { nurse.phone }, nurse.fullName) },
                    modifier = Modifier.testTag("detail_whatsapp_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("WhatsApp")
                }

                OutlinedButton(
                    onClick = { ContactActions.sendSms(context, nurse.phone, nurse.fullName) },
                    modifier = Modifier.testTag("detail_sms_button")
                ) {
                    Icon(Icons.Filled.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("SMS")
                }

                if (nurse.email.isNotBlank()) {
                    OutlinedButton(
                        onClick = { ContactActions.sendEmail(context, nurse.email, nurse.fullName) },
                        modifier = Modifier.testTag("detail_email_button")
                    ) {
                        Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Email")
                    }
                }

                OutlinedButton(
                    onClick = { ContactActions.copyContact(context, nurse) },
                    modifier = Modifier.testTag("detail_copy_button")
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy")
                }

                OutlinedButton(
                    onClick = { ContactActions.shareNurseCard(context, nurse) },
                    modifier = Modifier.testTag("detail_share_button")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(14.dp))

            DetailRow(Icons.Filled.VerifiedUser, "Service Category", nurse.categoryEnum.label)
            if (nurse.govtUid.isNotBlank()) {
                DetailRow(Icons.Filled.Badge, "Govt. UID", nurse.govtUid)
            }
            if (nurse.fatherName.isNotBlank()) {
                DetailRow(Icons.Filled.FamilyRestroom, "Father's Name", nurse.fatherName)
            }
            if (nurse.motherName.isNotBlank()) {
                DetailRow(Icons.Filled.FamilyRestroom, "Mother's Name", nurse.motherName)
            }
            DetailRow(
                Icons.Filled.LocalHospital,
                nurse.workplaceLabel,
                nurse.hospitalName.ifBlank { "Not specified (Non-Govt.)" }
            )
            val locationText = listOfNotNull(
                nurse.upazila.takeIf { it.isNotBlank() },
                nurse.district.takeIf { it.isNotBlank() },
                nurse.division.takeIf { it.isNotBlank() }?.let { "$it Division" }
            ).joinToString(", ")
            DetailRow(Icons.Filled.LocationOn, "Location", locationText)
            if (nurse.address.isNotBlank()) {
                DetailRow(Icons.Filled.Home, "Present Address (ঠিকানা)", nurse.address)
            }
            DetailRow(Icons.Filled.MedicalServices, "Clinical Specialty", nurse.specialty)

            val degrees = nurse.effectiveEducations()
            val educationText = if (degrees.isNotEmpty()) {
                degrees.joinToString("\n") { "• ${it.formattedSummary()}" }
            } else {
                nurse.qualification
            }
            DetailRow(Icons.Filled.School, "Educational Qualifications", educationText)
            DetailRow(Icons.Filled.WorkHistory, "Experience", "${nurse.experienceYears} Years")
            DetailRow(Icons.Filled.Badge, "Phone / Contact", nurse.phone)
            if (nurse.email.isNotBlank()) {
                DetailRow(Icons.Filled.Email, "Email", nurse.email)
            }

            if (nurse.bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Profile Summary",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nurse.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (canRequestTransfer || canEditAsAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                if (canRequestTransfer) {
                    FilledTonalButton(
                        onClick = onRequestTransferClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_request_transfer_button")
                    ) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (nurse.categoryEnum == NurseCategory.NON_GOVT) {
                                "Apply for Govt. Service / Transfer (সরকারি চাকরি / বদলি)"
                            } else {
                                "Request Hospital Transfer (বদলি আবেদন)"
                            }
                        )
                    }
                }

                if (canEditAsAdmin) {
                    if (canRequestTransfer) Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("detail_admin_edit_button")
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit Nurse")
                        }
                        OutlinedButton(
                            onClick = onDeleteClick,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("detail_admin_delete_button")
                        ) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
