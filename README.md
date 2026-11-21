# Nurses BD — Bangladesh Nurses E-Directory

A Material 3 Jetpack Compose Android app connecting nurses across all 8 divisions of Bangladesh, backed by **Cloud Firestore**, **Google Sign-In (Credential Manager)**, and role-based **Admin Analytics & Approval Management**.

## Key Features
- **Authenticated Access:** Sign in with Google before viewing or searching the directory.
- **Bangladesh Nurses Directory:** Search and filter approved nurses by name, BNMC number, division, district, hospital, specialty, blood group, and emergency availability, with quick Call / WhatsApp / Email actions.
- **Nurse Registration Workflow:** Validated registration form saving profiles to Cloud Firestore with `PENDING` approval status.
- **Admin Dashboard & Analytics:** KPI summary cards, division distribution, specialty breakdown, approval queue (`Approve` / `Reject` / `Delete`), and Super Admin management to add or remove admins.

## Download APK via GitHub Actions / Releases
This is a native Android Kotlin app (not a web app, so GitHub Pages does not apply). Every push to `main` triggers **GitHub Actions** (`.github/workflows/android-release.yml`) to:
1. Run unit tests (`gradle :app:testDebugUnitTest`)
2. Build `NursesBD-debug.apk` signed with the project debug keystore (matching `app/google-services.json` SHA-1)
3. Publish the downloadable APK under **Releases** (`latest-apk`) and **Actions** artifacts.

## Local Build
Requires JDK 17+, Android SDK (API 36), and Gradle 9.3.1:
```bash
base64 -d debug.keystore.base64 > debug.keystore
gradle :app:testDebugUnitTest :app:assembleDebug
```
