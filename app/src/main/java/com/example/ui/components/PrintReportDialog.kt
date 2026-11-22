package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AdminEntity
import com.example.data.ApprovalStatus
import com.example.data.BangladeshData
import com.example.data.NurseCategory
import com.example.data.NurseEntity
import com.example.ui.PrintColumn
import com.example.ui.PrintReportHelper
import com.example.ui.screens.FormDropdownField

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrintReportDialog(
    activeAdmin: AdminEntity,
    allNurses: List<NurseEntity>,
    onDismiss: () -> Unit,
    onPrintStarted: (String) -> Unit,
    onPrintError: (String) -> Unit
) {
    val context = LocalContext.current
    val divisionLock = activeAdmin.effectiveDivisionScope
    val isDivisionLocked = divisionLock != null

    var selectedStatusFilter by remember { mutableStateOf("Approved") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedDivision by remember(divisionLock) {
        mutableStateOf(divisionLock ?: "All")
    }
    var selectedDistrict by remember { mutableStateOf("All") }
    var selectedUpazila by remember { mutableStateOf("All") }
    var hospitalQuery by remember { mutableStateOf("") }

    var selectedColumns by remember {
        mutableStateOf(
            PrintColumn.entries.filter { it.defaultChecked }.toSet()
        )
    }

    val statusOptions = remember { listOf("Approved", "Pending", "Rejected", "All") }
    val categoryOptions = remember {
        listOf("All") + NurseCategory.entries.map { it.shortLabel }
    }
    val availableDistricts = remember(selectedDivision) {
        if (selectedDivision == "All") {
            listOf("All") + BangladeshData.districtsByDivision.values.flatten().distinct().sorted()
        } else {
            listOf("All") + (BangladeshData.districtsByDivision[selectedDivision] ?: emptyList())
        }
    }
    val availableUpazilas = remember(selectedDistrict, selectedDivision, allNurses) {
        when {
            selectedDistrict != "All" -> listOf("All") + BangladeshData.upazilasForDistrict(selectedDistrict)
            selectedDivision != "All" -> listOf("All") + allNurses
                .filter { it.division.equals(selectedDivision, ignoreCase = true) && it.upazila.isNotBlank() }
                .map { it.upazila }
                .distinct()
                .sorted()
            else -> listOf("All")
        }
    }

    val matchingNurses = remember(
        allNurses,
        divisionLock,
        selectedStatusFilter,
        selectedCategoryFilter,
        selectedDivision,
        selectedDistrict,
        selectedUpazila,
        hospitalQuery
    ) {
        val hq = hospitalQuery.trim().lowercase()
        allNurses.filter { nurse ->
            val matchesScope = divisionLock == null ||
                nurse.division.equals(divisionLock, ignoreCase = true)

            val matchesStatus = when (selectedStatusFilter) {
                "Approved" -> nurse.approvalEnum == ApprovalStatus.APPROVED
                "Pending" -> nurse.approvalEnum == ApprovalStatus.PENDING
                "Rejected" -> nurse.approvalEnum == ApprovalStatus.REJECTED
                else -> true
            }

            val matchesCategory = selectedCategoryFilter == "All" ||
                nurse.categoryEnum.shortLabel == selectedCategoryFilter

            val matchesDivision = selectedDivision == "All" ||
                nurse.division.equals(selectedDivision, ignoreCase = true)

            val matchesDistrict = selectedDistrict == "All" ||
                nurse.district.equals(selectedDistrict, ignoreCase = true)

            val matchesUpazila = selectedUpazila == "All" ||
                nurse.upazila.equals(selectedUpazila, ignoreCase = true)

            val matchesHospital = hq.isEmpty() ||
                nurse.hospitalName.lowercase().contains(hq) ||
                nurse.address.lowercase().contains(hq)

            matchesScope && matchesStatus && matchesCategory &&
                matchesDivision && matchesDistrict && matchesUpazila && matchesHospital
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Print, contentDescription = null) },
        title = { Text("Print / PDF Nurse Report") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isDivisionLocked) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Divisional Admin Scope: ${divisionLock} Division Only",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Super Admin Access: All Divisions",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "1. Filter Nurses (প্রিন্টের জন্য ফিল্টার করুন)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormDropdownField(
                        label = "Status",
                        selected = selectedStatusFilter,
                        options = statusOptions,
                        onSelect = { selectedStatusFilter = it },
                        modifier = Modifier.weight(1f)
                    )
                    FormDropdownField(
                        label = "Category",
                        selected = selectedCategoryFilter,
                        options = categoryOptions,
                        onSelect = { selectedCategoryFilter = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!isDivisionLocked) {
                    FormDropdownField(
                        label = "Division",
                        selected = selectedDivision,
                        options = listOf("All") + BangladeshData.divisions,
                        onSelect = { div ->
                            selectedDivision = div
                            selectedDistrict = "All"
                            selectedUpazila = "All"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormDropdownField(
                        label = "District",
                        selected = selectedDistrict,
                        options = availableDistricts,
                        onSelect = { dist ->
                            selectedDistrict = dist
                            selectedUpazila = "All"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FormDropdownField(
                        label = "Upazila",
                        selected = selectedUpazila,
                        options = availableUpazilas,
                        onSelect = { selectedUpazila = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = hospitalQuery,
                    onValueChange = { hospitalQuery = it },
                    label = { Text("Hospital / Institute Filter (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Matched Nurses: ${matchingNurses.size}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                HorizontalDivider()

                Text(
                    text = "2. Select Columns to Print (কলাম টিক দিন)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = {
                            selectedColumns = PrintColumn.entries.filter { it.defaultChecked }.toSet()
                        }
                    ) {
                        Text("Basic")
                    }
                    TextButton(
                        onClick = {
                            selectedColumns = setOf(
                                PrintColumn.NAME,
                                PrintColumn.FATHER_NAME,
                                PrintColumn.MOTHER_NAME,
                                PrintColumn.CATEGORY,
                                PrintColumn.DESIGNATION,
                                PrintColumn.REG_NO,
                                PrintColumn.GOVT_UID,
                                PrintColumn.MOBILE,
                                PrintColumn.WORKPLACE,
                                PrintColumn.LOCATION,
                                PrintColumn.EDUCATION
                            )
                        }
                    ) {
                        Text("Official")
                    }
                    TextButton(
                        onClick = {
                            selectedColumns = PrintColumn.entries.toSet()
                        }
                    ) {
                        Text("Select All")
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    PrintColumn.entries.forEach { column ->
                        val checked = column in selectedColumns
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp)
                                .clickable {
                                    selectedColumns = if (checked) {
                                        selectedColumns - column
                                    } else {
                                        selectedColumns + column
                                    }
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    selectedColumns = if (isChecked) {
                                        selectedColumns + column
                                    } else {
                                        selectedColumns - column
                                    }
                                },
                                modifier = Modifier.testTag("print_col_${column.name.lowercase()}")
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = column.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val orderedCols = PrintColumn.entries.filter { it in selectedColumns }
                    val scopeLabel = buildList {
                        add("Status: $selectedStatusFilter")
                        if (selectedCategoryFilter != "All") add("Category: $selectedCategoryFilter")
                        if (selectedDivision != "All") add("$selectedDivision Division") else add("All Bangladesh")
                        if (selectedDistrict != "All") add(selectedDistrict)
                        if (selectedUpazila != "All") add(selectedUpazila)
                        if (hospitalQuery.isNotBlank()) add("Workplace: ${hospitalQuery.trim()}")
                    }.joinToString(" • ")

                    PrintReportHelper.printNurseDirectoryPdf(
                        context = context,
                        nurses = matchingNurses,
                        selectedColumns = orderedCols,
                        reportTitle = if (isDivisionLocked) {
                            "Nurses BD — $divisionLock Division Report"
                        } else {
                            "Nurses BD — Official Directory Report"
                        },
                        scopeSubtitle = scopeLabel,
                        generatedBy = "${activeAdmin.fullName} (${activeAdmin.email})",
                        onStarted = {
                            onDismiss()
                            onPrintStarted("Opening system Print / Save PDF preview for ${matchingNurses.size} nurses...")
                        },
                        onError = onPrintError
                    )
                },
                enabled = matchingNurses.isNotEmpty() && selectedColumns.isNotEmpty(),
                modifier = Modifier.testTag("confirm_print_report_button")
            ) {
                Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Print / PDF (${matchingNurses.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
