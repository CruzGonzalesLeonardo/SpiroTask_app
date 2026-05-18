package com.example.spire_task.data.remote.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleSignInManager(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.example.spire_task.R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun handleSignInResult(data: Intent?): Result<GoogleSignInAccount> {
        return try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            Result.success(account)
        } catch (e: ApiException) {
            Result.failure(e)
        }
    }

    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Result<com.google.firebase.auth.AuthResult> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            Result.success(authResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentFirebaseUser() = auth.currentUser

    // ✅ ACTUALIZAR ESTA FUNCIÓN - Cierra sesión completa
    suspend fun signOut() {
        // Cierra sesión en Firebase
        auth.signOut()
        // Cierra sesión en Google (elimina la cuenta seleccionada)
        googleSignInClient.signOut().await()
        // También puedes usar revokeAccess() para revocar permisos (más agresivo)
        // googleSignInClient.revokeAccess().await()
    }

    // Opcional: Revocar acceso (elimina los permisos otorgados)
    suspend fun revokeAccess() {
        googleSignInClient.revokeAccess().await()
        auth.signOut()
    }
}