package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityItem
import com.example.data.AlertItem
import com.example.data.ChatMessage
import com.example.data.ClearanceDocument
import com.example.data.ClearanceStage
import com.example.data.StudentProfile
import com.example.data.db.ClearanceDatabase
import com.example.data.repository.ClearanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClearanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ClearanceDatabase.getDatabase(application, viewModelScope)
    private val repository = ClearanceRepository(database.clearanceDao())

    val stages: StateFlow<List<ClearanceStage>> = repository.stages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val documents: StateFlow<List<ClearanceDocument>> = repository.documents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activities: StateFlow<List<ActivityItem>> = repository.activities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val alerts: StateFlow<List<AlertItem>> = repository.alerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _studentProfile = MutableStateFlow(StudentProfile())
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Tasks, 2: AI, 3: Alerts, 4: Profile
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _uploadScreenStageId = MutableStateFlow<Int?>(null)
    val uploadScreenStageId: StateFlow<Int?> = _uploadScreenStageId.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    init {
        // Observe logged in student in Room database
        viewModelScope.launch {
            repository.loggedInStudentFlow.collect { user ->
                if (user != null && user.isLoggedIn) {
                    _studentProfile.value = StudentProfile(
                        studentId = user.matricNumber,
                        fullName = user.fullName,
                        email = user.email,
                        faculty = "School of Technology & Applied Sciences",
                        department = user.department,
                        level = user.level,
                        session = user.session,
                        matricNumber = user.matricNumber,
                        clearancePin = user.clearancePin,
                        role = "student",
                        isLoggedIn = true,
                        loginPin = user.loginPin,
                        lastLoginTime = "Today, Active"
                    )
                } else {
                    _studentProfile.value = StudentProfile(isLoggedIn = false)
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
        _uploadScreenStageId.value = null
    }

    fun openUploadScreen(stageId: Int = 1) {
        _uploadScreenStageId.value = stageId
    }

    fun closeUploadScreen() {
        _uploadScreenStageId.value = null
    }

    fun submitDocument(
        stageId: Int,
        docName: String,
        receiptNum: String,
        paymentDate: String,
        documentType: String? = null,
        fileUri: String? = null,
        remarks: String? = null
    ) {
        viewModelScope.launch {
            repository.submitDocument(
                stageId = stageId,
                docName = docName,
                receiptNumber = receiptNum,
                paymentDate = paymentDate,
                documentType = documentType,
                fileUri = fileUri,
                remarks = remarks
            )
            _uploadScreenStageId.value = null
        }
    }

    fun deleteDocument(docId: Int) {
        viewModelScope.launch {
            repository.deleteDocument(docId)
        }
    }

    fun loginStudent(
        matricNo: String,
        pin: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.loginStudent(matricNo, pin)
            res.fold(
                onSuccess = { user ->
                    _studentProfile.value = StudentProfile(
                        studentId = user.matricNumber,
                        fullName = user.fullName,
                        email = user.email,
                        department = user.department,
                        level = user.level,
                        session = user.session,
                        matricNumber = user.matricNumber,
                        clearancePin = user.clearancePin,
                        isLoggedIn = true,
                        loginPin = user.loginPin,
                        lastLoginTime = "Just now"
                    )
                    onResult(true, "Welcome back, ${user.fullName}!")
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Authentication failed.")
                }
            )
        }
    }

    fun registerStudent(
        matricNo: String,
        fullName: String,
        email: String,
        department: String,
        level: String,
        session: String,
        pin: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.registerStudent(
                matricNumber = matricNo,
                fullName = fullName,
                email = email,
                department = department,
                level = level,
                session = session,
                pin = pin
            )
            res.fold(
                onSuccess = { user ->
                    _studentProfile.value = StudentProfile(
                        studentId = user.matricNumber,
                        fullName = user.fullName,
                        email = user.email,
                        department = user.department,
                        level = user.level,
                        session = user.session,
                        matricNumber = user.matricNumber,
                        clearancePin = user.clearancePin,
                        isLoggedIn = true,
                        loginPin = user.loginPin,
                        lastLoginTime = "Just now"
                    )
                    onResult(true, "Registration successful! Clearance initialized for ${user.fullName}.")
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Registration failed.")
                }
            )
        }
    }

    fun logoutStudent() {
        viewModelScope.launch {
            repository.logoutStudent()
            _studentProfile.value = StudentProfile(isLoggedIn = false)
            _selectedTab.value = 0
        }
    }

    fun updateStudentProfile(updatedProfile: StudentProfile) {
        _studentProfile.value = updatedProfile
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                repository.sendMessage(text, _studentProfile.value)
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun markAlertRead(alertId: Int) {
        viewModelScope.launch {
            repository.markAlertAsRead(alertId)
        }
    }

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
    }

    fun adminApprove(stageId: Int) {
        viewModelScope.launch {
            repository.adminApproveStage(stageId)
        }
    }

    fun adminReject(stageId: Int, reason: String) {
        viewModelScope.launch {
            repository.adminRejectStage(stageId, reason)
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetData()
        }
    }
}

