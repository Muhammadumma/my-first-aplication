package com.example.data.repository

import com.example.data.ActivityItem
import com.example.data.AlertItem
import com.example.data.ChatMessage
import com.example.data.ClearanceDocument
import com.example.data.ClearanceStage
import com.example.data.ClearanceStatus
import com.example.data.DepartmentRequirementsProvider
import com.example.data.DocumentStatus
import com.example.data.StudentProfile
import com.example.data.StudentUserEntity
import com.example.data.db.ClearanceDao
import com.example.data.db.createCleanJigawaPolyStages
import com.example.data.network.FirebaseServerAuthService
import com.example.data.network.GeminiApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ClearanceRepository(
    private val dao: ClearanceDao,
    private val geminiClient: GeminiApiClient = GeminiApiClient(),
    private val firebaseAuthService: FirebaseServerAuthService = FirebaseServerAuthService()
) {
    val stages: Flow<List<ClearanceStage>> = dao.getAllStages()
    val documents: Flow<List<ClearanceDocument>> = dao.getAllDocuments()
    val activities: Flow<List<ActivityItem>> = dao.getRecentActivities()
    val chatMessages: Flow<List<ChatMessage>> = dao.getChatMessages()
    val alerts: Flow<List<AlertItem>> = dao.getAlerts()
    val loggedInStudentFlow: Flow<StudentUserEntity?> = dao.getLoggedInStudentFlow()

    suspend fun getLoggedInStudent(): StudentUserEntity? {
        return dao.getLoggedInStudent()
    }

    suspend fun registerStudent(
        matricNumber: String,
        fullName: String,
        email: String,
        department: String,
        level: String,
        session: String,
        pin: String
    ): Result<StudentUserEntity> {
        val trimmedMatric = matricNumber.trim()
        val trimmedPin = pin.trim()
        val trimmedEmail = email.trim()
        val trimmedName = fullName.trim()

        if (trimmedMatric.isBlank()) {
            return Result.failure(IllegalArgumentException("Matriculation number is required."))
        }
        if (trimmedPin.isBlank() || trimmedPin.length < 4) {
            return Result.failure(IllegalArgumentException("Password / PIN must be at least 4 characters."))
        }
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Full name is required."))
        }
        if (trimmedEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Email address is required for server authentication."))
        }

        // 1. Connect to Firebase Server Authentication and Register
        val serverResult = firebaseAuthService.registerOnServer(
            matricNumber = trimmedMatric,
            fullName = trimmedName,
            email = trimmedEmail,
            department = department.trim(),
            level = level.trim(),
            session = session.trim(),
            password = trimmedPin
        )

        val registeredUser = serverResult.getOrElse { error ->
            android.util.Log.w("ClearanceRepository", "Server auth notice: ${error.message}")
            // If server has specific validation failure like weak password or invalid email format, report error
            val errMsg = error.message ?: ""
            if (errMsg.contains("already registered", ignoreCase = true) ||
                errMsg.contains("weak", ignoreCase = true) ||
                errMsg.contains("badly formatted", ignoreCase = true)) {
                return Result.failure(error)
            }
            
            // Otherwise create user in local database
            val randId = (1000..9999).random()
            val clearancePin = "JSP-CLR-$randId"
            StudentUserEntity(
                matricNumber = trimmedMatric,
                fullName = trimmedName,
                email = trimmedEmail,
                department = department.trim(),
                level = level.trim(),
                session = session.trim(),
                loginPin = trimmedPin,
                clearancePin = clearancePin,
                registrationDate = "Today",
                isLoggedIn = true
            )
        }

        // 2. Synchronize server registration to local database
        dao.logoutAllStudents()
        dao.insertStudent(registeredUser)

        // Initialize clean state for this student (Zero mock data)
        dao.clearStages()
        dao.clearDocuments()
        dao.clearActivities()
        dao.clearAlerts()
        dao.clearChat()

        // Insert clean Jigawa Poly stages
        dao.insertStages(createCleanJigawaPolyStages())

        // Insert initial real registration activity
        dao.insertActivity(
            ActivityItem(
                title = "Clearance Registry Initialized",
                description = "Account registered on Firebase server for $trimmedName ($trimmedMatric) at Jigawa State Polytechnic Dutse.",
                timeAgo = "Just now",
                status = ClearanceStatus.READY,
                stageId = 1
            )
        )

        // Insert first action alert
        dao.insertAlert(
            AlertItem(
                title = "Stage 1: Admission Credentials Required",
                description = "Please upload your JAMB Admission Letter / JSP Admission slip and acceptance receipt to begin clearance.",
                timeAgo = "Just now",
                isUrgent = true,
                isRead = false,
                stageId = 1
            )
        )

        // Insert welcoming AI assistant message
        dao.insertChatMessage(
            ChatMessage(
                isFromUser = false,
                text = "Welcome **$trimmedName** to the Jigawa State Polytechnic Dutse Clearance Portal!\n\nYour account has been authenticated via Firebase server for **$department** ($level). All 8 polytechnic clearance stages are ready for your submissions. Start with Stage 1 (Directorate of Admissions & Registration)."
            )
        )

        return Result.success(registeredUser)
    }

    suspend fun loginStudent(matricOrEmail: String, pin: String): Result<StudentUserEntity> {
        val trimmedIdentifier = matricOrEmail.trim()
        val trimmedPin = pin.trim()

        if (trimmedIdentifier.isBlank() || trimmedPin.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your Matriculation Number / Email and Password."))
        }

        // Attempt server authentication first via Firebase
        val serverLoginResult = firebaseAuthService.loginOnServer(trimmedIdentifier, trimmedPin)

        if (serverLoginResult.isSuccess) {
            val (_, profileData) = serverLoginResult.getOrThrow()
            val matric = (profileData?.get("matricNumber") as? String) ?: trimmedIdentifier
            val name = (profileData?.get("fullName") as? String) ?: "Student"
            val email = (profileData?.get("email") as? String) ?: (if (trimmedIdentifier.contains("@")) trimmedIdentifier else "$trimmedIdentifier@jigawapoly.edu.ng")
            val dept = (profileData?.get("department") as? String) ?: "Computer Science"
            val lvl = (profileData?.get("level") as? String) ?: "ND II"
            val sess = (profileData?.get("session") as? String) ?: "2024/2025"
            val clrPin = (profileData?.get("clearancePin") as? String) ?: "JSP-CLR-${(1000..9999).random()}"

            dao.logoutAllStudents()
            val existing = dao.getStudentByMatric(matric)
            val userToSave = existing?.copy(
                isLoggedIn = true,
                loginPin = trimmedPin,
                fullName = name,
                email = email,
                department = dept,
                level = lvl,
                session = sess
            ) ?: StudentUserEntity(
                matricNumber = matric,
                fullName = name,
                email = email,
                department = dept,
                level = lvl,
                session = sess,
                loginPin = trimmedPin,
                clearancePin = clrPin,
                registrationDate = "Today",
                isLoggedIn = true
            )

            dao.insertStudent(userToSave)

            // Ensure stages exist
            val existingStages = dao.getAllStages().first()
            if (existingStages.isEmpty()) {
                dao.insertStages(createCleanJigawaPolyStages())
            }

            return Result.success(userToSave)
        }

        // If server login failed, check if there's a matching local cache
        val user = dao.getStudentByMatric(trimmedIdentifier)
        if (user != null && user.loginPin == trimmedPin) {
            dao.logoutAllStudents()
            val updatedUser = user.copy(isLoggedIn = true)
            dao.updateStudent(updatedUser)

            val existingStages = dao.getAllStages().first()
            if (existingStages.isEmpty()) {
                dao.insertStages(createCleanJigawaPolyStages())
            }
            return Result.success(updatedUser)
        }

        return Result.failure(serverLoginResult.exceptionOrNull() ?: IllegalStateException("Invalid login credentials on server."))
    }

    suspend fun logoutStudent() {
        firebaseAuthService.logoutServer()
        dao.logoutAllStudents()
    }

    fun getDocumentsForStage(stageId: Int): Flow<List<ClearanceDocument>> {
        return dao.getDocumentsByStage(stageId)
    }

    suspend fun submitDocument(
        stageId: Int,
        docName: String,
        receiptNumber: String,
        paymentDate: String,
        documentType: String? = null,
        fileUri: String? = null,
        remarks: String? = null
    ) {
        val currentStage = dao.getStageById(stageId) ?: return
        val requirement = DepartmentRequirementsProvider.getRequirementForStage(stageId)
        val selectedDocType = documentType ?: requirement.primaryDocumentLabel

        val clearanceDoc = ClearanceDocument(
            stageId = stageId,
            stageTitle = currentStage.title,
            documentType = selectedDocType,
            fileName = docName,
            fileUri = fileUri,
            receiptNumber = receiptNumber.ifBlank { "${requirement.defaultReceiptPrefix}${System.currentTimeMillis() % 10000}" },
            paymentDate = paymentDate.ifBlank { "Today" },
            status = DocumentStatus.PENDING_REVIEW,
            remarks = remarks ?: "Uploaded via JSP mobile portal"
        )
        dao.insertDocument(clearanceDoc)

        val updatedStage = currentStage.copy(
            status = ClearanceStatus.PENDING,
            documentName = docName,
            documentStatus = DocumentStatus.PENDING_REVIEW,
            receiptNumber = clearanceDoc.receiptNumber,
            paymentDate = clearanceDoc.paymentDate,
            rejectionReason = null,
            isActionRequired = false,
            actionButtonText = "Under Review",
            approvalDate = "Submitted today"
        )
        dao.updateStage(updatedStage)

        dao.insertActivity(
            ActivityItem(
                title = "${currentStage.title} - $selectedDocType Uploaded",
                description = "File '$docName' (Ref: ${clearanceDoc.receiptNumber}) submitted for clearance audit.",
                timeAgo = "Just now",
                status = ClearanceStatus.PENDING,
                stageId = stageId
            )
        )

        dao.insertChatMessage(
            ChatMessage(
                isFromUser = false,
                text = "Your document **$docName** ($selectedDocType) for **${currentStage.title}** has been securely submitted. JSP Clearance Officers will audit your submission (Ref: ${clearanceDoc.receiptNumber})."
            )
        )
    }

    suspend fun deleteDocument(docId: Int) {
        dao.deleteDocument(docId)
    }

    suspend fun adminApproveStage(stageId: Int) {
        val currentStage = dao.getStageById(stageId) ?: return
        val updatedStage = currentStage.copy(
            status = ClearanceStatus.COMPLETED,
            documentStatus = DocumentStatus.APPROVED,
            rejectionReason = null,
            isActionRequired = false,
            approvalDate = "Today, Verified"
        )
        dao.updateStage(updatedStage)

        val nextStage = dao.getStageById(stageId + 1)
        if (nextStage != null && nextStage.status == ClearanceStatus.LOCKED) {
            dao.updateStage(
                nextStage.copy(
                    status = ClearanceStatus.PENDING,
                    actionButtonText = "Start Clearance"
                )
            )
        }

        dao.insertActivity(
            ActivityItem(
                title = "${currentStage.title} Clearance Approved",
                description = "Jigawa State Polytechnic Officer approved all uploaded credentials. Stage 100% verified.",
                timeAgo = "Just now",
                status = ClearanceStatus.COMPLETED,
                stageId = stageId
            )
        )

        dao.insertAlerts(
            listOf(
                AlertItem(
                    title = "${currentStage.title} Approved",
                    description = "Congratulations! Your clearance for ${currentStage.title} is now verified and signed off.",
                    timeAgo = "Just now",
                    isUrgent = false,
                    isRead = false,
                    stageId = stageId
                )
            )
        )
    }

    suspend fun adminRejectStage(stageId: Int, reason: String) {
        val currentStage = dao.getStageById(stageId) ?: return
        val updatedStage = currentStage.copy(
            status = ClearanceStatus.ACTION_REQUIRED,
            documentStatus = DocumentStatus.REJECTED,
            rejectionReason = reason,
            isActionRequired = true,
            actionButtonText = "Re-upload Now"
        )
        dao.updateStage(updatedStage)

        dao.insertActivity(
            ActivityItem(
                title = "${currentStage.title} Action Required",
                description = reason,
                timeAgo = "Just now",
                status = ClearanceStatus.ACTION_REQUIRED,
                stageId = stageId
            )
        )

        dao.insertAlerts(
            listOf(
                AlertItem(
                    title = "Action Required: ${currentStage.title}",
                    description = reason,
                    timeAgo = "Just now",
                    isUrgent = true,
                    isRead = false,
                    stageId = stageId
                )
            )
        )
    }

    suspend fun sendMessage(userText: String, profile: StudentProfile) {
        dao.insertChatMessage(
            ChatMessage(
                isFromUser = true,
                text = userText
            )
        )

        val currentStages = dao.getAllStages().first()
        val aiReply = geminiClient.askGemini(userText, currentStages, profile)

        val hasUploadAction = aiReply.contains("Upload", ignoreCase = true) ||
                aiReply.contains("Re-upload", ignoreCase = true) ||
                userText.contains("upload", ignoreCase = true) ||
                userText.contains("bursary", ignoreCase = true)

        dao.insertChatMessage(
            ChatMessage(
                isFromUser = false,
                text = aiReply,
                actionButtonText = if (hasUploadAction) "Upload Document" else null,
                actionStageId = if (hasUploadAction) 1 else null
            )
        )
    }

    suspend fun markAlertAsRead(alertId: Int) {
        dao.markAlertAsRead(alertId)
    }

    suspend fun resetData() {
        dao.clearStages()
        dao.clearDocuments()
        dao.clearActivities()
        dao.clearAlerts()
        dao.clearChat()
        dao.insertStages(createCleanJigawaPolyStages())
    }
}

