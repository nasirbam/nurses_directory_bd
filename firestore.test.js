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
