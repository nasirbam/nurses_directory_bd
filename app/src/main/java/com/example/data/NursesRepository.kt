package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NursesRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        const val BOOTSTRAP_ADMIN_EMAIL = "mdnasirnaznin@gmail.com"
        private val VALID_EMAIL_DOC_ID_REGEX =
            Regex("^[a-z0-9._%+\\-]+@[a-z0-9.\\-]+\\.[a-z]{2,}$")
    }

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        check(!uid.isNullOrBlank()) {
            "User must be signed in with Google before accessing Cloud Firestore."
        }
        return uid
    }

    private fun requireVerifiedEmail(): String {
        val email = auth.currentUser?.email?.trim()?.lowercase()
        check(!email.isNullOrBlank()) {
            "Verified Google email is required."
        }
        return email
    }

    fun observeApprovedNurses(): Flow<List<NurseEntity>> = flow {
        val uid = requireUserId()
        val path = "nurses"
        emitAll(
            db.collection(path)
                .whereEqualTo("approvalStatus", ApprovalStatus.APPROVED.name)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        NurseEntity::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeMySubmissions(uid: String): Flow<List<NurseEntity>> {
        if (uid.isBlank()) return flowOf(emptyList())
        val path = "nurses"
        return db.collection(path)
            .whereEqualTo("submittedByUid", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    NurseEntity::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeAllNursesForAdmin(): Flow<List<NurseEntity>> = flow {
        val uid = requireUserId()
        val path = "nurses"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        NurseEntity::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeMyAdminDoc(emailLower: String): Flow<AdminEntity?> {
        val cleanEmail = emailLower.trim().lowercase()
        if (cleanEmail.isBlank() || !VALID_EMAIL_DOC_ID_REGEX.matches(cleanEmail)) {
            return flowOf(null)
        }
        val uid = auth.currentUser?.uid ?: return flowOf(null)
        val path = "admins/$cleanEmail"
        return db.collection("admins").document(cleanEmail)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        AdminEntity::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, path)
                }
                throw error
            }
    }

    fun observeAllAdmins(): Flow<List<AdminEntity>> = flow {
        val uid = requireUserId()
        val path = "admins"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        AdminEntity::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun submitRegistration(nurse: NurseEntity): Result<Unit> {
        var docPath = "nurses"
        return try {
            val uid = requireUserId()
            val cleanedName = nurse.fullName.trim()
            val cleanedBnmc = nurse.bnmcNumber.trim().uppercase()
            val cleanedPhone = nurse.phone.replace(Regex("[\\s\\-]"), "")
            val cleanedHospital = nurse.hospitalName.trim()
            val cleanedEmail = nurse.email.trim()
            val cleanedBio = nurse.bio.trim()
            val expYears = nurse.experienceYears.coerceIn(0, 60)

            require(cleanedName.length in 3..80) {
                "Full name must be between 3 and 80 characters."
            }
            require(cleanedBnmc.length in 4..30) {
                "BNMC registration number must be between 4 and 30 characters."
            }
            require(SecurityUtils.isValidBangladeshPhone(cleanedPhone) && cleanedPhone.length in 11..20) {
                "Enter a valid Bangladeshi phone number (e.g. 017XXXXXXXX)."
            }
            require(cleanedHospital.length in 3..120) {
                "Hospital or workplace name must be between 3 and 120 characters."
            }
            if (cleanedEmail.isNotBlank()) {
                require(SecurityUtils.isValidEmail(cleanedEmail) && cleanedEmail.length <= 120) {
                    "Enter a valid email address (max 120 chars)."
                }
            }
            require(cleanedBio.length <= 400) {
                "Short note must be 400 characters or less."
            }
            require(nurse.bloodGroup in BangladeshData.bloodGroups) {
                "Select a valid blood group."
            }

            val whatsappNumber = if (nurse.whatsapp.isBlank()) {
                cleanedPhone
            } else {
                nurse.whatsapp.replace(Regex("[\\s\\-]"), "").also {
                    require(SecurityUtils.isValidBangladeshPhone(it) && it.length in 11..20) {
                        "Enter a valid Bangladeshi WhatsApp number (e.g. 017XXXXXXXX)."
                    }
                }
            }

            // Duplicate BNMC guard across approved directory and user's own submissions (rules-aligned)
            val approvedDup = db.collection("nurses")
                .whereEqualTo("approvalStatus", ApprovalStatus.APPROVED.name)
                .get()
                .await()
                .toObjects(NurseEntity::class.java)
                .any { it.bnmcNumber.equals(cleanedBnmc, ignoreCase = true) }

            val ownDup = db.collection("nurses")
                .whereEqualTo("submittedByUid", uid)
                .get()
                .await()
                .toObjects(NurseEntity::class.java)
                .any { it.bnmcNumber.equals(cleanedBnmc, ignoreCase = true) }

            require(!approvedDup && !ownDup) {
                "BNMC number $cleanedBnmc is already registered in the directory."
            }

            val docRef = db.collection("nurses").document()
            docPath = docRef.path

            val payload = mapOf(
                "submittedByUid" to uid,
                "fullName" to cleanedName,
                "bnmcNumber" to cleanedBnmc,
                "designation" to nurse.designation.trim().take(60),
                "qualification" to nurse.qualification.trim().take(80),
                "specialty" to nurse.specialty.trim().take(60),
                "division" to nurse.division.trim().take(40),
                "district" to nurse.district.trim().take(40),
                "upazila" to nurse.upazila.trim().take(50),
                "hospitalName" to cleanedHospital,
                "phone" to cleanedPhone,
                "whatsapp" to whatsappNumber,
                "email" to cleanedEmail,
                "bloodGroup" to nurse.bloodGroup,
                "availabilityStatus" to nurse.availabilityEnum.name,
                "experienceYears" to expYears,
                "bio" to cleanedBio,
                "approvalStatus" to ApprovalStatus.PENDING.name,
                "rejectionReason" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun approveNurse(nurse: NurseEntity, reviewerEmail: String): Result<Unit> {
        val docPath = "nurses/${nurse.id}"
        return try {
            requireUserId()
            require(nurse.id.isNotBlank()) { "Missing nurse document ID." }
            db.collection("nurses").document(nurse.id)
                .update(
                    mapOf(
                        "approvalStatus" to ApprovalStatus.APPROVED.name,
                        "rejectionReason" to "",
                        "reviewedBy" to reviewerEmail.trim().lowercase().take(128),
                        "reviewedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun rejectNurse(
        nurse: NurseEntity,
        reason: String,
        reviewerEmail: String
    ): Result<Unit> {
        val docPath = "nurses/${nurse.id}"
        return try {
            requireUserId()
            require(nurse.id.isNotBlank()) { "Missing nurse document ID." }
            val cleanReason = reason.trim().ifBlank { "Details could not be verified" }.take(250)
            db.collection("nurses").document(nurse.id)
                .update(
                    mapOf(
                        "approvalStatus" to ApprovalStatus.REJECTED.name,
                        "rejectionReason" to cleanReason,
                        "reviewedBy" to reviewerEmail.trim().lowercase().take(128),
                        "reviewedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteNurse(nurseId: String): Result<Unit> {
        val docPath = "nurses/$nurseId"
        return try {
            requireUserId()
            require(nurseId.isNotBlank()) { "Missing nurse document ID." }
            db.collection("nurses").document(nurseId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun ensureBootstrapAdminAndSampleData(forceSeedIfEmpty: Boolean = true): Result<Unit> {
        return try {
            val uid = requireUserId()
            val email = requireVerifiedEmail()

            if (email == BOOTSTRAP_ADMIN_EMAIL) {
                val adminRef = db.collection("admins").document(BOOTSTRAP_ADMIN_EMAIL)
                val adminSnap = adminRef.get().await()
                if (!adminSnap.exists()) {
                    adminRef.set(
                        mapOf(
                            "fullName" to "Chief Directory Admin",
                            "email" to BOOTSTRAP_ADMIN_EMAIL,
                            "role" to AdminRole.SUPER_ADMIN.name,
                            "assignedDivision" to "All Bangladesh",
                            "addedBy" to "System Bootstrap",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }

            if (forceSeedIfEmpty) {
                val existingNurses = db.collection("nurses").limit(1).get().await()
                if (existingNurses.isEmpty) {
                    val batch = db.batch()
                    initialBangladeshNurses().forEach { nurse ->
                        val ref = db.collection("nurses").document()
                        val data = mutableMapOf<String, Any>(
                            "submittedByUid" to uid,
                            "fullName" to nurse.fullName,
                            "bnmcNumber" to nurse.bnmcNumber,
                            "designation" to nurse.designation,
                            "qualification" to nurse.qualification,
                            "specialty" to nurse.specialty,
                            "division" to nurse.division,
                            "district" to nurse.district,
                            "upazila" to nurse.upazila,
                            "hospitalName" to nurse.hospitalName,
                            "phone" to nurse.phone,
                            "whatsapp" to nurse.whatsapp,
                            "email" to nurse.email,
                            "bloodGroup" to nurse.bloodGroup,
                            "availabilityStatus" to nurse.availabilityStatus,
                            "experienceYears" to nurse.experienceYears,
                            "bio" to nurse.bio,
                            "approvalStatus" to nurse.approvalStatus,
                            "rejectionReason" to "",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                        if (nurse.approvalStatus == ApprovalStatus.APPROVED.name) {
                            data["reviewedBy"] = email
                            data["reviewedAt"] = FieldValue.serverTimestamp()
                        }
                        batch.set(ref, data)
                    }
                    batch.commit().await()
                }
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "admins/nurses")
            Result.failure(e)
        }
    }

    suspend fun addAdmin(
        fullName: String,
        email: String,
        role: AdminRole,
        assignedDivision: String,
        addedByEmail: String
    ): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        val docPath = "admins/$cleanEmail"
        return try {
            requireUserId()
            val cleanName = fullName.trim()
            require(cleanName.length in 2..80) {
                "Admin name must be between 2 and 80 characters."
            }
            require(
                cleanEmail.length in 5..128 && VALID_EMAIL_DOC_ID_REGEX.matches(cleanEmail)
            ) {
                "Enter a valid Google email address."
            }
            val adminRef = db.collection("admins").document(cleanEmail)
            val existingSnap = adminRef.get().await()
            if (existingSnap.exists()) {
                throw IllegalStateException("An admin with this email already exists.")
            }

            adminRef.set(
                mapOf(
                    "fullName" to cleanName,
                    "email" to cleanEmail,
                    "role" to role.name,
                    "assignedDivision" to assignedDivision.trim().take(50),
                    "addedBy" to addedByEmail.trim().lowercase().take(128),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun removeAdmin(targetAdmin: AdminEntity, currentAdminEmail: String): Result<Unit> {
        val targetDocId = targetAdmin.id.ifBlank { targetAdmin.email.trim().lowercase() }
        val docPath = "admins/$targetDocId"
        return try {
            requireUserId()
            require(!targetDocId.equals(currentAdminEmail.trim(), ignoreCase = true)) {
                "You cannot remove your own currently signed-in admin account."
            }
            require(!targetDocId.equals(BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true)) {
                "The primary system administrator cannot be removed."
            }
            db.collection("admins").document(targetDocId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    private fun initialBangladeshNurses(): List<NurseEntity> {
        return listOf(
            NurseEntity(
                fullName = "Tahmina Akter Chowdhury",
                bnmcNumber = "BNMC-24108",
                designation = "Senior Staff Nurse",
                qualification = "B.Sc in Nursing",
                specialty = "ICU & Critical Care",
                division = "Dhaka",
                district = "Dhaka",
                upazila = "Shahbagh",
                hospitalName = "Dhaka Medical College Hospital (DMCH)",
                phone = "01711450218",
                whatsapp = "01711450218",
                email = "tahmina.dmch@nursesbd.org",
                bloodGroup = "O+",
                availabilityStatus = AvailabilityStatus.ON_DUTY.name,
                experienceYears = 9,
                bio = "Lead ICU coordinator handling ventilator care and rapid response shifts at DMCH.",
                approvalStatus = ApprovalStatus.APPROVED.name
            ),
            NurseEntity(
                fullName = "Md. Kamrul Hasan",
                bnmcNumber = "BNMC-29842",
                designation = "Nursing Officer",
                qualification = "M.Sc in Nursing",
                specialty = "Cardiology & CCU",
                division = "Dhaka",
                district = "Dhaka",
                upazila = "Shahbagh",
                hospitalName = "Bangabandhu Sheikh Mujib Medical University (BSMMU)",
                phone = "01819334510",
                whatsapp = "01819334510",
                email = "kamrul.bsmmu@nursesbd.org",
                bloodGroup = "A+",
                availabilityStatus = AvailabilityStatus.EMERGENCY_AVAILABLE.name,
                experienceYears = 11,
                bio = "Cardiac care nursing officer specializing in post-angiography CCU recovery.",
                approvalStatus = ApprovalStatus.APPROVED.name
            ),
            NurseEntity(
                fullName = "Sharmin Sultana",
                bnmcNumber = "BNMC-31204",
                designation = "Certified Midwife",
                qualification = "Diploma in Nursing & Midwifery",
                specialty = "Maternal & Midwifery",
                division = "Chattogram",
                district = "Chattogram",
                upazila = "Panchlaish",
                hospitalName = "Chattogram Medical College Hospital (CMCH)",
                phone = "01912778904",
                whatsapp = "01912778904",
                email = "sharmin.cmch@nursesbd.org",
                bloodGroup = "B+",
                availabilityStatus = AvailabilityStatus.ON_DUTY.name,
                experienceYears = 7,
                bio = "Dedicated labor & maternity ward midwife supporting safe delivery initiatives in Chattogram.",
                approvalStatus = ApprovalStatus.APPROVED.name
            ),
            NurseEntity(
                fullName = "Nusrat Jahan Mim",
                bnmcNumber = "BNMC-38490",
                designation = "Staff Nurse",
                qualification = "B.Sc in Nursing",
                specialty = "Pediatrics & NICU",
                division = "Sylhet",
                district = "Sylhet",
                upazila = "Sylhet Sadar",
                hospitalName = "Sylhet MAG Osmani Medical College Hospital",
                phone = "01611902341",
                whatsapp = "01611902341",
                email = "nusrat.somch@nursesbd.org",
                bloodGroup = "AB+",
                availabilityStatus = AvailabilityStatus.EMERGENCY_AVAILABLE.name,
                experienceYears = 5,
                bio = "Neonatal intensive care nurse experienced in preterm infant monitoring and family counseling.",
                approvalStatus = ApprovalStatus.APPROVED.name
            ),
            NurseEntity(
                fullName = "Md. Rafiqul Islam",
                bnmcNumber = "BNMC-22765",
                designation = "OT Nurse",
                qualification = "Post-Basic B.Sc in Nursing",
                specialty = "Surgery & OT",
                division = "Rajshahi",
                district = "Rajshahi",
                upazila = "Rajpara",
                hospitalName = "Rajshahi Medical College Hospital (RMCH)",
                phone = "01715883210",
                whatsapp = "01715883210",
                email = "rafiqul.rmch@nursesbd.org",
                bloodGroup = "O-",
                availabilityStatus = AvailabilityStatus.ON_DUTY.name,
                experienceYears = 12,
                bio = "Senior Operation Theatre scrub nurse assisting neuro and general surgical teams.",
                approvalStatus = ApprovalStatus.APPROVED.name
            ),
            NurseEntity(
                fullName = "Farhana Yasmin",
                bnmcNumber = "BNMC-35012",
                designation = "Nursing Supervisor",
                qualification = "M.Sc in Nursing",
                specialty = "Emergency & Trauma",
                division = "Khulna",
                district = "Khulna",
                upazila = "Sonadanga",
                hospitalName = "Khulna Medical College Hospital",
                phone = "01720554321",
                whatsapp = "01720554321",
                email = "farhana.kmch@nursesbd.org",
                bloodGroup = "A-",
                availabilityStatus = AvailabilityStatus.EMERGENCY_AVAILABLE.name,
                experienceYears = 10,
                bio = "Emergency triage supervisor coordinating trauma response across Khulna division.",
                approvalStatus = ApprovalStatus.APPROVED.name
            ),
            NurseEntity(
                fullName = "Anwara Begum",
                bnmcNumber = "BNMC-26541",
                designation = "Senior Staff Nurse",
                qualification = "Diploma in Nursing & Midwifery",
                specialty = "General Medicine",
                division = "Barishal",
                district = "Barishal",
                upazila = "Barishal Sadar",
                hospitalName = "Sher-e-Bangla Medical College Hospital (SBMCH)",
                phone = "01814667788",
                whatsapp = "01814667788",
                email = "anwara.sbmch@nursesbd.org",
                bloodGroup = "B+",
                availabilityStatus = AvailabilityStatus.ON_DUTY.name,
                experienceYears = 14,
                bio = "Senior medicine ward nurse mentoring junior staff and coordinating patient outreach.",
                approvalStatus = ApprovalStatus.APPROVED.name
            ),
            NurseEntity(
                fullName = "Sumona Roy",
                bnmcNumber = "BNMC-40129",
                designation = "ICU Specialist Nurse",
                qualification = "B.Sc in Nursing",
                specialty = "ICU & Critical Care",
                division = "Rangpur",
                district = "Rangpur",
                upazila = "Rangpur Sadar",
                hospitalName = "Rangpur Medical College Hospital",
                phone = "01733991122",
                whatsapp = "01733991122",
                email = "sumona.rpmch@nursesbd.org",
                bloodGroup = "O+",
                availabilityStatus = AvailabilityStatus.ON_DUTY.name,
                experienceYears = 6,
                bio = "Critical care nurse focused on sepsis protocol and respiratory support.",
                approvalStatus = ApprovalStatus.APPROVED.name
            ),
            NurseEntity(
                fullName = "Md. Sohel Rana",
                bnmcNumber = "BNMC-33918",
                designation = "Staff Nurse",
                qualification = "B.Sc in Nursing",
                specialty = "Orthopedics",
                division = "Mymensingh",
                district = "Mymensingh",
                upazila = "Mymensingh Sadar",
                hospitalName = "Mymensingh Medical College Hospital",
                phone = "01918445566",
                whatsapp = "01918445566",
                email = "sohel.mmch@nursesbd.org",
                bloodGroup = "B-",
                availabilityStatus = AvailabilityStatus.OFF_DUTY.name,
                experienceYears = 5,
                bio = "Orthopedic trauma and rehabilitation nurse serving Mymensingh division.",
                approvalStatus = ApprovalStatus.APPROVED.name
            ),
            NurseEntity(
                fullName = "Roksana Parvin",
                bnmcNumber = "BNMC-37811",
                designation = "Senior Staff Nurse",
                qualification = "Post-Basic B.Sc in Nursing",
                specialty = "Oncology & Palliative",
                division = "Dhaka",
                district = "Gazipur",
                upazila = "Gazipur Sadar",
                hospitalName = "Shaheed Tajuddin Ahmad Medical College Hospital",
                phone = "01755223344",
                whatsapp = "01755223344",
                email = "roksana.gazipur@nursesbd.org",
                bloodGroup = "AB-",
                availabilityStatus = AvailabilityStatus.EMERGENCY_AVAILABLE.name,
                experienceYears = 8,
                bio = "Oncology infusion and palliative care specialist supporting Gazipur patients.",
                approvalStatus = ApprovalStatus.APPROVED.name
            ),
            NurseEntity(
                fullName = "Papia Chakma",
                bnmcNumber = "BNMC-41290",
                designation = "Community Health Nurse",
                qualification = "Diploma in Nursing & Midwifery",
                specialty = "Maternal & Midwifery",
                division = "Chattogram",
                district = "Cox's Bazar",
                upazila = "Cox's Bazar Sadar",
                hospitalName = "Cox's Bazar 250 Bed District Sadar Hospital",
                phone = "01822334455",
                whatsapp = "01822334455",
                email = "papia.coxsbazar@nursesbd.org",
                bloodGroup = "A+",
                availabilityStatus = AvailabilityStatus.ON_DUTY.name,
                experienceYears = 6,
                bio = "Maternal care and immunization coordinator for coastal communities in Cox's Bazar.",
                approvalStatus = ApprovalStatus.APPROVED.name
            ),
            NurseEntity(
                fullName = "Jannatul Ferdous",
                bnmcNumber = "BNMC-44102",
                designation = "Staff Nurse",
                qualification = "B.Sc in Nursing",
                specialty = "Emergency & Trauma",
                division = "Dhaka",
                district = "Narayanganj",
                upazila = "Narayanganj Sadar",
                hospitalName = "Khanpur 300 Bed Hospital, Narayanganj",
                phone = "01925678901",
                whatsapp = "01925678901",
                email = "jannatul.nganj@nursesbd.org",
                bloodGroup = "O+",
                availabilityStatus = AvailabilityStatus.ON_DUTY.name,
                experienceYears = 4,
                bio = "Emergency department staff nurse covering industrial trauma triage in Narayanganj.",
                approvalStatus = ApprovalStatus.APPROVED.name
            ),
            NurseEntity(
                fullName = "Sadia Afrin Tania",
                bnmcNumber = "BNMC-47855",
                designation = "Staff Nurse",
                qualification = "B.Sc in Nursing",
                specialty = "ICU & Critical Care",
                division = "Dhaka",
                district = "Dhaka",
                upazila = "Shahbagh",
                hospitalName = "National Institute of Cardiovascular Diseases (NICVD)",
                phone = "01798112233",
                whatsapp = "01798112233",
                email = "sadia.nicvd@nursesbd.org",
                bloodGroup = "A+",
                availabilityStatus = AvailabilityStatus.EMERGENCY_AVAILABLE.name,
                experienceYears = 3,
                bio = "Applying to join the verified directory for cardiac ICU peer coordination.",
                approvalStatus = ApprovalStatus.PENDING.name
            ),
            NurseEntity(
                fullName = "Md. Tariqul Islam",
                bnmcNumber = "BNMC-46519",
                designation = "Nursing Officer",
                qualification = "Post-Basic B.Sc in Nursing",
                specialty = "Emergency & Trauma",
                division = "Rajshahi",
                district = "Bogura",
                upazila = "Bogura Sadar",
                hospitalName = "Shaheed Ziaur Rahman Medical College Hospital, Bogura",
                phone = "01764332211",
                whatsapp = "01764332211",
                email = "tariqul.bogura@nursesbd.org",
                bloodGroup = "B+",
                availabilityStatus = AvailabilityStatus.ON_DUTY.name,
                experienceYears = 6,
                bio = "Emergency nursing officer in Bogura seeking verified directory listing.",
                approvalStatus = ApprovalStatus.PENDING.name
            ),
            NurseEntity(
                fullName = "Mousumi Das",
                bnmcNumber = "BNMC-48104",
                designation = "Certified Midwife",
                qualification = "Diploma in Midwifery",
                specialty = "Maternal & Midwifery",
                division = "Sylhet",
                district = "Moulvibazar",
                upazila = "Moulvibazar Sadar",
                hospitalName = "Moulvibazar 250 Bed General Hospital",
                phone = "01876543210",
                whatsapp = "01876543210",
                email = "mousumi.mbazar@nursesbd.org",
                bloodGroup = "O+",
                availabilityStatus = AvailabilityStatus.ON_DUTY.name,
                experienceYears = 4,
                bio = "Tea-garden & rural maternal outreach midwife in Moulvibazar.",
                approvalStatus = ApprovalStatus.PENDING.name
            )
        )
    }
}
