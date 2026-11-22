const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const BOOTSTRAP_UID = "admin_bootstrap_uid";
const BOOTSTRAP_EMAIL = "mdnasirnaznin@gmail.com";
const SECOND_ADMIN_UID = "second_admin_uid";
const SECOND_ADMIN_EMAIL = "dhaka.admin@nursesbd.org";
const ALICE_UID = "nurse_alice_uid";
const ALICE_EMAIL = "alice.nurse@gmail.com";
const BOB_UID = "nurse_bob_uid";
const BOB_EMAIL = "bob.nurse@gmail.com";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validNurseCreatePayload(uid, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    submittedByUid: uid,
    fullName: "Nusrat Jahan Mim",
    bnmcNumber: "BNMC-48920",
    designation: "Staff Nurse",
    qualification: "B.Sc in Nursing",
    specialty: "ICU & Critical Care",
    division: "Dhaka",
    district: "Dhaka",
    upazila: "Shahbagh",
    hospitalName: "Dhaka Medical College Hospital",
    phone: "01711450218",
    whatsapp: "01711450218",
    email: "nusrat@hospital.org",
    bloodGroup: "O+",
    availabilityStatus: "ON_DUTY",
    experienceYears: 4,
    bio: "Critical care nurse.",
    approvalStatus: "PENDING",
    rejectionReason: "",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validAdminPayload(email, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    fullName: "Division Admin Dhaka",
    email,
    role: "MODERATOR",
    assignedDivision: "Dhaka",
    addedBy: BOOTSTRAP_EMAIL,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write nurses or admins", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("nurses")
      .doc("nurse_1")
      .set(validNurseCreatePayload(ALICE_UID, { approvalStatus: "APPROVED" }));
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("nurses").where("approvalStatus", "==", "APPROVED").get()
  );
  await assertFails(unauthDb.collection("nurses").doc("nurse_1").get());
  await assertFails(
    unauthDb.collection("nurses").doc("nurse_2").set(validNurseCreatePayload(ALICE_UID))
  );
  await assertFails(unauthDb.collection("admins").doc(BOOTSTRAP_EMAIL).get());
});

test("Nurse registration enforces PENDING status, UID ownership, and rejects @DocumentId or ghost fields", async () => {
  const aliceDb = testEnv
    .authenticatedContext(ALICE_UID, { email: ALICE_EMAIL, email_verified: true })
    .firestore();

  // Valid registration succeeds (no `id` field, optional reviewedBy/reviewedAt omitted)
  await assertSucceeds(
    aliceDb.collection("nurses").doc("nurse_alice_1").set(validNurseCreatePayload(ALICE_UID))
  );

  // Self-approval privilege escalation fails
  await assertFails(
    aliceDb
      .collection("nurses")
      .doc("nurse_alice_evil")
      .set(validNurseCreatePayload(ALICE_UID, { approvalStatus: "APPROVED" }))
  );

  // Spoofed submittedByUid fails
  await assertFails(
    aliceDb
      .collection("nurses")
      .doc("nurse_alice_spoof")
      .set(validNurseCreatePayload(BOB_UID))
  );

  // `@DocumentId` `id` field in payload fails
  await assertFails(
    aliceDb
      .collection("nurses")
      .doc("nurse_alice_id")
      .set(validNurseCreatePayload(ALICE_UID, { id: "nurse_alice_id" }))
  );

  // Shadow update ghost field fails
  await assertFails(
    aliceDb
      .collection("nurses")
      .doc("nurse_alice_ghost")
      .set(validNurseCreatePayload(ALICE_UID, { isVerified: true }))
  );

  // Future timestamp fails
  await assertFails(
    aliceDb
      .collection("nurses")
      .doc("nurse_alice_future")
      .set(
        validNurseCreatePayload(ALICE_UID, {
          createdAt: new Date(Date.now() + 60000),
          updatedAt: new Date(Date.now() + 60000),
        })
      )
  );

  // Oversized upazila (>50 chars) fails
  await assertFails(
    aliceDb
      .collection("nurses")
      .doc("nurse_alice_bad_upazila")
      .set(
        validNurseCreatePayload(ALICE_UID, {
          upazila: "U".repeat(55),
        })
      )
  );
});

