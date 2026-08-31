package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.network.FirebaseServerAuthService
import com.example.ui.ClearanceViewModel
import com.example.ui.components.BottomNav
import com.example.ui.components.TopHeader
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DocumentUploadScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.ClearanceBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ClearanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseServerAuthService.initializeIfPossible(applicationContext)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                ClearanceApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ClearanceApp(viewModel: ClearanceViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val stages by viewModel.stages.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val uploadStageId by viewModel.uploadScreenStageId.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()

    AnimatedContent(
        targetState = studentProfile.isLoggedIn,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally { it } + fadeIn()).togetherWith(
                    slideOutHorizontally { -it } + fadeOut()
                )
            } else {
                (slideInHorizontally { -it } + fadeIn()).togetherWith(
                    slideOutHorizontally { it } + fadeOut()
                )
            }
        },
        label = "auth_main_transition"
    ) { isLoggedIn ->
        if (!isLoggedIn) {
            AuthScreen(
                onLogin = { matric, pin, onResult ->
                    viewModel.loginStudent(matric, pin, onResult)
                },
                onRegister = { matric, name, email, dept, lvl, sess, pin, onResult ->
                    viewModel.registerStudent(matric, name, email, dept, lvl, sess, pin, onResult)
                }
            )
        } else {
            BackHandler(enabled = uploadStageId != null || selectedTab != 0) {
                if (uploadStageId != null) {
                    viewModel.closeUploadScreen()
                } else if (selectedTab != 0) {
                    viewModel.selectTab(0)
                }
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ClearanceBackground),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    if (uploadStageId == null) {
                        TopHeader(
                            onProfileClick = { viewModel.selectTab(4) }
                        )
                    }
                },
                bottomBar = {
                    if (uploadStageId == null) {
                        BottomNav(
                            selectedIndex = selectedTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ClearanceBackground)
                        .padding(innerPadding)
                ) {
                    if (uploadStageId != null) {
                        DocumentUploadScreen(
                            stageId = uploadStageId ?: 1,
                            stages = stages,
                            documents = documents,
                            onBack = { viewModel.closeUploadScreen() },
                            onSubmit = { stageId, docName, receiptNum, paymentDate, docType, fileUri, remarks ->
                                viewModel.submitDocument(
                                    stageId = stageId,
                                    docName = docName,
                                    receiptNum = receiptNum,
                                    paymentDate = paymentDate,
                                    documentType = docType,
                                    fileUri = fileUri,
                                    remarks = remarks
                                )
                            },
                            onDeleteDocument = { docId ->
                                viewModel.deleteDocument(docId)
                            }
                        )
                    } else {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> -width } + fadeOut()
                                    )
                                } else {
                                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> width } + fadeOut()
                                    )
                                }
                            },
                            label = "tab_content_transition"
                        ) { tab ->
                            when (tab) {
                                0 -> HomeScreen(
                                    stages = stages,
                                    activities = activities,
                                    onTalkToAiClick = { viewModel.selectTab(2) },
                                    onUploadClick = { stageId -> viewModel.openUploadScreen(stageId) },
                                    onViewAllTasksClick = { viewModel.selectTab(1) },
                                    onViewAllActivitiesClick = { viewModel.selectTab(3) }
                                )
                                1 -> TasksScreen(
                                    stages = stages,
                                    documents = documents,
                                    onReuploadClick = { stageId -> viewModel.openUploadScreen(stageId) }
                                )
                                2 -> AiAssistantScreen(
                                    messages = chatMessages,
                                    isThinking = isAiThinking,
                                    onSendMessage = { text -> viewModel.sendChatMessage(text) },
                                    onActionClick = { stageId -> viewModel.openUploadScreen(stageId) }
                                )
                                3 -> AlertsScreen(
                                    alerts = alerts,
                                    onResolveAlert = { stageId -> viewModel.openUploadScreen(stageId) },
                                    onMarkRead = { alertId -> viewModel.markAlertRead(alertId) }
                                )
                                4 -> ProfileScreen(
                                    profile = studentProfile,
                                    stages = stages,
                                    isAdminMode = isAdminMode,
                                    onToggleAdminMode = { viewModel.toggleAdminMode() },
                                    onAdminApproveStage = { stageId -> viewModel.adminApprove(stageId) },
                                    onAdminRejectStage = { stageId, reason -> viewModel.adminReject(stageId, reason) },
                                    onResetDemo = { viewModel.resetDemoData() },
                                    onLogin = { matricNo, pin -> viewModel.loginStudent(matricNo, pin) { _, _ -> } },
                                    onLogout = { viewModel.logoutStudent() },
                                    onUpdateProfile = { updated -> viewModel.updateStudentProfile(updated) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
