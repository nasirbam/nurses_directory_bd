package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AdminEntity
import com.example.data.AdminRole
import com.example.data.ApprovalStatus
import com.example.data.AvailabilityStatus
import com.example.data.BangladeshData
import com.example.data.NurseEntity
import com.example.data.NursesRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DirectoryFilterState(
    val searchQuery: String = "",
    val selectedDivision: String = "All",
    val selectedDistrict: String = "All",
    val selectedUpazila: String = "All",
    val selectedHospital: String = "All",
    val selectedSpecialty: String = "All",
    val selectedBloodGroup: String = "All",
    val emergencyOnly: Boolean = false
)

data class DirectoryUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val totalVerifiedCount: Int = 0,
    val filteredNurses: List<NurseEntity> = emptyList(),
    val availableDistricts: List<String> = emptyList(),
    val availableUpazilas: List<String> = emptyList(),
    val availableHospitals: List<String> = emptyList(),
    val filters: DirectoryFilterState = DirectoryFilterState()
)

data class StatCount(
    val label: String,
    val count: Int
)

data class AnalyticsSummary(
    val totalRegistered: Int = 0,
    val totalApproved: Int = 0,
    val totalPending: Int = 0,
    val totalRejected: Int = 0,
    val emergencyReadyCount: Int = 0,
    val divisionsCovered: Int = 0,
    val totalAdmins: Int = 0,
    val divisionStats: List<StatCount> = emptyList(),
    val specialtyStats: List<StatCount> = emptyList(),
    val bloodGroupStats: List<StatCount> = emptyList()
)

data class AdminSessionState(
    val currentAdmin: AdminEntity? = null,
    val admins: List<AdminEntity> = emptyList(),
    val signedInEmail: String = "",
    val isCheckingRole: Boolean = true,
    val isAuthorizedAdmin: Boolean = false,
    val isSuperAdmin: Boolean = false,
    val primaryBootstrapEmail: String = NursesRepository.BOOTSTRAP_ADMIN_EMAIL
)