test("PII & Query Trust: non-admin can query APPROVED or own submissions, never other PENDING docs or unfiltered list", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db
      .collection("nurses")
      .doc("approved_1")
      .set(validNurseCreatePayload(BOB_UID, { approvalStatus: "APPROVED" }));
    await db
      .collection("nurses")
      .doc("pending_bob")
      .set(validNurseCreatePayload(BOB_UID, { approvalStatus: "PENDING" }));
    await db
      .collection("nurses")
      .doc("pending_alice")
      .set(validNurseCreatePayload(ALICE_UID, { approvalStatus: "PENDING" }));
  });

  const aliceDb = testEnv
    .authenticatedContext(ALICE_UID, { email: ALICE_EMAIL, email_verified: true })
    .firestore();

  // Filtered directory query succeeds
  await assertSucceeds(
    aliceDb.collection("nurses").where("approvalStatus", "==", "APPROVED").get()
  );

  // Own submissions query succeeds
  await assertSucceeds(
    aliceDb.collection("nurses").where("submittedByUid", "==", ALICE_UID).get()
  );

  // Unfiltered collection list fails for non-admin
  await assertFails(aliceDb.collection("nurses").get());

  // Cross-user PENDING document get fails
  await assertFails(aliceDb.collection("nurses").doc("pending_bob").get());

  // Own PENDING document get succeeds
  await assertSucceeds(aliceDb.collection("nurses").doc("pending_alice").get());
});

