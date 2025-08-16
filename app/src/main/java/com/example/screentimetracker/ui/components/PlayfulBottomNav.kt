package com.example.screentimetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.screentimetracker.ui.theme.*

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun PlayfulBottomNav(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        NavItem("dashboard_route", "Dashboard", Icons.Filled.Dashboard, MaterialTheme.colorScheme.primary),
        NavItem("analytics_route", "Analytics", Icons.Filled.Analytics, MaterialTheme.colorScheme.secondary),
        NavItem("wellness_route", "Wellness", Icons.Filled.Healing, MaterialTheme.colorScheme.tertiary),
        NavItem("goals_route", "Goals", Icons.Filled.EmojiEvents, MaterialTheme.colorScheme.tertiary),
        NavItem("settings_route", "Settings", Icons.Filled.Settings, MaterialTheme.colorScheme.secondary)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(navItems) { item ->
                PlayfulNavItem(
                    navItem = item,
                    isSelected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PlayfulNavItem(
    navItem: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        navItem.color.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }
    
    val contentColor = if (isSelected) {
        navItem.color
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = navItem.icon,
            contentDescription = navItem.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = navItem.label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = contentColor
        )
        
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(2.dp)
                    .background(
                        color = navItem.color,
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}