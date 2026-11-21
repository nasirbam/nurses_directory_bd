package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BangladeshData
import com.example.data.NurseEntity
import com.example.ui.ContactActions
import com.example.ui.DirectoryUiState

@Composable
fun DirectoryScreen(
    uiState: DirectoryUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectDivision: (String) -> Unit,
    onSelectDistrict: (String) -> Unit,
    onSelectUpazila: (String) -> Unit,
    onSelectHospital: (String) -> Unit,
    onSelectSpecialty: (String) -> Unit,
    onSelectBloodGroup: (String) -> Unit,
    onToggleEmergencyOnly: () -> Unit,
    onResetFilters: () -> Unit,
    onSelectNurse: (NurseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDistrictMenu by remember { mutableStateOf(false) }
    var showUpazilaMenu by remember { mutableStateOf(false) }
    var showHospitalMenu by remember { mutableStateOf(false) }
    var showSpecialtyMenu by remember { mutableStateOf(false) }
    var showBloodMenu by remember { mutableStateOf(false) }

    val divisionsWithAll = remember { listOf("All") + BangladeshData.divisions }
    val hasActiveExtraFilter = uiState.filters.selectedDistrict != "All" ||
        uiState.filters.selectedUpazila != "All" ||
        uiState.filters.selectedHospital != "All" ||
        uiState.filters.selectedSpecialty != "All" ||
        uiState.filters.selectedBloodGroup != "All" ||
        uiState.filters.emergencyOnly

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = if (maxWidth >= 680.dp) 2 else 1

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero Banner Card
            item(span = { GridItemSpan(columns) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner),
                            contentDescription = stringResource(id = R.string.hero_banner_desc),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x66002E27),
                                            Color(0xDD003B32)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0x33FFFFFF)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${uiState.totalVerifiedCount} Verified Nurses • 8 Divisions",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Bangladesh Nurses E-Directory",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "সারা বাংলাদেশের নার্সদের তথ্য ও দ্রুত যোগাযোগ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFE0F7F1)
                                )
                            }
                        }
                    }
                }
            }

            // Search Field
            item(span = { GridItemSpan(columns) }) {
                OutlinedTextField(
                    value = uiState.filters.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("directory_search_input"),
                    placeholder = {
                        Text(
                            "Search by Name, Hospital, or Mobile No...",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search nurses",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (uiState.filters.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )
            }

            // Division Chips
            item(span = { GridItemSpan(columns) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(divisionsWithAll) { division ->
                            val selected = uiState.filters.selectedDivision == division
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectDivision(division) },
                                label = {
                                    Text(
                                        text = if (division == "All") "All Divisions" else division,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("division_chip_$division")
                            )
                        }
                    }

                    // Cascading Location Filter Row (District / Jela -> Upazila -> Hospital)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Box {
                                FilterChip(
                                    selected = uiState.filters.selectedDistrict != "All",
                                    onClick = { showDistrictMenu = true },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.LocationCity,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            if (uiState.filters.selectedDistrict == "All") "Jela (District)"
                                            else uiState.filters.selectedDistrict
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.testTag("district_filter_chip")
                                )
                                DropdownMenu(
                                    expanded = showDistrictMenu,
                                    onDismissRequest = { showDistrictMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Districts (Jela)") },
                                        onClick = {
                                            onSelectDistrict("All")
                                            showDistrictMenu = false
                                        }
                                    )
                                    uiState.availableDistricts.forEach { district ->
                                        DropdownMenuItem(
                                            text = { Text(district) },
                                            onClick = {
                                                onSelectDistrict(district)
                                                showDistrictMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Box {
                                FilterChip(
                                    selected = uiState.filters.selectedUpazila != "All",
                                    onClick = { showUpazilaMenu = true },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            if (uiState.filters.selectedUpazila == "All") "Upazila"
                                            else uiState.filters.selectedUpazila
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.testTag("upazila_filter_chip")
                                )
                                DropdownMenu(
                                    expanded = showUpazilaMenu,
                                    onDismissRequest = { showUpazilaMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Upazilas") },
                                        onClick = {
                                            onSelectUpazila("All")
                                            showUpazilaMenu = false
                                        }
                                    )
                                    uiState.availableUpazilas.forEach { upazila ->
                                        DropdownMenuItem(
                                            text = { Text(upazila) },
                                            onClick = {
                                                onSelectUpazila(upazila)
                                                showUpazilaMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Box {
                                FilterChip(
                                    selected = uiState.filters.selectedHospital != "All",
                                    onClick = { showHospitalMenu = true },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.LocalHospital,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            if (uiState.filters.selectedHospital == "All") "Hospital"
                                            else uiState.filters.selectedHospital,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.testTag("hospital_filter_chip")
                                )
                                DropdownMenu(
                                    expanded = showHospitalMenu,
                                    onDismissRequest = { showHospitalMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Hospitals") },
                                        onClick = {
                                            onSelectHospital("All")
                                            showHospitalMenu = false
                                        }
                                    )
                                    uiState.availableHospitals.forEach { hospital ->
                                        DropdownMenuItem(
                                            text = { Text(hospital) },
                                            onClick = {
                                                onSelectHospital(hospital)
                                                showHospitalMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Secondary Filter Row (Specialty, Blood Group, Emergency)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Box {
                                FilterChip(
                                    selected = uiState.filters.selectedSpecialty != "All",
                                    onClick = { showSpecialtyMenu = true },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.MedicalServices,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            if (uiState.filters.selectedSpecialty == "All") "Specialty"
                                            else uiState.filters.selectedSpecialty
                                        )
                                    },
                                    modifier = Modifier.testTag("specialty_filter_chip")
                                )
                                DropdownMenu(
                                    expanded = showSpecialtyMenu,
                                    onDismissRequest = { showSpecialtyMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Specialties") },
                                        onClick = {
                                            onSelectSpecialty("All")
                                            showSpecialtyMenu = false
                                        }
                                    )
                                    BangladeshData.specialties.forEach { specialty ->
                                        DropdownMenuItem(
                                            text = { Text(specialty) },
                                            onClick = {
                                                onSelectSpecialty(specialty)
                                                showSpecialtyMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Box {
                                FilterChip(
                                    selected = uiState.filters.selectedBloodGroup != "All",
                                    onClick = { showBloodMenu = true },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Bloodtype,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            if (uiState.filters.selectedBloodGroup == "All") "Blood Group"
                                            else "Blood: ${uiState.filters.selectedBloodGroup}"
                                        )
                                    },
                                    modifier = Modifier.testTag("blood_filter_chip")
                                )
                                DropdownMenu(
                                    expanded = showBloodMenu,
                                    onDismissRequest = { showBloodMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Any Blood Group") },
                                        onClick = {
                                            onSelectBloodGroup("All")
                                            showBloodMenu = false
                                        }
                                    )
                                    BangladeshData.bloodGroups.forEach { bg ->
                                        DropdownMenuItem(
                                            text = { Text(bg) },
                                            onClick = {
                                                onSelectBloodGroup(bg)
                                                showBloodMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            FilterChip(
                                selected = uiState.filters.emergencyOnly,
                                onClick = onToggleEmergencyOnly,
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.LocalHospital,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text("Active / Emergency") },
                                modifier = Modifier.testTag("emergency_filter_chip")
                            )
                        }

                        if (hasActiveExtraFilter || uiState.filters.selectedDivision != "All" || uiState.filters.searchQuery.isNotBlank()) {
                            item {
                                TextButton(onClick = onResetFilters) {
                                    Text("Reset")
                                }
                            }
                        }
                    }
                }
            }

            // Results Count Header
            item(span = { GridItemSpan(columns) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.filteredNurses.size} Nurses Available",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Tap card for full profile",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Empty State or Nurse List
            if (uiState.filteredNurses.isEmpty() && !uiState.isLoading) {
                item(span = { GridItemSpan(columns) }) {
                    AnimatedVisibility(visible = true) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            shape = MaterialTheme.shapes.large,
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
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "No matching nurses found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Try clearing filters or searching another name, hospital, upazila, or mobile number.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(14.dp))
                                OutlinedButton(onClick = onResetFilters) {
                                    Text("Show All Nurses")
                                }
                            }
                        }
                    }
                }
            } else {
                items(
                    items = uiState.filteredNurses,
                    key = { it.id }
                ) { nurse ->
                    com.example.ui.components.NurseCard(
                        nurse = nurse,
                        onCardClick = { onSelectNurse(nurse) },
                        onCallClick = { ContactActions.dialPhone(context, nurse.phone) },
                        onMessageClick = {
                            ContactActions.openWhatsApp(
                                context,
                                nurse.whatsapp.ifBlank { nurse.phone },
                                nurse.fullName
                            )
                        }
                    )
                }
            }

            item(span = { GridItemSpan(columns) }) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
