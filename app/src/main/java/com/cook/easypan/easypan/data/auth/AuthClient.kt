/*
 * Created  18/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.cook.easypan.BuildConfig
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.domain.Result
import com.cook.easypan.easypan.data.analytics.AnalyticsClient
import com.cook.easypan.easypan.domain.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class AuthClient(
    private val applicationContext: Context,
    private val auth: FirebaseAuth,
    private val analytics: AnalyticsClient
) {

    companion object {
        private const val TAG = "AuthClient"
    }

    private val clientId = BuildConfig.CLIENT_ID
    private val credentialManager by lazy { CredentialManager.create(applicationContext) }

    fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it ->
            str + "%02x".format(it)
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): Result {
        return try {
            val credential = getGoogleCredential(activityContext)
                ?: return Result.Failure(AppError.AUTH_FAILED)
            auth.signInWithCredential(credential).await()
            analytics.setUserId(auth.currentUser?.uid)
            Result.Success
        } catch (e: GetCredentialCancellationException) {
            Result.Failure(AppError.SIGN_IN_CANCELLED)
        } catch (e: NoCredentialException) {
            Result.Failure(AppError.NO_GOOGLE_ACCOUNT)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential request failed", e)
            Result.Failure(AppError.AUTH_FAILED)
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Failed to parse Google ID token", e)
            Result.Failure(AppError.AUTH_FAILED)
        } catch (e: FirebaseNetworkException) {
            Result.Failure(AppError.NETWORK)
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Firebase sign-in failed", e)
            Result.Failure(AppError.AUTH_FAILED)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected sign-in failure", e)
            Result.Failure(AppError.UNKNOWN)
        }
    }

    suspend fun deleteAccount(activityContext: Context): Result {
        val user = auth.currentUser ?: return Result.Failure(AppError.NOT_SIGNED_IN)
        return try {
            try {
                user.delete().await()
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                val credential = getGoogleCredential(activityContext)
                    ?: return Result.Failure(AppError.REAUTH_REQUIRED)
                user.reauthenticate(credential).await()
                user.delete().await()
            }
            clearCredentialState()
            analytics.reset()
            Result.Success
        } catch (e: GetCredentialCancellationException) {
            Result.Failure(AppError.SIGN_IN_CANCELLED)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Re-authentication failed", e)
            Result.Failure(AppError.REAUTH_REQUIRED)
        } catch (e: FirebaseNetworkException) {
            Result.Failure(AppError.NETWORK)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Account deletion failed", e)
            Result.Failure(AppError.UNKNOWN)
        }
    }

    suspend fun signOut() {
        auth.signOut()
        clearCredentialState()
        analytics.reset()
    }

    fun getSignedInUser(): User? = auth.currentUser?.run {
        User(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString(),
            email = this.email
        )
    }

    private suspend fun getGoogleCredential(activityContext: Context): AuthCredential? {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
            .setNonce(createNonce())
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val result = credentialManager.getCredential(
            context = activityContext,
            request = request
        )
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        }
        Log.e(TAG, "Unexpected credential type: ${credential.type}")
        return null
    }

    private suspend fun clearCredentialState() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: ClearCredentialException) {
            Log.e(TAG, "Failed to clear credential state", e)
        }
    }
}