test("Update rules: owner can update safe profile fields only; cannot self-approve or touch createdAt", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("nurses")
      .doc("alice_app")
      .set(validNurseCreatePayload(ALICE_UID));
  });

  const aliceDb = testEnv
    .authenticatedContext(ALICE_UID, { email: ALICE_EMAIL, email_verified: true })
    .firestore();

  await assertSucceeds(
    aliceDb.collection("nurses").doc("alice_app").update({
      availabilityStatus: "EMERGENCY_AVAILABLE",
      phone: "01711450219",
      bio: "Updated bio note.",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Owner cannot directly change hospitalName/upazila (must use transfer request)
  await assertFails(
    aliceDb.collection("nurses").doc("alice_app").update({
      upazila: "Dhanmondi",
      hospitalName: "BSMMU Dhaka",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Owner cannot self-approve
  await assertFails(
    aliceDb.collection("nurses").doc("alice_app").update({
      approvalStatus: "APPROVED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Owner cannot mutate immutable createdAt
  await assertFails(
    aliceDb.collection("nurses").doc("alice_app").update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Bootstrap admin & Email Spoofing protection: verified bootstrap admin can manage admins and approve nurses; unverified email fails", async () => {
  const spoofedAdminDb = testEnv
    .authenticatedContext("spoof_uid", { email: BOOTSTRAP_EMAIL, email_verified: false })
    .firestore();

  // Spoofed email_verified: false MUST fail
  await assertFails(
    spoofedAdminDb
      .collection("admins")
      .doc(BOOTSTRAP_EMAIL)
      .set(
        validAdminPayload(BOOTSTRAP_EMAIL, {
          fullName: "Chief Directory Admin",
          role: "SUPER_ADMIN",
          assignedDivision: "All Bangladesh",
          addedBy: "System Bootstrap",
        })
      )
  );
  await assertFails(spoofedAdminDb.collection("nurses").get());

  const adminDb = testEnv
    .authenticatedContext(BOOTSTRAP_UID, { email: BOOTSTRAP_EMAIL, email_verified: true })
    .firestore();

  // Bootstrap admin creates own SUPER_ADMIN doc
  await assertSucceeds(
    adminDb
      .collection("admins")
      .doc(BOOTSTRAP_EMAIL)
      .set(
        validAdminPayload(BOOTSTRAP_EMAIL, {
          fullName: "Chief Directory Admin",
          role: "SUPER_ADMIN",
          assignedDivision: "All Bangladesh",
          addedBy: "System Bootstrap",
        })
      )
  );

  // Super Admin adds a second MODERATOR admin
  await assertSucceeds(
    adminDb
      .collection("admins")
      .doc(SECOND_ADMIN_EMAIL)
      .set(validAdminPayload(SECOND_ADMIN_EMAIL))
  );

  // Admin can list all nurses (pending + approved) and approve a nurse
  const aliceDb = testEnv
    .authenticatedContext(ALICE_UID, { email: ALICE_EMAIL, email_verified: true })
    .firestore();
  await assertSucceeds(
    aliceDb.collection("nurses").doc("pending_n1").set(validNurseCreatePayload(ALICE_UID))
  );

  await assertSucceeds(adminDb.collection("nurses").get());
  await assertSucceeds(
    adminDb.collection("nurses").doc("pending_n1").update({
      approvalStatus: "APPROVED",
      rejectionReason: "",
      reviewedBy: BOOTSTRAP_EMAIL,
      reviewedAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Admin can edit an approved nurse's profile fields and delete if needed
  await assertSucceeds(
    adminDb.collection("nurses").doc("pending_n1").update({
      fullName: "Nusrat Jahan Updated",
      hospitalName: "Square Hospital Dhaka",
      division: "Dhaka",
      district: "Dhaka",
      upazila: "Dhanmondi",
      designation: "Senior Staff Nurse",
      phone: "01711998877",
      whatsapp: "01711998877",
      reviewedBy: BOOTSTRAP_EMAIL,
      reviewedAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Second admin (MODERATOR) can approve/list nurses, but CANNOT add or remove admins
  const modDb = testEnv
    .authenticatedContext(SECOND_ADMIN_UID, {
      email: SECOND_ADMIN_EMAIL,
      email_verified: true,
    })
    .firestore();
  await assertSucceeds(modDb.collection("nurses").get());
  await assertFails(
    modDb
      .collection("admins")
      .doc("rogue@gmail.com")
      .set(validAdminPayload("rogue@gmail.com"))
  );
  await assertFails(modDb.collection("admins").doc(BOOTSTRAP_EMAIL).delete());

  // Super Admin can remove moderator, but cannot remove self/bootstrap
  await assertFails(adminDb.collection("admins").doc(BOOTSTRAP_EMAIL).delete());
  await assertSucceeds(adminDb.collection("admins").doc(SECOND_ADMIN_EMAIL).delete());

  // Non-admin cannot read another admin's doc or list admins
  await assertFails(aliceDb.collection("admins").get());
  await assertFails(aliceDb.collection("admins").doc(BOOTSTRAP_EMAIL).get());
});

test("Hospital transfer requests: approved nurse owner can submit PENDING transfer; admin approves & updates nurse atomically", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db
      .collection("nurses")
      .doc("alice_approved")
      .set(validNurseCreatePayload(ALICE_UID, { approvalStatus: "APPROVED" }));
    await db
      .collection("nurses")
      .doc("alice_pending")
      .set(validNurseCreatePayload(ALICE_UID, { approvalStatus: "PENDING" }));
    await db
      .collection("nurses")
      .doc("bob_approved")
      .set(validNurseCreatePayload(BOB_UID, { approvalStatus: "APPROVED" }));
  });

  const aliceDb = testEnv
    .authenticatedContext(ALICE_UID, { email: ALICE_EMAIL, email_verified: true })
    .firestore();
  const bobDb = testEnv
    .authenticatedContext(BOB_UID, { email: BOB_EMAIL, email_verified: true })
    .firestore();
  const adminDb = testEnv
    .authenticatedContext(BOOTSTRAP_UID, { email: BOOTSTRAP_EMAIL, email_verified: true })
    .firestore();

  const now = new Date(Date.now() - 1000);
  const transferPayload = {
    nurseId: "alice_approved",
    nurseName: "Nusrat Jahan Mim",
    bnmcNumber: "BNMC-48920",
    requestedByUid: ALICE_UID,
    currentHospitalName: "Dhaka Medical College Hospital",
    currentDivision: "Dhaka",
    currentDistrict: "Dhaka",
    currentUpazila: "Shahbagh",
    newHospitalName: "Chattogram Medical College Hospital",
    newDivision: "Chattogram",
    newDistrict: "Chattogram",
    newUpazila: "Panchlaish",
    transferNote: "Transferred via DGHS order #412",
    status: "PENDING",
    rejectionReason: "",
    createdAt: now,
    updatedAt: now,
  };

  // Alice submits valid transfer request for her own APPROVED nurse profile
  await assertSucceeds(
    aliceDb.collection("transfer_requests").doc("tr_1").set(transferPayload)
  );

  // Alice queries her own transfer requests
  await assertSucceeds(
    aliceDb.collection("transfer_requests").where("requestedByUid", "==", ALICE_UID).get()
  );

  // Alice cannot submit transfer for PENDING profile or Bob's profile
  await assertFails(
    aliceDb
      .collection("transfer_requests")
      .doc("tr_pending_fail")
      .set({ ...transferPayload, nurseId: "alice_pending" })
  );
  await assertFails(
    aliceDb
      .collection("transfer_requests")
      .doc("tr_bob_fail")
      .set({ ...transferPayload, nurseId: "bob_approved" })
  );

  // Bob cannot read or list all transfer requests, nor self-approve
  await assertFails(bobDb.collection("transfer_requests").doc("tr_1").get());
  await assertFails(bobDb.collection("transfer_requests").get());
  await assertFails(
    aliceDb.collection("transfer_requests").doc("tr_1").update({
      status: "APPROVED",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Admin can list all transfer requests and atomically update nurse + approve transfer
  await assertSucceeds(adminDb.collection("transfer_requests").get());
  const batch = adminDb.batch();
  batch.update(adminDb.collection("nurses").doc("alice_approved"), {
    division: "Chattogram",
    district: "Chattogram",
    upazila: "Panchlaish",
    hospitalName: "Chattogram Medical College Hospital",
    reviewedBy: BOOTSTRAP_EMAIL,
    reviewedAt: new Date(Date.now() - 500),
    updatedAt: new Date(Date.now() - 500),
  });
  batch.update(adminDb.collection("transfer_requests").doc("tr_1"), {
    status: "APPROVED",
    rejectionReason: "",
    reviewedBy: BOOTSTRAP_EMAIL,
    reviewedAt: new Date(Date.now() - 500),
    updatedAt: new Date(Date.now() - 500),
  });
  await assertSucceeds(batch.commit());
});

test("Live remote notice: signed-in users can read app_config/live_notice; only admins can publish", async () => {
  const aliceDb = testEnv
    .authenticatedContext(ALICE_UID, { email: ALICE_EMAIL, email_verified: true })
    .firestore();
  const adminDb = testEnv
    .authenticatedContext(BOOTSTRAP_UID, { email: BOOTSTRAP_EMAIL, email_verified: true })
    .firestore();

  const now = new Date(Date.now() - 1000);
  await assertFails(
    aliceDb.collection("app_config").doc("live_notice").set({
      title: "Fake Notice",
      message: "Unauthorized",
      active: true,
      updatedBy: ALICE_EMAIL,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    adminDb.collection("app_config").doc("live_notice").set({
      title: "Directory Update",
      message: "New hospital transfer requests are now open.",
      active: true,
      updatedBy: BOOTSTRAP_EMAIL,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(aliceDb.collection("app_config").doc("live_notice").get());
});

test("Categories, parent names, multi-degree educations, Non-Govt optional workplace, and Non-Govt to Govt transfer", async () => {
  const aliceDb = testEnv
    .authenticatedContext(ALICE_UID, { email: ALICE_EMAIL, email_verified: true })
    .firestore();
  const adminDb = testEnv
    .authenticatedContext(BOOTSTRAP_UID, { email: BOOTSTRAP_EMAIL, email_verified: true })
    .firestore();

  // Non-Govt nurse with blank hospitalName succeeds when address is provided
  await assertSucceeds(
    aliceDb.collection("nurses").doc("non_govt_1").set(
      validNurseCreatePayload(ALICE_UID, {
        fatherName: "Md. Abdul Karim",
        motherName: "Begum Rokeya",
        nurseCategory: "NON_GOVT",
        govtUid: "",
        hospitalName: "",
        address: "House 14, Road 6, Mirpur-10, Dhaka",
        educations: [
          {
            courseName: "BSc in Nursing (Basic)",
            major: "Medical-Surgical",
            university: "University of Dhaka",
            passedYear: "2022",
          },
          {
            courseName: "MSN",
            major: "Critical Care",
            university: "BSMMU",
            passedYear: "2024",
          },
        ],
        qualification: "BSc in Nursing (Basic) — Medical-Surgical (University of Dhaka, 2022)",
      })
    )
  );

  // Non-Govt nurse with BOTH blank hospitalName and blank address fails
  await assertFails(
    aliceDb.collection("nurses").doc("non_govt_invalid").set(
      validNurseCreatePayload(ALICE_UID, {
        nurseCategory: "NON_GOVT",
        hospitalName: "",
        address: "",
      })
    )
  );

  // Student nurse with blank educational institute (hospitalName) fails
  await assertFails(
    aliceDb.collection("nurses").doc("student_invalid").set(
      validNurseCreatePayload(ALICE_UID, {
        nurseCategory: "STUDENT",
        designation: "Student Nurse",
        hospitalName: "",
        address: "Mirpur, Dhaka",
      })
    )
  );

  // Student nurse with Educational Institute & Student Registration succeeds
  await assertSucceeds(
    aliceDb.collection("nurses").doc("student_valid").set(
      validNurseCreatePayload(ALICE_UID, {
        nurseCategory: "STUDENT",
        designation: "Student Nurse",
        bnmcNumber: "STU-2025-881",
        hospitalName: "Dhaka Nursing College",
        address: "Bakshibazar, Dhaka",
      })
    )
  );

  // Approve non_govt_1 as Admin
  await assertSucceeds(
    adminDb.collection("nurses").doc("non_govt_1").update({
      approvalStatus: "APPROVED",
      reviewedBy: BOOTSTRAP_EMAIL,
      reviewedAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Non-Govt nurse applies for Non-Govt -> Govt transfer with mandatory newHospitalName & Govt UID
  const now = new Date(Date.now() - 1000);
  await assertSucceeds(
    aliceDb.collection("transfer_requests").doc("tr_govt_join").set({
      nurseId: "non_govt_1",
      nurseName: "Nusrat Jahan Mim",
      bnmcNumber: "BNMC-48920",
      requestedByUid: ALICE_UID,
      currentCategory: "NON_GOVT",
      newCategory: "GOVT",
      newGovtUid: "DGHS-994120",
      newDesignation: "Senior Staff Nurse",
      currentHospitalName: "",
      currentDivision: "Dhaka",
      currentDistrict: "Dhaka",
      currentUpazila: "Mirpur",
      newHospitalName: "Dhaka Medical College Hospital",
      newDivision: "Dhaka",
      newDistrict: "Dhaka",
      newUpazila: "Shahbagh",
      transferNote: "Joined Govt service via PSC batch",
      status: "PENDING",
      rejectionReason: "",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Admin atomically approves Non-Govt -> Govt transfer
  const batch = adminDb.batch();
  batch.update(adminDb.collection("nurses").doc("non_govt_1"), {
    nurseCategory: "GOVT",
    govtUid: "DGHS-994120",
    designation: "Senior Staff Nurse",
    division: "Dhaka",
    district: "Dhaka",
    upazila: "Shahbagh",
    hospitalName: "Dhaka Medical College Hospital",
    reviewedBy: BOOTSTRAP_EMAIL,
    reviewedAt: new Date(Date.now() - 500),
    updatedAt: new Date(Date.now() - 500),
  });
  batch.update(adminDb.collection("transfer_requests").doc("tr_govt_join"), {
    status: "APPROVED",
    rejectionReason: "",
    reviewedBy: BOOTSTRAP_EMAIL,
    reviewedAt: new Date(Date.now() - 500),
    updatedAt: new Date(Date.now() - 500),
  });
  await assertSucceeds(batch.commit());
});

test("Live directory_config and app_release: admin writes & signed-in reads succeed; non-admin & http URLs fail", async () => {
  const aliceDb = testEnv
    .authenticatedContext(ALICE_UID, { email: ALICE_EMAIL, email_verified: true })
    .firestore();
  const adminDb = testEnv
    .authenticatedContext(BOOTSTRAP_UID, { email: BOOTSTRAP_EMAIL, email_verified: true })
    .firestore();

  const now = new Date(Date.now() - 1000);

  // Non-admin cannot create directory_config or app_release
  await assertFails(
    aliceDb.collection("app_config").doc("directory_config").set({
      extraDesignations: ["Matron"],
      extraCourseNames: ["Post Basic Oncology"],
      extraSpecialties: ["Dialysis"],
      updatedBy: ALICE_EMAIL,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    aliceDb.collection("app_config").doc("app_release").set({
      latestVersionCode: 5,
      latestVersionName: "1.4",
      releaseNotes: "Rogue update",
      apkDownloadUrl: "https://example.com/app.apk",
      webPortalUrl: "",
      active: true,
      updatedBy: ALICE_EMAIL,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Admin rejects insecure http:// URL
  await assertFails(
    adminDb.collection("app_config").doc("app_release").set({
      latestVersionCode: 5,
      latestVersionName: "1.4",
      releaseNotes: "Insecure URL",
      apkDownloadUrl: "http://example.com/app.apk",
      webPortalUrl: "",
      active: true,
      updatedBy: BOOTSTRAP_EMAIL,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Admin creates directory_config & app_release validly
  await assertSucceeds(
    adminDb.collection("app_config").doc("directory_config").set({
      extraDesignations: ["Matron", "Nursing Superintendent"],
      extraCourseNames: ["Post Basic ICU Course"],
      extraSpecialties: ["Burn & Plastic"],
      updatedBy: BOOTSTRAP_EMAIL,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("app_config").doc("app_release").set({
      latestVersionCode: 4,
      latestVersionName: "1.3",
      releaseNotes: "Live dynamic form options and update alerts.",
      apkDownloadUrl: "https://github.com/example/nursesbd/releases/tag/latest-apk",
      webPortalUrl: "https://example.github.io/nursesbd/",
      active: true,
      updatedBy: BOOTSTRAP_EMAIL,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Admin partial update preserves createdAt
  await assertSucceeds(
    adminDb.collection("app_config").doc("directory_config").update({
      extraDesignations: ["Matron", "Deputy Nursing Superintendent"],
      extraCourseNames: ["Post Basic ICU Course"],
      extraSpecialties: ["Burn & Plastic"],
      updatedBy: BOOTSTRAP_EMAIL,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Signed-in nurse can read both documents live
  await assertSucceeds(aliceDb.collection("app_config").doc("directory_config").get());
  await assertSucceeds(aliceDb.collection("app_config").doc("app_release").get());
});
