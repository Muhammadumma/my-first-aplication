package com.example.data.network

import android.content.Context
import android.util.Log
import com.example.data.StudentUserEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseServerAuthService {
    companion object {
        private const val TAG = "FirebaseServerAuth"
        private const val STUDENTS_COLLECTION = "jsp_students"

        fun initializeIfPossible(context: Context) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Firebase initialization notice: ${e.message}")
            }
        }
    }

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseAuth instance retrieval failed: ${e.message}")
            null
        }

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseFirestore instance retrieval failed: ${e.message}")
            null
        }

    suspend fun registerOnServer(
        matricNumber: String,
        fullName: String,
        email: String,
        department: String,
        level: String,
        session: String,
        password: String
    ): Result<StudentUserEntity> = withContext(Dispatchers.IO) {
        try {
            val currentAuth = auth ?: throw IllegalStateException("Firebase Authentication service is currently unavailable. Please verify connection or configuration.")
            
            Log.d(TAG, "Initiating Firebase server registration for: $email, Matric: $matricNumber")

            // 1. Create User in Firebase Auth Server
            val authResult: AuthResult = awaitTask(currentAuth.createUserWithEmailAndPassword(email, password))
            val firebaseUser: FirebaseUser = authResult.user
                ?: throw IllegalStateException("Firebase server failed to create user instance.")

            // 2. Update Display Name on Firebase Profile
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                awaitTask(firebaseUser.updateProfile(profileUpdates))
            } catch (e: Throwable) {
                Log.w(TAG, "Profile display name update warning: ${e.message}")
            }

            val randId = (1000..9999).random()
            val clearancePin = "JSP-CLR-$randId"

            val studentData = hashMapOf(
                "uid" to firebaseUser.uid,
                "matricNumber" to matricNumber,
                "fullName" to fullName,
                "email" to email,
                "department" to department,
                "level" to level,
                "session" to session,
                "clearancePin" to clearancePin,
                "registrationDate" to "Today",
                "serverTimestamp" to System.currentTimeMillis()
            )

            // 3. Save Student Record in Firestore Server Database
            try {
                firestore?.let { db ->
                    awaitTask(
                        db.collection(STUDENTS_COLLECTION)
                            .document(matricNumber.replace("/", "_"))
                            .set(studentData, SetOptions.merge())
                    )
                    awaitTask(
                        db.collection(STUDENTS_COLLECTION)
                            .document(firebaseUser.uid)
                            .set(studentData, SetOptions.merge())
                    )
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Firestore sync warning: ${e.message}")
            }

            val entity = StudentUserEntity(
                matricNumber = matricNumber,
                fullName = fullName,
                email = email,
                department = department,
                level = level,
                session = session,
                loginPin = password,
                clearancePin = clearancePin,
                registrationDate = "Today",
                isLoggedIn = true
            )

            Result.success(entity)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase server registration failed", e)
            val friendlyMsg = mapFirebaseErrorMessage(e)
            Result.failure(Exception(friendlyMsg, e))
        }
    }

    suspend fun loginOnServer(
        emailOrMatric: String,
        password: String
    ): Result<Pair<String, Map<String, Any>?>> = withContext(Dispatchers.IO) {
        try {
            val currentAuth = auth ?: throw IllegalStateException("Firebase Authentication service unavailable.")

            var targetEmail = emailOrMatric.trim()

            // If user passed a matric number instead of an email, look up in Firestore server
            if (!targetEmail.contains("@")) {
                try {
                    firestore?.let { db ->
                        val doc = awaitTask(
                            db.collection(STUDENTS_COLLECTION)
                                .document(targetEmail.replace("/", "_"))
                                .get()
                        )
                        val foundEmail = doc.getString("email")
                        if (foundEmail != null) {
                            targetEmail = foundEmail
                        } else {
                            targetEmail = "$targetEmail@jigawapoly.edu.ng"
                        }
                    }
                } catch (e: Throwable) {
                    targetEmail = "$targetEmail@jigawapoly.edu.ng"
                }
            }

            val authResult: AuthResult = awaitTask(currentAuth.signInWithEmailAndPassword(targetEmail, password))
            val user = authResult.user
                ?: throw IllegalStateException("Firebase server login returned empty user session.")

            // Fetch stored profile from Firestore
            var profileData: Map<String, Any>? = null
            try {
                firestore?.let { db ->
                    val doc = awaitTask(
                        db.collection(STUDENTS_COLLECTION)
                            .document(user.uid)
                            .get()
                    )
                    if (doc.exists()) {
                        profileData = doc.data
                    }
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Firestore profile retrieval error: ${e.message}")
            }

            Result.success(Pair(user.uid, profileData))
        } catch (e: Exception) {
            Log.e(TAG, "Firebase server login failed", e)
            val friendlyMsg = mapFirebaseErrorMessage(e)
            Result.failure(Exception(friendlyMsg, e))
        }
    }

    suspend fun logoutServer() = withContext(Dispatchers.IO) {
        try {
            auth?.signOut()
        } catch (e: Throwable) {
            Log.e(TAG, "Firebase sign out error: ${e.message}")
        }
    }

    fun getCurrentFirebaseUser(): FirebaseUser? {
        return try {
            auth?.currentUser
        } catch (e: Throwable) {
            null
        }
    }

    private fun mapFirebaseErrorMessage(e: Exception): String {
        val msg = e.message ?: "Authentication server error."
        return when {
            msg.contains("The email address is already in use", ignoreCase = true) ||
            msg.contains("ERROR_EMAIL_ALREADY_IN_USE", ignoreCase = true) ->
                "This email is already registered on the server. Please sign in instead."

            msg.contains("The email address is badly formatted", ignoreCase = true) ||
            msg.contains("ERROR_INVALID_EMAIL", ignoreCase = true) ->
                "Invalid email format. Please check your email address."

            msg.contains("Password should be at least 6 characters", ignoreCase = true) ||
            msg.contains("ERROR_WEAK_PASSWORD", ignoreCase = true) ->
                "Password is too weak. Please use at least 6 characters."

            msg.contains("There is no user record corresponding to this identifier", ignoreCase = true) ||
            msg.contains("ERROR_USER_NOT_FOUND", ignoreCase = true) ||
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            msg.contains("wrong password", ignoreCase = true) ->
                "Invalid credentials. Please verify your Email/Matric and Password."

            msg.contains("network error", ignoreCase = true) ||
            msg.contains("UNAVAILABLE", ignoreCase = true) ->
                "Server network connection error. Please check your internet connection."

            else -> msg
        }
    }

    private suspend fun <T> awaitTask(task: Task<T>): T = suspendCancellableCoroutine { continuation ->
        task.addOnCompleteListener { completedTask ->
            if (completedTask.isSuccessful) {
                continuation.resume(completedTask.result)
            } else {
                continuation.resumeWithException(
                    completedTask.exception ?: RuntimeException("Firebase task failed without exception.")
                )
            }
        }
    }
}
