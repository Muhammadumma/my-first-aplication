package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClearanceOnPrimaryContainer
import com.example.ui.theme.ClearanceOnSurfaceVariant
import com.example.ui.theme.ClearancePrimary
import com.example.ui.theme.ClearancePrimaryContainer
import com.example.ui.theme.ClearanceSurface

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun BottomNav(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavTabItem("HOME", Icons.Default.GridView, "nav_home"),
        NavTabItem("TASKS", Icons.Default.FactCheck, "nav_tasks"),
        NavTabItem("AI", Icons.Default.Bolt, "nav_ai"),
        NavTabItem("ALERTS", Icons.Default.Notifications, "nav_alerts"),
        NavTabItem("PROFILE", Icons.Default.AccountCircle, "nav_profile")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ClearanceSurface)
            .shadow(elevation = 8.dp, spotColor = Color(0x1F000000))
            .navigationBarsPadding()
            .height(68.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedIndex == index
            val iconColor = if (isSelected) ClearancePrimary else ClearanceOnSurfaceVariant
            val textColor = if (isSelected) ClearancePrimary else ClearanceOnSurfaceVariant

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onTabSelected(index) }
                    )
                    .padding(vertical = 4.dp)
                    .testTag(item.tag),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ClearancePrimaryContainer.copy(alpha = 0.5f) else Color.Transparent)
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = item.title,
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

