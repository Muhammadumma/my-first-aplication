package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ClearanceStage
import com.example.data.ClearanceStatus
import com.example.data.StudentProfile
import com.example.ui.components.CameraCaptureView
import com.example.ui.theme.ClearanceBackground
import com.example.ui.theme.ClearanceError
import com.example.ui.theme.ClearanceErrorContainer
import com.example.ui.theme.ClearanceOnError
import com.example.ui.theme.ClearanceOnPrimary
import com.example.ui.theme.ClearanceOnSecondaryContainer
import com.example.ui.theme.ClearanceOnSurface
import com.example.ui.theme.ClearanceOnSurfaceVariant
import com.example.ui.theme.ClearanceOutlineVariant
import com.example.ui.theme.ClearancePrimary
import com.example.ui.theme.ClearancePrimaryContainer
import com.example.ui.theme.ClearanceSecondary
import com.example.ui.theme.ClearanceSecondaryContainer
import com.example.ui.theme.ClearanceSurface
import com.example.ui.theme.ClearanceSurfaceContainer
import com.example.ui.theme.ClearanceSurfaceContainerHigh
import com.example.ui.theme.ClearanceSurfaceContainerHighest
import com.example.ui.theme.ClearanceSurfaceContainerLow
import com.example.util.QrCodeUtil

