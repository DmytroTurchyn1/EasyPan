package com.cook.easypan.easypan.data.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.cook.easypan.R
import com.cook.easypan.easypan.domain.SignInResult
import com.cook.easypan.easypan.domain.UserData
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth

    /**
     * Initiates the Google One Tap sign-in flow and returns an IntentSender for launching the sign-in UI.
     *
     * @return An IntentSender to start the Google sign-in UI, or null if the sign-in initiation fails.
     */
    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    /**
     * Processes the result of a Google One Tap sign-in intent and attempts to authenticate the user with Firebase.
     *
     * Extracts the Google ID token from the provided intent, exchanges it for Firebase credentials, and signs in the user asynchronously.
     *
     * @param intent The intent returned from the Google One Tap sign-in flow.
     * @return A [SignInResult] containing user data if sign-in succeeds, or an error message if it fails.
     */
    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    /**
     * Signs out the current user from both Google One Tap and Firebase Authentication.
     *
     * This operation is performed asynchronously and handles exceptions, ensuring coroutine cancellations are propagated.
     */
    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    /**
     * Returns the currently signed-in user's data, or null if no user is signed in.
     *
     * @return A [UserData] object containing the user's ID, display name, and profile picture URL, or null if not signed in.
     */
    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }

    /**
     * Creates a `BeginSignInRequest` configured for Google One Tap sign-in with ID token support.
     *
     * The request is set to filter by authorized accounts, use the server client ID from resources, and enable auto-selection.
     *
     * @return A configured `BeginSignInRequest` for initiating the Google sign-in flow.
     */
    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}