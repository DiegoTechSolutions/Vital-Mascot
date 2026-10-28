package com.vcm.adoptamascota.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(context: Context) {

    private val appContext = context.applicationContext
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(appContext)

    // Emite el usuario actual cada vez que cambia la sesión (null = sin sesión)
    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // Inicia sesión con Google; requiere un Context de Activity para mostrar el selector de cuentas
    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
        return try {
            // El Client ID web lo genera el plugin google-services como recurso string
            val webClientIdRes = appContext.resources
                .getIdentifier("default_web_client_id", "string", appContext.packageName)
            if (webClientIdRes == 0) {
                return Result.failure(
                    IllegalStateException(
                        "Falta el cliente OAuth en google-services.json. Activa Google en Firebase Authentication, " +
                            "agrega el SHA-1/SHA-256 y descarga el archivo de nuevo."
                    )
                )
            }
            val webClientId = appContext.getString(webClientIdRes)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credential = credentialManager.getCredential(activityContext, request).credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return Result.failure(IllegalStateException("Tipo de credencial no soportado"))
            }

            // Cambia el token de Google por una sesión de Firebase
            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val user = auth.signInWithCredential(firebaseCredential).await().user
                ?: return Result.failure(IllegalStateException("Firebase no devolvió usuario"))
            Result.success(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cierra la sesión de Firebase y limpia el estado de credenciales del dispositivo
    suspend fun signOut() {
        auth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // La sesión de Firebase ya está cerrada; solo falló la limpieza local
        }
    }
}
