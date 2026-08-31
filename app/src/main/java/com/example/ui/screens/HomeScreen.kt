package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivityItem
import com.example.data.ClearanceStage
import com.example.data.ClearanceStatus
import com.example.ui.theme.ClearanceBackground
import com.example.ui.theme.ClearanceError
import com.example.ui.theme.ClearanceErrorContainer
import com.example.ui.theme.ClearanceOnError
import com.example.ui.theme.ClearanceOnErrorContainer
import com.example.ui.theme.ClearanceOnPrimary
import com.example.ui.theme.ClearanceOnSecondary
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

@Composable
fun HomeScreen(
    stages: List<ClearanceStage>,
    activities: List<ActivityItem>,
    onTalkToAiClick: () -> Unit,
    onUploadClick: (stageId: Int) -> Unit,
    onViewAllTasksClick: () -> Unit,
    onViewAllActivitiesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedStagesCount = stages.count { it.status == ClearanceStatus.COMPLETED }
    val totalStages = if (stages.isNotEmpty()) stages.size else 8
    val progressPercentage = if (stages.isNotEmpty()) {
        (completedStagesCount.toFloat() / totalStages.toFloat()) * 100f
    } else 62.5f

    val actionRequiredStages = stages.filter { it.status == ClearanceStatus.ACTION_REQUIRED }
    val currentStageIndex = minOf(completedStagesCount + 1, totalStages)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ClearanceBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Radial Progress Meter Section
        item {
            RadialProgressSection(
                progressPercent = progressPercentage,
                currentStage = currentStageIndex,
                totalStages = totalStages
            )
        }

        // Next Steps Section
        item {
            NextStepsSection(
                stages = stages,
                actionRequiredCount = actionRequiredStages.size,
                onResolveClick = { stageId -> onUploadClick(stageId) },
                onViewAllClick = onViewAllTasksClick
            )
        }

        // Quick Actions Section
        item {
            QuickActionsSection(
                onTalkToAiClick = onTalkToAiClick,
                onUploadClick = { onUploadClick(4) }
            )
        }

        // Recent Activity Section
        item {
            RecentActivityHeader(onViewAllClick = onViewAllActivitiesClick)
        }

        items(activities.take(4)) { activity ->
            ActivityListItem(activity = activity)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RadialProgressSection(
    progressPercent: Float,
    currentStage: Int,
    totalStages: Int
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent / 100f,
        animationSpec = tween(durationMillis = 1200),
        label = "radial_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(210.dp)
                .testTag("radial_progress_gauge"),
            contentAlignment = Alignment.Center
        ) {
            // Glow Backdrop
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ClearancePrimaryContainer.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Circular Progress Arc
            Canvas(modifier = Modifier.size(180.dp)) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
                val arcSize = Size(radius * 2, radius * 2)

                // Background track
                drawArc(
                    color = ClearanceSurfaceContainerHighest,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active Progress arc
                drawArc(
                    color = ClearancePrimary,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Percentage Text Inside
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("${progressPercent.toInt()}")
                        withStyle(
                            style = SpanStyle(
                                fontSize = 24.sp,
                                color = ClearancePrimary,
                                fontWeight = FontWeight.Black
                            )
                        ) {
                            append("%")
                        }
                    },
                    color = ClearanceOnSurface,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp
                )
                Text(
                    text = "CLEARED",
                    color = ClearanceOnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Stage $currentStage of $totalStages",
            color = ClearanceOnSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "You're making great progress. Keep up the momentum.",
            color = ClearanceOnSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}

@Composable
fun NextStepsSection(
    stages: List<ClearanceStage>,
    actionRequiredCount: Int,
    onResolveClick: (Int) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Next Steps",
                color = ClearanceOnSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ClearancePrimaryContainer.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (actionRequiredCount > 0) "$actionRequiredCount Action Items" else "All On Track",
                    color = ClearancePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card 1: Action Required (e.g. Departmental / Bursary)
            val actionStage = stages.find { it.status == ClearanceStatus.ACTION_REQUIRED }
                ?: stages.find { it.id == 4 }
            if (actionStage != null) {
                NextStepCard(
                    title = "${actionStage.title} Clearance",
                    description = actionStage.rejectionReason ?: actionStage.description,
                    badgeText = "ACTION REQUIRED",
                    badgeColor = ClearanceError,
                    badgeBg = ClearanceErrorContainer,
                    icon = Icons.Default.Warning,
                    iconTint = ClearanceOnErrorContainer,
                    iconBg = ClearanceErrorContainer,
                    buttonText = "Resolve Now",
                    buttonBg = ClearanceError,
                    buttonTextColor = ClearanceOnError,
                    onButtonClick = { onResolveClick(actionStage.id) }
                )
            }

            // Card 2: Pending (e.g. Bursary / Examination)
            val pendingStage = stages.find { it.status == ClearanceStatus.PENDING }
                ?: stages.find { it.id == 5 }
            if (pendingStage != null) {
                NextStepCard(
                    title = "${pendingStage.title} Clearance",
                    description = pendingStage.description,
                    badgeText = "PENDING",
                    badgeColor = ClearancePrimary,
                    badgeBg = ClearancePrimaryContainer,
                    icon = Icons.Default.AccountBalance,
                    iconTint = ClearancePrimary,
                    iconBg = ClearancePrimaryContainer,
                    buttonText = "Reviewing",
                    buttonBg = ClearanceSurfaceContainerHighest,
                    buttonTextColor = ClearanceOnSurfaceVariant,
                    isButtonDisabled = true,
                    onButtonClick = {}
                )
            }

            // Card 3: Ready or Library
            val nextReadyStage = stages.find { it.status == ClearanceStatus.READY }
                ?: stages.find { it.id == 2 }
            if (nextReadyStage != null) {
                NextStepCard(
                    title = "${nextReadyStage.title} Clearance",
                    description = nextReadyStage.description,
                    badgeText = if (nextReadyStage.status == ClearanceStatus.COMPLETED) "CLEARED" else "READY",
                    badgeColor = ClearanceSecondary,
                    badgeBg = ClearanceSecondaryContainer,
                    icon = Icons.Default.LibraryBooks,
                    iconTint = ClearanceOnSecondaryContainer,
                    iconBg = ClearanceSecondaryContainer,
                    buttonText = if (nextReadyStage.status == ClearanceStatus.COMPLETED) "View Slip" else "Start Stage",
                    buttonBg = ClearancePrimary,
                    buttonTextColor = ClearanceOnPrimary,
                    onButtonClick = { onViewAllClick() }
                )
            }
        }
    }
}

@Composable
fun NextStepCard(
    title: String,
    description: String,
    badgeText: String,
    badgeColor: Color,
    badgeBg: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    buttonText: String,
    buttonBg: Color,
    buttonTextColor: Color,
    isButtonDisabled: Boolean = false,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x15000000))
            .border(1.dp, ClearanceOutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ClearanceSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                color = ClearanceOnSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                color = ClearanceOnSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onButtonClick,
                enabled = !isButtonDisabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("action_card_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonBg,
                    contentColor = buttonTextColor,
                    disabledContainerColor = buttonBg,
                    disabledContentColor = buttonTextColor
                )
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onTalkToAiClick: () -> Unit,
    onUploadClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Quick Actions",
            color = ClearanceOnSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Talk to Assistant Button (Cobalt primary card)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(3.dp, RoundedCornerShape(16.dp), spotColor = ClearancePrimary.copy(alpha = 0.3f))
                    .clickable { onTalkToAiClick() }
                    .testTag("quick_action_talk_to_assistant"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClearancePrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Assistant",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Talk to\nAssistant",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                }
            }

            // Upload Document Button (Surface white card with border)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x15000000))
                    .border(1.dp, ClearanceOutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { onUploadClick() }
                    .testTag("quick_action_upload_document"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClearanceSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ClearancePrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Upload Document",
                            tint = ClearancePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Upload\nDocument",
                        color = ClearanceOnSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RecentActivityHeader(onViewAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recent Activity",
            color = ClearanceOnSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )

        Text(
            text = "View All",
            color = ClearancePrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { onViewAllClick() }
                .padding(4.dp)
                .testTag("view_all_activities")
        )
    }
}

@Composable
fun ActivityListItem(activity: ActivityItem) {
    val (icon, iconBg, iconTint) = when (activity.status) {
        ClearanceStatus.COMPLETED -> Triple(Icons.Default.CheckCircle, ClearanceSecondaryContainer, ClearanceOnSecondaryContainer)
        ClearanceStatus.ACTION_REQUIRED -> Triple(Icons.Default.Cancel, ClearanceErrorContainer, ClearanceOnErrorContainer)
        ClearanceStatus.PENDING -> Triple(Icons.Default.Schedule, ClearancePrimaryContainer, ClearancePrimary)
        else -> Triple(Icons.Default.Info, ClearanceSurfaceContainerHigh, ClearanceOnSurfaceVariant)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .shadow(1.dp, RoundedCornerShape(14.dp), spotColor = Color(0x10000000))
            .border(1.dp, ClearanceOutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ClearanceSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    color = ClearanceOnSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = activity.description,
                    color = ClearanceOnSurfaceVariant,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activity.timeAgo,
                    color = ClearanceOnSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

