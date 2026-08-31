package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AlertItem
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
import com.example.ui.theme.ClearanceSecondary
import com.example.ui.theme.ClearanceSecondaryContainer
import com.example.ui.theme.ClearanceSurface
import com.example.ui.theme.ClearanceSurfaceContainer
import com.example.ui.theme.ClearanceSurfaceContainerHigh
import com.example.ui.theme.ClearanceSurfaceContainerLow

@Composable
fun AlertsScreen(
    alerts: List<AlertItem>,
    onResolveAlert: (stageId: Int) -> Unit,
    onMarkRead: (alertId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredAlerts = remember(alerts, selectedFilter) {
        when (selectedFilter) {
            "Urgent" -> alerts.filter { it.isUrgent }
            "Unread" -> alerts.filter { !it.isRead }
            else -> alerts
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ClearanceBackground)
            .padding(horizontal = 20.dp)
            .testTag("alerts_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Clearance Alerts",
                    color = ClearanceOnSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.0).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Real-time updates on administrative verification.",
                    color = ClearanceOnSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Urgent", "Unread").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ClearancePrimary,
                            selectedLabelColor = ClearanceOnPrimary,
                            containerColor = ClearanceSurface,
                            labelColor = ClearanceOnSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) ClearancePrimary else ClearanceOutlineVariant.copy(alpha = 0.5f),
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        if (filteredAlerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = ClearanceSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "You're all caught up!",
                            color = ClearanceOnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No alerts under $selectedFilter filter.",
                            color = ClearanceOnSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        items(filteredAlerts) { alert ->
            AlertCardItem(
                alert = alert,
                onResolve = { alert.stageId?.let { onResolveAlert(it) } },
                onMarkRead = { onMarkRead(alert.id) }
            )
        }
    }
}

@Composable
fun AlertCardItem(
    alert: AlertItem,
    onResolve: () -> Unit,
    onMarkRead: () -> Unit
) {
    val (icon, iconBg, iconTint) = if (alert.isUrgent) {
        Triple(Icons.Default.Warning, ClearanceErrorContainer, ClearanceOnErrorContainer)
    } else {
        Triple(Icons.Default.NotificationsActive, ClearanceSecondaryContainer, ClearanceOnSecondaryContainer)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x15000000))
            .border(
                1.dp,
                if (alert.isUrgent) ClearanceError.copy(alpha = 0.4f) else ClearanceOutlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onMarkRead() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!alert.isRead) ClearanceSurface else ClearanceSurfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = alert.title,
                            color = if (alert.isUrgent) ClearanceError else ClearanceOnSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = alert.timeAgo,
                            color = ClearanceOnSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = alert.description,
                        color = ClearanceOnSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal
                    )

                    if (alert.stageId != null && alert.isUrgent) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onResolve,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClearanceError,
                                contentColor = ClearanceOnError
                            )
                        ) {
                            Text(
                                text = "Resolve Now",
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

