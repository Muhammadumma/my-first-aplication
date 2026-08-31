package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClearanceDocument
import com.example.data.ClearanceStage
import com.example.data.ClearanceStatus
import com.example.data.DepartmentRequirementsProvider
import com.example.data.DocumentStatus
import com.example.ui.theme.ClearanceBackground
import com.example.ui.theme.ClearanceError
import com.example.ui.theme.ClearanceErrorContainer
import com.example.ui.theme.ClearanceOnError
import com.example.ui.theme.ClearanceOnErrorContainer
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

data class StageNodeStyle(
    val icon: ImageVector,
    val bg: Color,
    val tint: Color,
    val isLocked: Boolean
)

@Composable
fun TasksScreen(
    stages: List<ClearanceStage>,
    documents: List<ClearanceDocument> = emptyList(),
    onReuploadClick: (stageId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val expandedStates = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            put(1, true) // Admission open by default
            put(2, false)
            put(3, false)
            put(4, true) // Bursary open by default
        }
    }

    val completedCount = stages.count { it.status == ClearanceStatus.COMPLETED }
    val totalStages = if (stages.isNotEmpty()) stages.size else 8
    val progressPercent = if (stages.isNotEmpty()) {
        ((completedCount.toFloat() / totalStages.toFloat()) * 100f).toInt()
    } else 37

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ClearanceBackground)
            .padding(horizontal = 20.dp)
            .testTag("tasks_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Clearance Stages",
                    color = ClearanceOnSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.0).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Upload requirements & verify documents for every university department.",
                    color = ClearanceOnSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            OverallProgressCard(
                progressPercent = progressPercent,
                completedCount = completedCount,
                totalStages = totalStages,
                hasActionRequired = stages.any { it.status == ClearanceStatus.ACTION_REQUIRED }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        itemsIndexed(stages) { index, stage ->
            val isExpanded = expandedStates[stage.id] ?: stage.isExpandedByDefault
            val isLast = index == stages.size - 1
            val stageDocs = documents.filter { it.stageId == stage.id }

            StepperStageItem(
                stage = stage,
                stageDocuments = stageDocs,
                isExpanded = isExpanded,
                isLast = isLast,
                onToggleExpand = {
                    expandedStates[stage.id] = !isExpanded
                },
                onUploadProof = { onReuploadClick(stage.id) }
            )
        }
    }
}

