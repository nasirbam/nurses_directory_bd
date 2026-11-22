package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AdminEntity
import com.example.data.AdminRole
import com.example.data.ApprovalStatus
import com.example.data.BangladeshData
import com.example.data.LiveNoticeEntity
import com.example.data.NurseEntity
import com.example.data.TransferRequestEntity
import com.example.data.TransferStatus
import com.example.ui.AdminSessionState
import com.example.ui.AnalyticsSummary
import com.example.ui.components.ApprovalFunnelCard
import com.example.ui.components.DivisionBarChartCard
import com.example.ui.components.PublishLiveNoticeDialog
import com.example.ui.components.SpecialtyDonutChartCard
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRejected

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    session: AdminSessionState,
    analytics: AnalyticsSummary,
    allNurses: List<NurseEntity>,
    transferRequests: List<TransferRequestEntity> = emptyList(),
    liveNotice: LiveNoticeEntity? = null,
    onSignOutAndSwitch: () -> Unit,
    onApproveNurse: (NurseEntity, (Result<Unit>) -> Unit) -> Unit,
    onRejectNurse: (NurseEntity, String, (Result<Unit>) -> Unit) -> Unit,
    onEditNurse: (NurseEntity) -> Unit = {},
    onDeleteNurse: (String, (Result<Unit>) -> Unit) -> Unit,
    onApproveTransfer: (TransferRequestEntity, (Result<Unit>) -> Unit) -> Unit = { _, cb -> cb(Result.success(Unit)) },
    onRejectTransfer: (TransferRequestEntity, String, (Result<Unit>) -> Unit) -> Unit = { _, _, cb -> cb(Result.success(Unit)) },
    onPublishLiveNotice: (String, String, Boolean, (Result<Unit>) -> Unit) -> Unit = { _, _, _, cb -> cb(Result.success(Unit)) },
    onAddAdmin: (String, String, AdminRole, String, (Result<Unit>) -> Unit) -> Unit,
    onRemoveAdmin: (AdminEntity, (Result<Unit>) -> Unit) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAdmin = session.currentAdmin
    if (activeAdmin == null) {
        AdminAuthGate(
            isSignedIn = true,
            signedInEmail = session.signedInEmail,
            isCheckingRole = session.isCheckingRole,
            onSignOutAndSwitch = onSignOutAndSwitch,
            modifier = modifier
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var approvalFilter by remember { mutableStateOf(ApprovalStatus.PENDING) }
    var transferFilter by remember { mutableStateOf(TransferStatus.PENDING) }
    var nurseToReject by remember { mutableStateOf<NurseEntity?>(null) }
    var nurseToDelete by remember { mutableStateOf<NurseEntity?>(null) }
    var transferToReject by remember { mutableStateOf<TransferRequestEntity?>(null) }
    var showAddAdminDialog by remember { mutableStateOf(false) }
    var showLiveNoticeDialog by remember { mutableStateOf(false) }
    var adminToRemove by remember { mutableStateOf<AdminEntity?>(null) }

    val pendingTransfersCount = remember(transferRequests) {
        transferRequests.count { it.statusEnum == TransferStatus.PENDING }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Logged-in Admin Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = activeAdmin.fullName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${activeAdmin.roleEnum.displayName} • ${activeAdmin.assignedDivision}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                TextButton(
                    onClick = onSignOutAndSwitch,
                    modifier = Modifier.testTag("admin_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Sign Out")
                }
            }
        }

        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Filled.Analytics, contentDescription = null) },
                text = { Text("Overview", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                modifier = Modifier.testTag("admin_tab_analytics")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = {
                    BadgedBox(
                        badge = {
                            if (analytics.totalPending > 0) {
                                Badge { Text("${analytics.totalPending}") }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.PendingActions, contentDescription = null)
                    }
                },
                text = { Text("Nurses", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                modifier = Modifier.testTag("admin_tab_approvals")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = {
                    BadgedBox(
                        badge = {
                            if (pendingTransfersCount > 0) {
                                Badge { Text("$pendingTransfersCount") }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                    }
                },
                text = { Text("Transfers", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                modifier = Modifier.testTag("admin_tab_transfers")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = { Icon(Icons.Filled.AddModerator, contentDescription = null) },
                text = { Text("Admins", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                modifier = Modifier.testTag("admin_tab_admins")
            )
        }

        when (selectedTab) {
            0 -> AdminDashboardScreen(
                analytics = analytics,
                pendingNurses = allNurses.filter { it.approvalEnum == ApprovalStatus.PENDING },
                pendingTransfersCount = pendingTransfersCount,
                liveNotice = liveNotice,
                onApprove = { nurse ->
                    onApproveNurse(nurse) { res ->
                        res.onSuccess { onShowMessage("${nurse.fullName} approved & published!") }
                            .onFailure { onShowMessage(it.message ?: "Approval failed") }
                    }
                },
                onRequestDelete = { nurseToDelete = it },
                onOpenApprovals = {
                    approvalFilter = ApprovalStatus.PENDING
                    selectedTab = 1
                },
                onOpenApprovedEdit = {
                    approvalFilter = ApprovalStatus.APPROVED
                    selectedTab = 1
                },
                onOpenTransfers = {
                    transferFilter = TransferStatus.PENDING
                    selectedTab = 2
                },
                onOpenLiveNotice = { showLiveNoticeDialog = true }
            )
            1 -> ApprovalsQueueTab(
                allNurses = allNurses,
                selectedStatus = approvalFilter,
                onSelectStatus = { approvalFilter = it },
                onApprove = { nurse ->
                    onApproveNurse(nurse) { res ->
                        res.onSuccess { onShowMessage("${nurse.fullName} approved & published!") }
                            .onFailure { onShowMessage(it.message ?: "Approval failed") }
                    }
                },
                onEdit = onEditNurse,
                onRequestReject = { nurseToReject = it },
                onRequestDelete = { nurseToDelete = it }
            )
            2 -> TransfersQueueTab(
                requests = transferRequests,
                selectedStatus = transferFilter,
                onSelectStatus = { transferFilter = it },
                onApproveTransfer = { req ->
                    onApproveTransfer(req) { res ->
                        res.onSuccess { onShowMessage("${req.nurseName} transferred to ${req.newHospitalName}!") }
                            .onFailure { onShowMessage(it.message ?: "Transfer approval failed") }
                    }
                },
                onRequestReject = { transferToReject = it }
            )
            3 -> AdminManagementTab(
                admins = session.admins,
                currentAdminEmail = activeAdmin.email,
                isSuperAdmin = session.isSuperAdmin,
                onAddAdminClick = { showAddAdminDialog = true },
                onRemoveAdminClick = { adminToRemove = it }
            )
        }
    }

    // Reject Reason Dialog
    nurseToReject?.let { nurse ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { nurseToReject = null },
            title = { Text("Reject Application") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Provide a brief reason for rejecting ${nurse.fullName}'s registration:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason (Optional)") },
                        placeholder = { Text("e.g., Unverified BNMC number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectNurse(nurse, reason) { res ->
                            res.onSuccess { onShowMessage("${nurse.fullName} rejected") }
                                .onFailure { onShowMessage(it.message ?: "Action failed") }
                        }
                        nurseToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { nurseToReject = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Nurse Confirmation
    nurseToDelete?.let { nurse ->
        AlertDialog(
            onDismissRequest = { nurseToDelete = null },
            title = { Text("Remove Nurse Record?") },
            text = { Text("Remove ${nurse.fullName} permanently from the directory?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNurse(nurse.id) { res ->
                            res.onSuccess { onShowMessage("${nurse.fullName} removed") }
                                .onFailure { onShowMessage(it.message ?: "Delete failed") }
                        }
                        nurseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { nurseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Admin Dialog
    if (showAddAdminDialog) {
        AddAdminDialog(
            onDismiss = { showAddAdminDialog = false },
            onConfirm = { name, email, role, division, onError ->
                onAddAdmin(name, email, role, division) { res ->
                    res.onSuccess {
                        showAddAdminDialog = false
                        onShowMessage("Admin $name added successfully!")
                    }.onFailure { err ->
                        onError(err.message ?: "Could not add admin")
                    }
                }
            }
        )
    }

    // Reject Transfer Confirmation
    transferToReject?.let { req ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { transferToReject = null },
            title = { Text("Reject Transfer Request") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Reject ${req.nurseName}'s transfer request to ${req.newHospitalName}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason (Optional)") },
                        placeholder = { Text("e.g., Transfer order verification pending") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectTransfer(req, reason) { res ->
                            res.onSuccess { onShowMessage("Transfer request rejected") }
                                .onFailure { onShowMessage(it.message ?: "Action failed") }
                        }
                        transferToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { transferToReject = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLiveNoticeDialog) {
        PublishLiveNoticeDialog(
            currentNotice = liveNotice,
            onDismiss = { showLiveNoticeDialog = false },
            onPublish = { title, message, active, onError ->
                onPublishLiveNotice(title, message, active) { res ->
                    res.onSuccess {
                        showLiveNoticeDialog = false
                        onShowMessage(
                            if (active) "Live broadcast updated across all phones!"
                            else "Live broadcast banner turned off"
                        )
                    }.onFailure { err ->
                        onError(err.message ?: "Could not publish live notice")
                    }
                }
            }
        )
    }

    // Remove Admin Confirmation
    adminToRemove?.let { target ->
        AlertDialog(
            onDismissRequest = { adminToRemove = null },
            title = { Text("Remove Admin?") },
            text = {
                Text("Revoke admin privileges for ${target.fullName} (${target.email})?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveAdmin(target) { res ->
                            res.onSuccess { onShowMessage("${target.fullName} removed from Admins") }
                                .onFailure { err -> onShowMessage(err.message ?: "Cannot remove admin") }
                        }
                        adminToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = { adminToRemove = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminDashboardScreen(
    analytics: AnalyticsSummary,
    pendingNurses: List<NurseEntity>,
    pendingTransfersCount: Int = 0,
    liveNotice: LiveNoticeEntity? = null,
    onApprove: (NurseEntity) -> Unit,
    onRequestDelete: (NurseEntity) -> Unit,
    onOpenApprovals: () -> Unit,
    onOpenApprovedEdit: () -> Unit = {},
    onOpenTransfers: () -> Unit = {},
    onOpenLiveNotice: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "Total Registered",
                        value = analytics.totalRegistered.toString(),
                        subtitle = "Live Cloud Firestore",
                        icon = Icons.Filled.Groups,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_total_registered")
                    )
                    KpiCard(
                        title = "Approved Nurses",
                        value = analytics.totalApproved.toString(),
                        subtitle = "Tap to Edit / Delete",
                        icon = Icons.Filled.HowToReg,
                        accentColor = StatusApproved,
                        onClick = onOpenApprovedEdit,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_active_nurses")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "Pending Approvals",
                        value = analytics.totalPending.toString(),
                        subtitle = "Tap to review queue",
                        icon = Icons.Filled.PendingActions,
                        accentColor = StatusPending,
                        onClick = onOpenApprovals,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_pending_approvals")
                    )
                    KpiCard(
                        title = "Transfer Requests",
                        value = pendingTransfersCount.toString(),
                        subtitle = "Tap to approve moves",
                        icon = Icons.Filled.SwapHoriz,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        onClick = onOpenTransfers,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_pending_transfers")
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live Broadcast Update",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = if (liveNotice?.active == true) {
                                "Active: ${liveNotice.title.ifBlank { liveNotice.message }}"
                            } else {
                                "অ্যাপ ডাউনলোড ছাড়াই সবার ফোনে তাৎক্ষণিক তথ্য আপডেট পাঠান"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    FilledTonalButton(
                        onClick = onOpenLiveNotice,
                        modifier = Modifier.testTag("admin_live_notice_button")
                    ) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Broadcast")
                    }
                }
            }
        }

        if (pendingNurses.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pending Approvals (${pendingNurses.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onOpenApprovals) {
                        Text("View All")
                    }
                }
            }

            items(pendingNurses, key = { "dash_pending_${it.id}" }) { nurse ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_pending_card_${nurse.id}"),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nurse.fullName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${nurse.designation} • ${nurse.bnmcNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusPending.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "PENDING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPending,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "${nurse.hospitalName} • ${nurse.district}, ${nurse.division}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApprove(nurse) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dash_approve_nurse_${nurse.id}")
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Approve")
                            }
                            OutlinedButton(
                                onClick = { onRequestDelete(nurse) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dash_delete_nurse_${nurse.id}"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }

        item {
            ApprovalFunnelCard(
                approved = analytics.totalApproved,
                pending = analytics.totalPending,
                rejected = analytics.totalRejected
            )
        }

        item {
            DivisionBarChartCard(divisionStats = analytics.divisionStats)
        }

        item {
            SpecialtyDonutChartCard(
                specialtyStats = analytics.specialtyStats,
                totalCount = analytics.totalApproved
            )
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
        }
    }
}

@Composable
private fun ApprovalsQueueTab(
    allNurses: List<NurseEntity>,
    selectedStatus: ApprovalStatus,
    onSelectStatus: (ApprovalStatus) -> Unit,
    onApprove: (NurseEntity) -> Unit,
    onEdit: (NurseEntity) -> Unit,
    onRequestReject: (NurseEntity) -> Unit,
    onRequestDelete: (NurseEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(allNurses, selectedStatus, searchQuery) {
        val q = searchQuery.trim().lowercase()
        allNurses.filter { nurse ->
            val matchesStatus = nurse.approvalEnum == selectedStatus
            val matchesSearch = q.isEmpty() ||
                nurse.fullName.lowercase().contains(q) ||
                nurse.bnmcNumber.lowercase().contains(q) ||
                nurse.hospitalName.lowercase().contains(q) ||
                nurse.phone.contains(q) ||
                nurse.district.lowercase().contains(q)
            matchesStatus && matchesSearch
        }
    }
    val pendingCount = allNurses.count { it.approvalEnum == ApprovalStatus.PENDING }
    val approvedCount = allNurses.count { it.approvalEnum == ApprovalStatus.APPROVED }
    val rejectedCount = allNurses.count { it.approvalEnum == ApprovalStatus.REJECTED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search nurse by name, BNMC, hospital, phone...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_nurses_search_input")
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedStatus == ApprovalStatus.PENDING,
                    onClick = { onSelectStatus(ApprovalStatus.PENDING) },
                    label = { Text("Pending ($pendingCount)") }
                )
                FilterChip(
                    selected = selectedStatus == ApprovalStatus.APPROVED,
                    onClick = { onSelectStatus(ApprovalStatus.APPROVED) },
                    label = { Text("Approved ($approvedCount)") }
                )
                FilterChip(
                    selected = selectedStatus == ApprovalStatus.REJECTED,
                    onClick = { onSelectStatus(ApprovalStatus.REJECTED) },
                    label = { Text("Rejected ($rejectedCount)") }
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PendingActions,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "No ${selectedStatus.name.lowercase()} nurses found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { nurse ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("approval_card_${nurse.id}"),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nurse.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${nurse.designation} • ${nurse.bnmcNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (nurse.approvalEnum) {
                                    ApprovalStatus.APPROVED -> StatusApproved.copy(alpha = 0.14f)
                                    ApprovalStatus.PENDING -> StatusPending.copy(alpha = 0.14f)
                                    ApprovalStatus.REJECTED -> StatusRejected.copy(alpha = 0.14f)
                                }
                            ) {
                                Text(
                                    text = nurse.approvalEnum.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (nurse.approvalEnum) {
                                        ApprovalStatus.APPROVED -> StatusApproved
                                        ApprovalStatus.PENDING -> StatusPending
                                        ApprovalStatus.REJECTED -> StatusRejected
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        val loc = listOfNotNull(
                            nurse.upazila.takeIf { it.isNotBlank() },
                            nurse.district.takeIf { it.isNotBlank() },
                            nurse.division.takeIf { it.isNotBlank() }
                        ).joinToString(", ")
                        Text(
                            text = "${nurse.hospitalName} ($loc)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${nurse.specialty} • ${nurse.qualification} • Blood: ${nurse.bloodGroup} • ${nurse.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (nurse.rejectionReason.isNotBlank() && nurse.approvalEnum == ApprovalStatus.REJECTED) {
                            Text(
                                text = "Reason: ${nurse.rejectionReason}",
                                style = MaterialTheme.typography.labelMedium,
                                color = StatusRejected
                            )
                        }

                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (nurse.approvalEnum != ApprovalStatus.APPROVED) {
                                Button(
                                    onClick = { onApprove(nurse) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("approve_nurse_${nurse.id}")
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Approve")
                                }
                            }
                            if (nurse.approvalEnum == ApprovalStatus.PENDING) {
                                OutlinedButton(
                                    onClick = { onRequestReject(nurse) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reject_nurse_${nurse.id}"),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reject")
                                }
                            }
                            FilledTonalButton(
                                onClick = { onEdit(nurse) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_nurse_${nurse.id}")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit ${nurse.fullName}", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit")
                            }
                            OutlinedButton(
                                onClick = { onRequestDelete(nurse) },
                                modifier = Modifier.testTag("delete_nurse_${nurse.id}"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete ${nurse.fullName}", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransfersQueueTab(
    requests: List<TransferRequestEntity>,
    selectedStatus: TransferStatus,
    onSelectStatus: (TransferStatus) -> Unit,
    onApproveTransfer: (TransferRequestEntity) -> Unit,
    onRequestReject: (TransferRequestEntity) -> Unit
) {
    val filtered = remember(requests, selectedStatus) {
        requests.filter { it.statusEnum == selectedStatus }
    }
    val pendingCount = requests.count { it.statusEnum == TransferStatus.PENDING }
    val approvedCount = requests.count { it.statusEnum == TransferStatus.APPROVED }
    val rejectedCount = requests.count { it.statusEnum == TransferStatus.REJECTED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedStatus == TransferStatus.PENDING,
                    onClick = { onSelectStatus(TransferStatus.PENDING) },
                    label = { Text("Pending ($pendingCount)") }
                )
                FilterChip(
                    selected = selectedStatus == TransferStatus.APPROVED,
                    onClick = { onSelectStatus(TransferStatus.APPROVED) },
                    label = { Text("Approved ($approvedCount)") }
                )
                FilterChip(
                    selected = selectedStatus == TransferStatus.REJECTED,
                    onClick = { onSelectStatus(TransferStatus.REJECTED) },
                    label = { Text("Rejected ($rejectedCount)") }
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "No ${selectedStatus.name.lowercase()} transfer requests",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { req ->
                val statusColor = when (req.statusEnum) {
                    TransferStatus.APPROVED -> StatusApproved
                    TransferStatus.PENDING -> StatusPending
                    TransferStatus.REJECTED -> StatusRejected
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_card_${req.id}"),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = req.nurseName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = req.bnmcNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = req.statusEnum.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "From: ${req.currentHospitalName} (${req.currentUpazila}, ${req.currentDistrict})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "To: ${req.newHospitalName} (${req.newUpazila}, ${req.newDistrict}, ${req.newDivision})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (req.transferNote.isNotBlank()) {
                            Text(
                                text = "Note: ${req.transferNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (req.rejectionReason.isNotBlank() && req.statusEnum == TransferStatus.REJECTED) {
                            Text(
                                text = "Rejection reason: ${req.rejectionReason}",
                                style = MaterialTheme.typography.labelMedium,
                                color = StatusRejected
                            )
                        }

                        if (req.statusEnum == TransferStatus.PENDING) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onApproveTransfer(req) },
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .testTag("approve_transfer_${req.id}")
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Approve & Update")
                                }
                                OutlinedButton(
                                    onClick = { onRequestReject(req) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reject_transfer_${req.id}"),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminManagementTab(
    admins: List<AdminEntity>,
    currentAdminEmail: String,
    isSuperAdmin: Boolean,
    onAddAdminClick: () -> Unit,
    onRemoveAdminClick: (AdminEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin Access Control",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "অ্যাডমিন যোগ বা অপসারণ করুন (${admins.size} active)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (isSuperAdmin) {
                        Spacer(Modifier.width(10.dp))
                        Button(
                            onClick = onAddAdminClick,
                            modifier = Modifier.testTag("add_admin_button")
                        ) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add Admin")
                        }
                    }
                }
            }
        }

        items(admins, key = { it.id }) { admin ->
            val isSelf = admin.email.equals(currentAdminEmail, ignoreCase = true)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_item_${admin.id}"),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = admin.fullName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSelf) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "You",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = admin.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${admin.roleEnum.displayName} • ${admin.assignedDivision}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (isSuperAdmin && !isSelf && !admin.email.equals(com.example.data.NursesRepository.BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true)) {
                        IconButton(
                            onClick = { onRemoveAdminClick(admin) },
                            modifier = Modifier.testTag("remove_admin_${admin.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Remove ${admin.fullName}",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddAdminDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        email: String,
        role: AdminRole,
        division: String,
        onError: (String) -> Unit
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(AdminRole.MODERATOR) }
    var division by remember { mutableStateOf("All Bangladesh") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Admin") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorText = null },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_admin_name")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorText = null },
                    label = { Text("Google Email Address *") },
                    placeholder = { Text("admin@gmail.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_admin_email")
                )
                FormDropdownField(
                    label = "Role",
                    selected = role.displayName,
                    options = AdminRole.entries.map { it.displayName },
                    onSelect = { selected ->
                        role = AdminRole.entries.first { it.displayName == selected }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                FormDropdownField(
                    label = "Assigned Division",
                    selected = division,
                    options = listOf("All Bangladesh") + BangladeshData.divisions,
                    onSelect = { division = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "নতুন অ্যাডমিন এই Google ইমেইল দিয়ে Sign in with Google করলে অ্যাডমিন প্যানেলে প্রবেশ করতে পারবেন।",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    onConfirm(name, email, role, division) { msg ->
                        errorText = msg
                    }
                },
                modifier = Modifier.testTag("confirm_add_admin")
            ) {
                Text("Add Admin")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