data class NurseListUiState(
    val searchQuery: String = "",
    val selectedStatus: ApprovalStatus? = null,
    val nurses: List<NurseEntity> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class NursesViewModel(
    private val repository: NursesRepository,
    val currentUserId: String,
    val currentUserEmail: String
) : ViewModel() {

    private companion object {
        const val TAG = "NursesVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }

    private fun <T> Flow<T>.toSafeUiStateFlow(errorLabel: String): Flow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, errorLabel, rootError)
                emit(UiState.Error(rootError.localizedMessage ?: errorLabel))
            }

    private val _filters = MutableStateFlow(DirectoryFilterState())
    private val _selectedNurse = MutableStateFlow<NurseEntity?>(null)
    val selectedNurse: StateFlow<NurseEntity?> = _selectedNurse.asStateFlow()

    private val _nurseListQuery = MutableStateFlow("")
    private val _nurseListStatusFilter = MutableStateFlow<ApprovalStatus?>(null)

    private val normalizedUserEmail = currentUserEmail.trim().lowercase()
    private val isBootstrapEmail = normalizedUserEmail == NursesRepository.BOOTSTRAP_ADMIN_EMAIL

    private val myAdminDocState: StateFlow<UiState<AdminEntity?>> =
        repository.observeMyAdminDoc(normalizedUserEmail)
            .toSafeUiStateFlow("Failed to verify admin role")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    private val isAdminFlow: Flow<Boolean> = myAdminDocState.map { state ->
        when (state) {
            is UiState.Success -> state.data != null || isBootstrapEmail
            is UiState.Loading -> isBootstrapEmail
            is UiState.Error -> isBootstrapEmail
        }
    }.distinctUntilChanged()

    private val approvedNursesState: StateFlow<UiState<List<NurseEntity>>> =
        repository.observeApprovedNurses()
            .toSafeUiStateFlow("Failed to load verified directory")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    private val mySubmissionsState: StateFlow<UiState<List<NurseEntity>>> =
        repository.observeMySubmissions(currentUserId)
            .toSafeUiStateFlow("Failed to load your submissions")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    private val adminNursesState: StateFlow<UiState<List<NurseEntity>>> =
        isAdminFlow.flatMapLatest { isAdmin ->
            if (!isAdmin) {
                flowOf(UiState.Success(emptyList()))
            } else {
                repository.observeAllNursesForAdmin()
                    .toSafeUiStateFlow("Failed to load admin nurse registry")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val adminListState: StateFlow<UiState<List<AdminEntity>>> =
        isAdminFlow.flatMapLatest { isAdmin ->
            if (!isAdmin) {
                flowOf(UiState.Success(emptyList()))
            } else {
                repository.observeAllAdmins()
                    .toSafeUiStateFlow("Failed to load admin list")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val allNurses: StateFlow<List<NurseEntity>> = adminNursesState.map { state ->
        (state as? UiState.Success)?.data ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val adminSessionState: StateFlow<AdminSessionState> = combine(
        myAdminDocState,
        adminListState
    ) { myDocState, adminsState ->
        val myDoc = (myDocState as? UiState.Success)?.data
        val admins = (adminsState as? UiState.Success)?.data ?: emptyList()
        val effectiveAdmin = myDoc ?: if (isBootstrapEmail) {
            AdminEntity(
                id = normalizedUserEmail,
                fullName = "Chief Directory Admin",
                email = normalizedUserEmail,
                role = AdminRole.SUPER_ADMIN.name,
                assignedDivision = "All Bangladesh",
                addedBy = "System Bootstrap"
            )
        } else {
            null
        }
        val authorized = effectiveAdmin != null
        val isSuper = isBootstrapEmail || effectiveAdmin?.roleEnum == AdminRole.SUPER_ADMIN
        AdminSessionState(
            currentAdmin = effectiveAdmin,
            admins = admins,
            signedInEmail = currentUserEmail,
            isCheckingRole = myDocState is UiState.Loading && !isBootstrapEmail,
            isAuthorizedAdmin = authorized,
            isSuperAdmin = isSuper
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        AdminSessionState(
            signedInEmail = currentUserEmail,
            isCheckingRole = !isBootstrapEmail,
            isAuthorizedAdmin = isBootstrapEmail,
            isSuperAdmin = isBootstrapEmail
        )
    )

    val directoryUiState: StateFlow<DirectoryUiState> = combine(
        approvedNursesState,
        _filters
    ) { approvedState, filters ->
        when (approvedState) {
            is UiState.Loading -> DirectoryUiState(isLoading = true, filters = filters)
            is UiState.Error -> DirectoryUiState(
                isLoading = false,
                errorMessage = approvedState.message,
                filters = filters
            )
            is UiState.Success -> {
                val approved = approvedState.data
                val districtsForDivision = if (filters.selectedDivision == "All") {
                    BangladeshData.districtsByDivision.values.flatten().distinct().sorted()
                } else {
                    BangladeshData.districtsByDivision[filters.selectedDivision] ?: emptyList()
                }
                val upazilasForSelection = when {
                    filters.selectedDistrict != "All" -> {
                        val staticUpazilas = BangladeshData.upazilasForDistrict(filters.selectedDistrict)
                        val dynamicUpazilas = approved
                            .filter { it.district.equals(filters.selectedDistrict, ignoreCase = true) && it.upazila.isNotBlank() }
                            .map { it.upazila }
                        (staticUpazilas + dynamicUpazilas).distinct()
                    }
                    filters.selectedDivision != "All" -> {
                        approved
                            .filter { it.division.equals(filters.selectedDivision, ignoreCase = true) && it.upazila.isNotBlank() }
                            .map { it.upazila }
                            .distinct()
                            .sorted()
                    }
                    else -> {
                        approved.mapNotNull { it.upazila.takeIf(String::isNotBlank) }.distinct().sorted()
                    }
                }
                val hospitalsForSelection = approved
                    .filter { nurse ->
                        (filters.selectedDivision == "All" || nurse.division.equals(filters.selectedDivision, ignoreCase = true)) &&
                            (filters.selectedDistrict == "All" || nurse.district.equals(filters.selectedDistrict, ignoreCase = true)) &&
                            (filters.selectedUpazila == "All" || nurse.upazila.equals(filters.selectedUpazila, ignoreCase = true))
                    }
                    .map { it.hospitalName }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()

                val q = filters.searchQuery.trim().lowercase()
                val qDigits = q.filter { it.isDigit() }
                val filtered = approved.filter { nurse ->
                    val matchesQuery = q.isEmpty() ||
                        nurse.fullName.lowercase().contains(q) ||
                        nurse.hospitalName.lowercase().contains(q) ||
                        nurse.district.lowercase().contains(q) ||
                        nurse.upazila.lowercase().contains(q) ||
                        nurse.division.lowercase().contains(q) ||
                        nurse.bnmcNumber.lowercase().contains(q) ||
                        nurse.specialty.lowercase().contains(q) ||
                        nurse.phone.contains(q) ||
                        (qDigits.isNotEmpty() && nurse.phone.filter { it.isDigit() }.contains(qDigits))

                    val matchesDivision = filters.selectedDivision == "All" ||
                        nurse.division.equals(filters.selectedDivision, ignoreCase = true)

                    val matchesDistrict = filters.selectedDistrict == "All" ||
                        nurse.district.equals(filters.selectedDistrict, ignoreCase = true)

                    val matchesUpazila = filters.selectedUpazila == "All" ||
                        nurse.upazila.equals(filters.selectedUpazila, ignoreCase = true)

                    val matchesHospital = filters.selectedHospital == "All" ||
                        nurse.hospitalName.equals(filters.selectedHospital, ignoreCase = true)

                    val matchesSpecialty = filters.selectedSpecialty == "All" ||
                        nurse.specialty.equals(filters.selectedSpecialty, ignoreCase = true)

                    val matchesBlood = filters.selectedBloodGroup == "All" ||
                        nurse.bloodGroup.equals(filters.selectedBloodGroup, ignoreCase = true)

                    val matchesEmergency = !filters.emergencyOnly ||
                        nurse.availabilityEnum == AvailabilityStatus.EMERGENCY_AVAILABLE ||
                        nurse.availabilityEnum == AvailabilityStatus.ON_DUTY

                    matchesQuery && matchesDivision && matchesDistrict && matchesUpazila &&
                        matchesHospital && matchesSpecialty && matchesBlood && matchesEmergency
                }
                DirectoryUiState(
                    isLoading = false,
                    totalVerifiedCount = approved.size,
                    filteredNurses = filtered,
                    availableDistricts = districtsForDivision,
                    availableUpazilas = upazilasForSelection,
                    availableHospitals = hospitalsForSelection,
                    filters = filters
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), DirectoryUiState())

    val nurseListUiState: StateFlow<NurseListUiState> = combine(
        approvedNursesState,
        mySubmissionsState,
        adminNursesState,
        adminSessionState,
        combine(_nurseListQuery, _nurseListStatusFilter) { q, s -> q to s }
    ) { approvedState, myState, adminState, session, queryAndStatus ->
        val (query, statusFilter) = queryAndStatus
        val combinedNurses = if (session.isAuthorizedAdmin) {
            (adminState as? UiState.Success)?.data
                ?: ((approvedState as? UiState.Success)?.data ?: emptyList())
        } else {
            val approved = (approvedState as? UiState.Success)?.data ?: emptyList()
            val mine = (myState as? UiState.Success)?.data ?: emptyList()
            (mine + approved).distinctBy { it.id.ifBlank { it.bnmcNumber } }
        }

        val q = query.trim().lowercase()
        val matching = combinedNurses.filter { nurse ->
            val matchesQuery = q.isEmpty() ||
                nurse.fullName.lowercase().contains(q) ||
                nurse.division.lowercase().contains(q) ||
                nurse.district.lowercase().contains(q) ||
                nurse.upazila.lowercase().contains(q) ||
                nurse.hospitalName.lowercase().contains(q) ||
                nurse.phone.contains(q) ||
                nurse.bnmcNumber.lowercase().contains(q)
            val matchesStatus = statusFilter == null || nurse.approvalEnum == statusFilter
            matchesQuery && matchesStatus
        }

        val err = (approvedState as? UiState.Error)?.message
            ?: (adminState as? UiState.Error)?.message

        NurseListUiState(
            searchQuery = query,
            selectedStatus = statusFilter,
            nurses = matching,
            totalCount = combinedNurses.size,
            isLoading = approvedState is UiState.Loading,
            errorMessage = err
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), NurseListUiState())

    val analyticsSummary: StateFlow<AnalyticsSummary> = combine(
        adminNursesState,
        adminListState
    ) { nursesState, adminsState ->
        val nurses = (nursesState as? UiState.Success)?.data ?: emptyList()
        val admins = (adminsState as? UiState.Success)?.data ?: emptyList()

        val approved = nurses.filter { it.approvalEnum == ApprovalStatus.APPROVED }
        val pending = nurses.count { it.approvalEnum == ApprovalStatus.PENDING }
        val rejected = nurses.count { it.approvalEnum == ApprovalStatus.REJECTED }
        val emergencyReady = approved.count {
            it.availabilityEnum == AvailabilityStatus.EMERGENCY_AVAILABLE
        }
        val divisionCounts = BangladeshData.divisions.map { div ->
            StatCount(
                label = div,
                count = approved.count { it.division.equals(div, ignoreCase = true) }
            )
        }
        val divisionsCovered = divisionCounts.count { it.count > 0 }

        val specialtyCounts = approved
            .groupBy { it.specialty }
            .map { (spec, list) -> StatCount(spec, list.size) }
            .sortedByDescending { it.count }

        val bloodCounts = BangladeshData.bloodGroups.map { bg ->
            StatCount(bg, approved.count { it.bloodGroup == bg })
        }.filter { it.count > 0 }

        AnalyticsSummary(
            totalRegistered = nurses.size,
            totalApproved = approved.size,
            totalPending = pending,
            totalRejected = rejected,
            emergencyReadyCount = emergencyReady,
            divisionsCovered = divisionsCovered,
            totalAdmins = maxOf(admins.size, if (isBootstrapEmail) 1 else 0),
            divisionStats = divisionCounts,
            specialtyStats = specialtyCounts,
            bloodGroupStats = bloodCounts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AnalyticsSummary())

    init {
        viewModelScope.launch {
            isAdminFlow.collect { isAdmin ->
                if (isAdmin) {
                    repository.ensureBootstrapAdminAndSampleData(forceSeedIfEmpty = true)
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _filters.update { it.copy(searchQuery = query) }
    }

    fun selectDivisionFilter(division: String) {
        _filters.update {
            it.copy(
                selectedDivision = division,
                selectedDistrict = "All",
                selectedUpazila = "All",
                selectedHospital = "All"
            )
        }
    }

    fun selectDistrictFilter(district: String) {
        _filters.update {
            it.copy(
                selectedDistrict = district,
                selectedUpazila = "All",
                selectedHospital = "All"
            )
        }
    }

    fun selectUpazilaFilter(upazila: String) {
        _filters.update {
            it.copy(
                selectedUpazila = upazila,
                selectedHospital = "All"
            )
        }
    }

    fun selectHospitalFilter(hospital: String) {
        _filters.update { it.copy(selectedHospital = hospital) }
    }

    fun selectSpecialtyFilter(specialty: String) {
        _filters.update { it.copy(selectedSpecialty = specialty) }
    }

    fun selectBloodGroupFilter(bloodGroup: String) {
        _filters.update { it.copy(selectedBloodGroup = bloodGroup) }
    }

    fun toggleEmergencyOnly() {
        _filters.update { it.copy(emergencyOnly = !it.emergencyOnly) }
    }

    fun resetFilters() {
        _filters.value = DirectoryFilterState()
    }

    fun updateNurseListSearchQuery(query: String) {
        _nurseListQuery.value = query
    }

    fun selectNurseListStatus(status: ApprovalStatus?) {
        _nurseListStatusFilter.value = status
    }

    fun openNurseDetail(nurse: NurseEntity) {
        _selectedNurse.value = nurse
    }

    fun closeNurseDetail() {
        _selectedNurse.value = null
    }

    fun submitRegistration(
        nurse: NurseEntity,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.submitRegistration(nurse)
            onResult(result)
        }
    }

    fun seedSampleData(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.ensureBootstrapAdminAndSampleData(forceSeedIfEmpty = true)
            onResult(result)
        }
    }

    fun approveNurse(nurse: NurseEntity, onResult: (Result<Unit>) -> Unit) {
        val reviewer = adminSessionState.value.currentAdmin?.email ?: normalizedUserEmail
        if (reviewer.isBlank()) {
            onResult(Result.failure(IllegalStateException("Admin session required.")))
            return
        }
        viewModelScope.launch {
            val result = repository.approveNurse(nurse, reviewer)
            onResult(result)
        }
    }

    fun rejectNurse(nurse: NurseEntity, reason: String, onResult: (Result<Unit>) -> Unit) {
        val reviewer = adminSessionState.value.currentAdmin?.email ?: normalizedUserEmail
        if (reviewer.isBlank()) {
            onResult(Result.failure(IllegalStateException("Admin session required.")))
            return
        }
        viewModelScope.launch {
            val result = repository.rejectNurse(nurse, reason, reviewer)
            onResult(result)
        }
    }

    fun deleteNurse(nurseId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteNurse(nurseId)
            onResult(result)
        }
    }

    fun addAdmin(
        fullName: String,
        email: String,
        role: AdminRole,
        assignedDivision: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val addedBy = adminSessionState.value.currentAdmin?.email ?: normalizedUserEmail
        viewModelScope.launch {
            val result = repository.addAdmin(
                fullName = fullName,
                email = email,
                role = role,
                assignedDivision = assignedDivision,
                addedByEmail = addedBy
            )
            onResult(result)
        }
    }

    fun removeAdmin(target: AdminEntity, onResult: (Result<Unit>) -> Unit) {
        val currentEmail = adminSessionState.value.currentAdmin?.email ?: normalizedUserEmail
        viewModelScope.launch {
            val result = repository.removeAdmin(target, currentEmail)
            onResult(result)
        }
    }

    class Factory(
        private val repository: NursesRepository,
        private val currentUserId: String,
        private val currentUserEmail: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NursesViewModel(
                repository = repository,
                currentUserId = currentUserId,
                currentUserEmail = currentUserEmail
            ) as T
        }
    }
}