@Composable
fun OverallProgressCard(
    progressPercent: Int,
    completedCount: Int,
    totalStages: Int,
    hasActionRequired: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ClearanceSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OVERALL CLEARANCE STATUS",
                        color = ClearanceOnSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (hasActionRequired) "Attention Needed" else "Verification In Progress",
                        color = if (hasActionRequired) ClearanceError else ClearanceOnSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (hasActionRequired) ClearanceErrorContainer else ClearancePrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$progressPercent%",
                        color = if (hasActionRequired) ClearanceError else ClearancePrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (hasActionRequired) ClearanceError else ClearancePrimary,
                trackColor = ClearanceSurfaceContainerHighest,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$completedCount of $totalStages departments cleared",
                    color = ClearanceOnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "8 Required Verifications",
                    color = ClearancePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StepperStageItem(
    stage: ClearanceStage,
    stageDocuments: List<ClearanceDocument>,
    isExpanded: Boolean,
    isLast: Boolean,
    onToggleExpand: () -> Unit,
    onUploadProof: () -> Unit
) {
    val req = DepartmentRequirementsProvider.getRequirementForStage(stage.id)

    val nodeStyle = when (stage.status) {
        ClearanceStatus.COMPLETED -> StageNodeStyle(Icons.Default.CheckCircle, ClearanceSecondaryContainer, ClearanceSecondary, false)
        ClearanceStatus.ACTION_REQUIRED -> StageNodeStyle(Icons.Default.Cancel, ClearanceErrorContainer, ClearanceError, false)
        ClearanceStatus.PENDING -> StageNodeStyle(Icons.Default.Schedule, ClearancePrimaryContainer, ClearancePrimary, false)
        ClearanceStatus.READY -> StageNodeStyle(Icons.Default.Schedule, ClearanceSecondaryContainer, ClearanceSecondary, false)
        ClearanceStatus.LOCKED -> StageNodeStyle(Icons.Default.Lock, ClearanceSurfaceContainerHigh, ClearanceOnSurfaceVariant.copy(alpha = 0.6f), false)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stepper_item_${stage.id}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(nodeStyle.bg)
                    .clickable { onToggleExpand() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = nodeStyle.icon,
                    contentDescription = stage.title,
                    tint = nodeStyle.tint,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(if (isExpanded) 280.dp else 56.dp)
                        .background(ClearanceSurfaceContainerHighest)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 24.dp else 16.dp)
        ) {
            // Header Row (Clickable Accordion)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleExpand() }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${stage.stageNumber}. ${stage.title}",
                        color = if (stage.status == ClearanceStatus.ACTION_REQUIRED) ClearanceError else ClearanceOnSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stage.department,
                        color = ClearanceOnSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusBadgeText = when (stage.status) {
                        ClearanceStatus.COMPLETED -> "Cleared"
                        ClearanceStatus.ACTION_REQUIRED -> "Action Needed"
                        ClearanceStatus.PENDING -> "Under Review"
                        ClearanceStatus.READY -> "Ready"
                        ClearanceStatus.LOCKED -> "Pending"
                    }
                    val statusBadgeBg = when (stage.status) {
                        ClearanceStatus.COMPLETED -> ClearanceSecondaryContainer
                        ClearanceStatus.ACTION_REQUIRED -> ClearanceErrorContainer
                        ClearanceStatus.PENDING -> ClearancePrimaryContainer
                        ClearanceStatus.READY -> ClearanceSecondaryContainer
                        ClearanceStatus.LOCKED -> ClearanceSurfaceContainerHigh
                    }
                    val statusBadgeColor = when (stage.status) {
                        ClearanceStatus.COMPLETED -> ClearanceSecondary
                        ClearanceStatus.ACTION_REQUIRED -> ClearanceError
                        ClearanceStatus.PENDING -> ClearancePrimary
                        ClearanceStatus.READY -> ClearanceSecondary
                        ClearanceStatus.LOCKED -> ClearanceOnSurfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .background(statusBadgeBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusBadgeText,
                            color = statusBadgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    val rotation by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        label = "icon_rotation"
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = ClearanceOnSurfaceVariant,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

            // Expanded Stage Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .border(1.dp, ClearanceOutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ClearanceSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Stage Description
                        Text(
                            text = stage.description,
                            color = ClearanceOnSurface,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        // Rejection / Action Banner if applicable
                        if (stage.status == ClearanceStatus.ACTION_REQUIRED) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ClearanceErrorContainer.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = ClearanceError,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stage.rejectionReason ?: "Document rejected. Please provide a clear scan.",
                                    color = ClearanceOnErrorContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Requirements Checklist for this Department
                        Text(
                            text = "Department Requirements:",
                            color = ClearanceOnSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        req.requiredDocuments.forEach { reqDoc ->
                            val hasDoc = stageDocuments.any { it.documentType.contains(reqDoc.take(15), ignoreCase = true) } ||
                                    (stage.documentName != null && stage.status == ClearanceStatus.COMPLETED)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (hasDoc) Icons.Default.CheckCircle else Icons.Default.Description,
                                    contentDescription = null,
                                    tint = if (hasDoc) ClearanceSecondary else ClearanceOnSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = reqDoc,
                                    color = if (hasDoc) ClearanceOnSurface else ClearanceOnSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = if (hasDoc) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }

                        // Attached / Uploaded Documents
                        if (stage.documentName != null || stageDocuments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Attached Proof / Records:",
                                color = ClearanceOnSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val displayDocName = stage.documentName ?: stageDocuments.firstOrNull()?.fileName ?: "Proof_Document.pdf"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ClearanceSurfaceContainerHighest)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = ClearancePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = displayDocName,
                                            color = ClearanceOnSurface,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Ref: ${stage.receiptNumber ?: "Verified"} • ${stage.approvalDate ?: "Under Review"}",
                                            color = ClearanceOnSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                if (stage.status == ClearanceStatus.COMPLETED) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = ClearanceSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: "Upload / Scan Proof with CameraX"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onUploadProof,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("upload_button_stage_${stage.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (stage.status == ClearanceStatus.ACTION_REQUIRED) ClearanceError else ClearancePrimary,
                                    contentColor = ClearanceOnPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Camera Scan",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (stage.status == ClearanceStatus.ACTION_REQUIRED) "Re-upload with CameraX" else "Upload Proof / CameraX",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