@Composable
fun ProfileScreen(
    profile: StudentProfile,
    stages: List<ClearanceStage>,
    isAdminMode: Boolean,
    onToggleAdminMode: () -> Unit,
    onAdminApproveStage: (stageId: Int) -> Unit,
    onAdminRejectStage: (stageId: Int, reason: String) -> Unit,
    onResetDemo: () -> Unit,
    onLogin: (matricNo: String, pin: String) -> Unit = { _, _ -> },
    onLogout: () -> Unit = {},
    onUpdateProfile: (StudentProfile) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val completedStages = stages.count { it.status == ClearanceStatus.COMPLETED }
    val isFullyCleared = completedStages == stages.size && stages.isNotEmpty()

    // State for QR code generation
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showQrEnlargedDialog by remember { mutableStateOf(false) }

    // State for QR Scanner
    var showScannerDialog by remember { mutableStateOf(false) }
    var scannedStudentData by remember { mutableStateOf<Map<String, String>?>(null) }
    var showScannedDetailsDialog by remember { mutableStateOf(false) }

    // State for Edit Profile Dialog
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // State for Login Dashboard Dialog / Mode
    var showLoginDialog by remember { mutableStateOf(!profile.isLoggedIn) }

    // Generate or update QR code bitmap whenever profile or clearance status changes
    LaunchedEffect(profile, stages) {
        val payload = QrCodeUtil.buildStudentClearancePayload(profile, stages)
        qrBitmap = QrCodeUtil.generateQrCodeBitmap(payload, sizePx = 600)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ClearanceBackground)
            .padding(horizontal = 20.dp)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Student Portal",
                    color = ClearanceOnSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.0).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Authentication & Verifiable Digital Clearance Pass",
                    color = ClearanceOnSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Student Login / Authentication Status Card
        item {
            StudentAuthCard(
                profile = profile,
                onLoginClick = { showLoginDialog = true },
                onLogoutClick = {
                    onLogout()
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                },
                onEditClick = { showEditProfileDialog = true },
                onScanQrClick = { showScannerDialog = true }
            )
        }

        // Automatic Verifiable QR Code Pass Card
        item {
            VerifiableQrPassCard(
                profile = profile,
                stages = stages,
                qrBitmap = qrBitmap,
                isFullyCleared = isFullyCleared,
                completedStages = completedStages,
                onEnlargeQr = { showQrEnlargedDialog = true },
                onShareQr = {
                    Toast.makeText(
                        context,
                        "Clearance QR Token copied for ${profile.fullName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        // Student Dossier Information Details Card
        item {
            AcademicDossierCard(profile = profile)
        }

        // QR Code Verification Scanner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("qr_scanner_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClearanceSurfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ClearancePrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan QR",
                                tint = ClearancePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Scan Clearance QR Pass",
                                color = ClearanceOnSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Verify another student or officer clearance credentials",
                                color = ClearanceOnSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showScannerDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ClearancePrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan with Camera", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                // Simulate decoding current payload
                                val payload = QrCodeUtil.buildStudentClearancePayload(profile, stages)
                                scannedStudentData = QrCodeUtil.parseStudentClearanceData(payload)
                                showScannedDetailsDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Verify Self Pass", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Web Admin Portal Notice & Student Account Actions
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_webapp_notice_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClearanceSurfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ClearancePrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = ClearancePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Institutional Web Admin Database",
                                color = ClearanceOnSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Officer verification portal is web-hosted",
                                color = ClearanceOnSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Official department audits, approvals, and fee clearances are processed by university officers via the Web Admin Database console. This mobile client is dedicated for student submissions and digital pass display.",
                        color = ClearanceOnSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onResetDemo,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Data", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onLogout()
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ClearanceError),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Logout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Enlarge QR Code Pass
    if (showQrEnlargedDialog && qrBitmap != null) {
        Dialog(onDismissRequest = { showQrEnlargedDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "University Clearance Pass",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = profile.fullName,
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Matric: ${profile.matricNumber} • ${profile.department}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "Enlarged QR Code",
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Scan with any university terminal to verify clearance credentials.",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showQrEnlargedDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ClearancePrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Dialog: Scanner Simulation with CameraX
    if (showScannerDialog) {
        Dialog(onDismissRequest = { showScannerDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ClearanceSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QR Code Live Scanner",
                        color = ClearanceOnSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Point camera at student's official clearance QR code",
                        color = ClearanceOnSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .border(2.dp, ClearancePrimary, RoundedCornerShape(12.dp))
                            .background(ClearanceSurfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = ClearancePrimary,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val payload = QrCodeUtil.buildStudentClearancePayload(profile, stages)
                            scannedStudentData = QrCodeUtil.parseStudentClearanceData(payload)
                            showScannerDialog = false
                            showScannedDetailsDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ClearancePrimary)
                    ) {
                        Text("Simulate Live Scan", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showScannerDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = ClearanceOnSurfaceVariant)
                    }
                }
            }
        }
    }

    // Dialog: Scanned Clearance Dossier Result
    if (showScannedDetailsDialog && scannedStudentData != null) {
        val data = scannedStudentData!!
        AlertDialog(
            onDismissRequest = { showScannedDetailsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = ClearanceSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clearance Verification Result",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "AUTHENTICATED UNIVERSITY RECORD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClearanceSecondary,
                        letterSpacing = 1.sp
                    )
                    HorizontalDivider(color = ClearanceOutlineVariant)

                    ScannedField("Full Name", data["fullName"] ?: profile.fullName)
                    ScannedField("Matric No.", data["matricNumber"] ?: profile.matricNumber)
                    ScannedField("Student ID", data["studentId"] ?: profile.studentId)
                    ScannedField("Department", data["department"] ?: profile.department)
                    ScannedField("Faculty", data["faculty"] ?: profile.faculty)
                    ScannedField("Clearance Status", data["clearanceStatus"] ?: "IN_PROGRESS")
                    ScannedField("Progress", data["progress"] ?: "${completedStages}/${stages.size} completed")
                    ScannedField("Security Token", data["verificationHash"] ?: "SEC-VALID-TOKEN")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showScannedDetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ClearancePrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }

    // Dialog: Student Login Form
    if (showLoginDialog) {
        var matricInput by remember { mutableStateOf(profile.matricNumber) }
        var pinInput by remember { mutableStateOf(profile.loginPin) }
        var isPinVisible by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showLoginDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ClearanceSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = ClearancePrimary,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Student Login Dashboard",
                        color = ClearanceOnSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Sign in to access and generate your clearance QR code",
                        color = ClearanceOnSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = matricInput,
                        onValueChange = { matricInput = it },
                        label = { Text("Matriculation / Student ID") },
                        placeholder = { Text("MAT-19-44021") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = ClearancePrimary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text("Clearance PIN / Password") },
                        placeholder = { Text("Enter 4-digit PIN") },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = ClearancePrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle PIN visibility"
                                )
                            }
                        },
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset Demo Student Accounts for Quick Testing
                    Text(
                        text = "Or quick switch demo account:",
                        fontSize = 11.sp,
                        color = ClearanceOnSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                matricInput = "MAT-19-44021"
                                pinInput = "1234"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Sci (400L)", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                matricInput = "ENG-20-88190"
                                pinInput = "5678"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Eng (500L)", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (matricInput.isNotBlank()) {
                                onLogin(matricInput, pinInput)
                                showLoginDialog = false
                                Toast.makeText(context, "Welcome back, student!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ClearancePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log In to Dashboard", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { showLoginDialog = false }) {
                        Text("Cancel", color = ClearanceOnSurfaceVariant)
                    }
                }
            }
        }
    }

    // Dialog: Edit Student Profile Details
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(profile.fullName) }
        var editMatric by remember { mutableStateOf(profile.matricNumber) }
        var editDept by remember { mutableStateOf(profile.department) }
        var editFaculty by remember { mutableStateOf(profile.faculty) }
        var editLevel by remember { mutableStateOf(profile.level) }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ClearanceSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Edit Student Profile",
                        color = ClearanceOnSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editMatric,
                        onValueChange = { editMatric = it },
                        label = { Text("Matriculation Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editDept,
                        onValueChange = { editDept = it },
                        label = { Text("Department") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editFaculty,
                        onValueChange = { editFaculty = it },
                        label = { Text("Faculty") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onUpdateProfile(
                                profile.copy(
                                    fullName = editName,
                                    matricNumber = editMatric,
                                    department = editDept,
                                    faculty = editFaculty,
                                    level = editLevel
                                )
                            )
                            showEditProfileDialog = false
                            Toast.makeText(context, "Profile updated & QR regenerated!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ClearancePrimary)
                    ) {
                        Text("Save & Update QR Pass", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text("Cancel", color = ClearanceOnSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAuthCard(
    profile: StudentProfile,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onEditClick: () -> Unit,
    onScanQrClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_auth_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ClearanceSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ClearancePrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = ClearancePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = profile.fullName,
                            color = ClearanceOnSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Matric: ${profile.matricNumber}",
                            color = ClearanceOnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(ClearanceSecondaryContainer, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (profile.isLoggedIn) "Logged In" else "Guest",
                        color = ClearanceSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Info", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Switch Acc", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Logout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VerifiableQrPassCard(
    profile: StudentProfile,
    stages: List<ClearanceStage>,
    qrBitmap: Bitmap?,
    isFullyCleared: Boolean,
    completedStages: Int,
    onEnlargeQr: () -> Unit,
    onShareQr: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .testTag("qr_pass_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "UNIVERSITY DIGITAL CLEARANCE PASS",
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Official Academic Verification Token",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (isFullyCleared) ClearanceSecondaryContainer else ClearancePrimaryContainer,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isFullyCleared) "FULLY CLEARED" else "$completedStages/8 CLEARED",
                        color = if (isFullyCleared) ClearanceSecondary else ClearancePrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real Generated QR Code
            if (qrBitmap != null) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable { onEnlargeQr() }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Verifiable QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(ClearanceSurfaceContainerHighest, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Generating QR Code...", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "PIN: ${profile.clearancePin} • ID: ${profile.studentId}",
                color = Color.Black,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap QR Code to enlarge or show to department clearance officer",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEnlargeQr,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClearancePrimary)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enlarge QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onShareQr,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Token", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AcademicDossierCard(profile: StudentProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ClearanceSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Student Dossier",
                color = ClearanceOnSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            ProfileField("Full Name", profile.fullName)
            ProfileField("Matriculation No.", profile.matricNumber)
            ProfileField("Faculty", profile.faculty)
            ProfileField("Department", profile.department)
            ProfileField("Level / Class", profile.level)
            ProfileField("Academic Session", profile.session)
            ProfileField("Institutional Email", profile.email)
            ProfileField("Clearance PIN", profile.clearancePin)
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = ClearanceOnSurfaceVariant,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = ClearanceOnSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ScannedField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 11.sp, color = ClearanceOnSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClearanceOnSurface)
    }
}

@Composable
fun AdminSandboxSection(
    isAdminMode: Boolean,
    stages: List<ClearanceStage>,
    onToggleAdminMode: () -> Unit,
    onAdminApproveStage: (stageId: Int) -> Unit,
    onAdminRejectStage: (stageId: Int, reason: String) -> Unit,
    onResetDemo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ClearanceSurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = ClearancePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Admin Sandbox Mode",
                            color = ClearanceOnSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Simulate officer approval / rejections",
                            color = ClearanceOnSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = isAdminMode,
                    onCheckedChange = { onToggleAdminMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ClearancePrimary,
                        checkedTrackColor = ClearancePrimaryContainer
                    )
                )
            }

            AnimatedVisibility(visible = isAdminMode) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Department Officer Actions:",
                        color = ClearanceOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    stages.forEach { stage ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stage.stageNumber}. ${stage.title}",
                                color = ClearanceOnSurface,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onAdminApproveStage(stage.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ClearanceSecondary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        onAdminRejectStage(
                                            stage.id,
                                            "Document requires clearer official seal from ${stage.department}."
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ClearanceError),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onResetDemo,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset All Clearance Stages")
                    }
                }
            }
        }
    }
}
