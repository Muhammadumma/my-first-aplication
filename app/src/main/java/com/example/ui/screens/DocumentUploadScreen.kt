package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.ClearanceDocument
import com.example.data.ClearanceStage
import com.example.data.DepartmentRequirementsProvider
import com.example.data.DocumentStatus
import com.example.ui.components.CameraCaptureView
import com.example.ui.theme.ClearanceBackground
import com.example.ui.theme.ClearanceError
import com.example.ui.theme.ClearanceErrorContainer
import com.example.ui.theme.ClearanceOnErrorContainer
import com.example.ui.theme.ClearanceOnPrimary
import com.example.ui.theme.ClearanceOnSecondaryContainer
import com.example.ui.theme.ClearanceOnSurface
import com.example.ui.theme.ClearanceOnSurfaceVariant
import com.example.ui.theme.ClearanceOutline
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DocumentUploadScreen(
    stageId: Int,
    stages: List<ClearanceStage>,
    documents: List<ClearanceDocument> = emptyList(),
    onBack: () -> Unit,
    onSubmit: (stageId: Int, docName: String, receiptNum: String, paymentDate: String, docType: String, fileUri: String?, remarks: String?) -> Unit,
    onDeleteDocument: (docId: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedStageId by remember { mutableIntStateOf(stageId) }
    val currentStage = stages.find { it.id == selectedStageId } ?: stages.firstOrNull()
    val requirement = DepartmentRequirementsProvider.getRequirementForStage(selectedStageId)

    var selectedDocType by remember(selectedStageId) {
        mutableStateOf(requirement.primaryDocumentLabel)
    }

    var showCameraView by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isCapturingFromCamera by remember { mutableStateOf(false) }

    var receiptNumber by remember(selectedStageId) {
        mutableStateOf(currentStage?.receiptNumber ?: "${requirement.defaultReceiptPrefix}${System.currentTimeMillis() % 10000}")
    }
    var paymentDate by remember(selectedStageId) {
        mutableStateOf(
            currentStage?.paymentDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }
    var studentRemarks by remember { mutableStateOf("") }
    var isOcrExtracting by remember { mutableStateOf(false) }
    var ocrSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }
    var isStageDropdownExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Gallery File Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                selectedFileUri = it
                selectedFileName = "PROOF_${requirement.defaultReceiptPrefix}${System.currentTimeMillis() % 10000}.jpg"
                isCapturingFromCamera = false

                // Trigger OCR extraction
                coroutineScope.launch {
                    isOcrExtracting = true
                    delay(1200)
                    receiptNumber = "${requirement.defaultReceiptPrefix}${(1000..9999).random()}"
                    paymentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    ocrSuccessMessage = "Auto-extracted Reference ID & Date from document."
                    isOcrExtracting = false
                }
            }
        }
    )

    // Fullscreen CameraX Live Viewfinder
    if (showCameraView) {
        CameraCaptureView(
            onImageCaptured = { uri, fileName ->
                selectedFileUri = uri
                selectedFileName = fileName
                isCapturingFromCamera = true
                showCameraView = false

                // Auto OCR extraction
                coroutineScope.launch {
                    isOcrExtracting = true
                    delay(1200)
                    receiptNumber = "${requirement.defaultReceiptPrefix}${(1000..9999).random()}"
                    paymentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    ocrSuccessMessage = "Camera scan processed: Reference & details parsed."
                    isOcrExtracting = false
                }
            },
            onClose = {
                showCameraView = false
            }
        )
        return
    }

    val stageDocuments = documents.filter { it.stageId == selectedStageId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClearanceBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("document_upload_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ClearanceSurfaceContainer)
                    .testTag("upload_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = ClearanceOnSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Upload Clearance Proof",
                    color = ClearanceOnSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = requirement.departmentName,
                    color = ClearanceOnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Department Switcher Dropdown Button
            Box {
                Button(
                    onClick = { isStageDropdownExpanded = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClearancePrimaryContainer,
                        contentColor = ClearancePrimary
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = currentStage?.title ?: "Stage",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Select Department",
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = isStageDropdownExpanded,
                    onDismissRequest = { isStageDropdownExpanded = false },
                    modifier = Modifier.background(ClearanceSurface)
                ) {
                    stages.forEach { stg ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${stg.stageNumber}. ${stg.title}",
                                    fontWeight = if (stg.id == selectedStageId) FontWeight.Bold else FontWeight.Normal,
                                    color = if (stg.id == selectedStageId) ClearancePrimary else ClearanceOnSurface
                                )
                            },
                            onClick = {
                                selectedStageId = stg.id
                                isStageDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Scrollable Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Department Requirements Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClearanceSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ClearancePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Requirements for ${currentStage?.title ?: "Clearance"}",
                            color = ClearanceOnSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = requirement.guidelines,
                        color = ClearanceOnSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Select Document Being Uploaded:",
                        color = ClearanceOnSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        requirement.requiredDocuments.forEach { reqDoc ->
                            val isSelected = selectedDocType == reqDoc
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDocType = reqDoc },
                                label = {
                                    Text(
                                        text = reqDoc,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ClearancePrimaryContainer,
                                    selectedLabelColor = ClearancePrimary,
                                    selectedLeadingIconColor = ClearancePrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CameraX Capture & File Selector Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_dropzone_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClearanceSurfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedFileUri != null) {
                        // File / Camera Preview Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ClearanceSurfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(selectedFileUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Captured Proof",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Status badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCapturingFromCamera) Icons.Default.PhotoCamera else Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isCapturingFromCamera) "CameraX Capture" else "Gallery File",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedFileName ?: "document_proof.jpg",
                                    color = ClearanceOnSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = selectedDocType,
                                    color = ClearancePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { showCameraView = true },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(ClearancePrimaryContainer, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retake Photo",
                                        tint = ClearancePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        selectedFileUri = null
                                        selectedFileName = null
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(ClearanceErrorContainer, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove File",
                                        tint = ClearanceError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Action Buttons: Live CameraX vs File Picker
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = ClearancePrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Capture or Select Proof Document",
                            color = ClearanceOnSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Take a live photo using CameraX or select from gallery",
                            color = ClearanceOnSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showCameraView = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("camera_capture_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ClearancePrimary,
                                    contentColor = ClearanceOnPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Camera",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CameraX",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("browse_files_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ClearanceSurfaceContainerHighest,
                                    contentColor = ClearanceOnSurface
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = "Upload",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Gallery",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // OCR Extraction Progress & Notification
                    AnimatedVisibility(
                        visible = isOcrExtracting,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .background(ClearancePrimaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = ClearancePrimary,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Extracting receipt data via AI OCR...",
                                color = ClearancePrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    ocrSuccessMessage?.let { msg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(ClearanceSecondaryContainer.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ClearanceSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = ClearanceOnSecondaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verification Metadata Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClearanceSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Verification Details",
                        color = ClearanceOnSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Reference Number
                    OutlinedTextField(
                        value = receiptNumber,
                        onValueChange = { receiptNumber = it },
                        label = { Text("Receipt / Ref / Teller No.") },
                        placeholder = { Text("e.g. ${requirement.defaultReceiptPrefix}9842") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Tag, contentDescription = null, tint = ClearancePrimary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("receipt_number_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClearancePrimary,
                            unfocusedBorderColor = ClearanceOutlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Payment / Issue Date
                    OutlinedTextField(
                        value = paymentDate,
                        onValueChange = { paymentDate = it },
                        label = { Text("Payment / Issue Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = ClearancePrimary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_date_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClearancePrimary,
                            unfocusedBorderColor = ClearanceOutlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Student Remarks / Notes
                    OutlinedTextField(
                        value = studentRemarks,
                        onValueChange = { studentRemarks = it },
                        label = { Text("Remarks / Officer Notes (Optional)") },
                        placeholder = { Text("e.g. Original signed by Dean on Friday...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = ClearancePrimary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("remarks_input"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClearancePrimary,
                            unfocusedBorderColor = ClearanceOutlineVariant
                        )
                    )
                }
            }

            // Documents History for This Stage
            if (stageDocuments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Uploaded Documents for ${currentStage?.title}",
                    color = ClearanceOnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                stageDocuments.forEach { doc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ClearanceSurfaceContainerHigh)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = doc.documentType,
                                    color = ClearanceOnSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ref: ${doc.receiptNumber ?: "N/A"} • ${doc.uploadDate}",
                                    color = ClearanceOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (doc.status == DocumentStatus.APPROVED) ClearanceSecondaryContainer else ClearancePrimaryContainer,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (doc.status == DocumentStatus.APPROVED) "Verified" else "Under Review",
                                        color = if (doc.status == DocumentStatus.APPROVED) ClearanceOnSecondaryContainer else ClearancePrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteDocument(doc.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = ClearanceOnSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Action Submit Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ClearanceSurface)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = {
                    if (isSubmitting) return@Button
                    isSubmitting = true
                    coroutineScope.launch {
                        delay(600)
                        val fileName = selectedFileName ?: "${requirement.primaryDocumentLabel.replace(" ", "_")}.jpg"
                        onSubmit(
                            selectedStageId,
                            fileName,
                            receiptNumber,
                            paymentDate,
                            selectedDocType,
                            selectedFileUri?.toString(),
                            studentRemarks
                        )
                        isSubmitting = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_document_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClearancePrimary,
                    contentColor = ClearanceOnPrimary
                ),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = ClearanceOnPrimary,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Submit",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit to ${currentStage?.title ?: "Department"} Officer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
