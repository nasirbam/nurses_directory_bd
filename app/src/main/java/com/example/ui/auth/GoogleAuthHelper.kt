package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Suppress("DEPRECATION")
internal fun getAppSigningSha1(context: Context): String? {
    return runCatching {
        val pkgManager = context.packageManager
        val certBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = pkgManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            info.signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()
        } else {
            val info = pkgManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            info.signatures?.firstOrNull()?.toByteArray()
        } ?: return null

        val digest = MessageDigest.getInstance("SHA-1").digest(certBytes)
        digest.joinToString(":") { byte -> "%02X".format(byte) }
    }.getOrNull()
}

private fun isFirebaseAvailable(context: Context): Boolean {
    return runCatching {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        FirebaseApp.getApps(context).isNotEmpty()
    }.getOrDefault(false)
}

private fun resolveDefaultWebClientId(context: Context): String? {
    return runCatching { context.getString(R.string.default_web_client_id) }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (!isFirebaseAvailable(context)) {
        onUnauthenticated()
        return
    }
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = resolveDefaultWebClientId(context)
    if (clientId == null) {
        onUnauthenticated()
        return
    }
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val cred = result.credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: (String) -> Unit = {}
) {
    val activity = context.findActivity()
    if (activity == null) {
        onAuthError("Activity context is required for Google Sign-In.")
        return
    }
    if (!isFirebaseAvailable(context)) {
        Log.e("Auth", "Server configuration unavailable in this build.")
        onAuthError("অ্যাপের নিরাপত্তা সংযোগ প্রস্তুত নয়। অনুগ্রহ করে অফিসিয়াল সংস্করণ ব্যবহার করুন।")
        return
    }
    val clientId = resolveDefaultWebClientId(context)
    if (clientId == null) {
        Log.e("Auth", "Google Sign-In client ID missing in build resources.")
        onAuthError("সাইন-ইন সেবা সাময়িকভাবে অনুপলব্ধ। অনুগ্রহ করে কিছুক্ষণ পর আবার চেষ্টা করুন।")
        return
    }
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()
    scope.launch {
        val installedSha1 = getAppSigningSha1(context)
        try {
            val result = credentialManager.getCredential(activity, request)
            val cred = result.credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onAuthError("গুগল অ্যাকাউন্ট যাচাই করা যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন।")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(
                "Auth",
                "Google Sign-In cancelled/rejected. sha1=$installedSha1 msg=${e.errorMessage ?: e.message}",
                e
            )
            val detail = e.errorMessage?.toString()?.trim().orEmpty()
            onAuthCancelled(
                "গুগল সাইন-ইন সম্পন্ন হয়নি। ফোনে ইন্টারনেট সংযোগ, Google অ্যাকাউন্ট ও স্বয়ংক্রিয় সময় (Auto date & time) পরীক্ষা করে আবার চেষ্টা করুন।" +
                    if (detail.isNotBlank()) "\n($detail)" else ""
            )
        } catch (e: NoCredentialException) {
            Log.w("Auth", "No Google credentials available: ${e.message}", e)
            onAuthError("এই ফোনে কোনো গুগল অ্যাকাউন্ট পাওয়া যায়নি। Settings থেকে একটি Google অ্যাকাউন্ট যুক্ত করে আবার চেষ্টা করুন।")
        } catch (e: GetCredentialException) {
            Log.e("Auth", "Credential Manager error (${e.type}) sha1=$installedSha1: ${e.errorMessage}", e)
            val detail = e.errorMessage?.toString()?.trim().orEmpty()
            onAuthError(
                "সাইন-ইন করতে সমস্যা হয়েছে। Google Play Services ও ইন্টারনেট সংযোগ পরীক্ষা করে আবার চেষ্টা করুন।" +
                    if (detail.isNotBlank()) "\n($detail)" else ""
            )
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError("সাইন-ইন সম্পন্ন হয়নি। অনুগ্রহ করে পুনরায় চেষ্টা করুন।")
        }
    }
}

fun signOut(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state: ${e.message}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}

@Composable
fun GoogleSignInButton(
    label: String = "Sign in with Google",
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                isLoading = true
                onGoogleSignInClicked(
                    context = context,
                    credentialManager = credentialManager,
                    onAuthSuccess = {
                        isLoading = false
                        onAuthSuccess()
                    },
                    onAuthError = { msg ->
                        isLoading = false
                        onAuthError(msg)
                    },
                    scope = scope,
                    onAuthCancelled = { cancelMsg ->
                        isLoading = false
                        onAuthError(cancelMsg)
                    }
                )
            },
            enabled = !isLoading,
            colors = colors,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
