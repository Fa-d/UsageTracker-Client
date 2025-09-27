package dev.sadakat.screentimetracker.core.navigation

object ScreenTimeDestinations {
    const val DASHBOARD = "dashboard"
    const val ANALYTICS = "analytics"
    const val WELLNESS = "wellness"
    const val GOALS = "goals"
    const val SETTINGS = "settings"
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String,
    val selectedIcon: String = icon
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = ScreenTimeDestinations.DASHBOARD,
        title = "Dashboard",
        icon = "🏠",
        selectedIcon = "🏠"
    ),
    BottomNavItem(
        route = ScreenTimeDestinations.ANALYTICS,
        title = "Analytics",
        icon = "📊",
        selectedIcon = "📊"
    ),
    BottomNavItem(
        route = ScreenTimeDestinations.WELLNESS,
        title = "Wellness",
        icon = "🧘",
        selectedIcon = "🧘"
    ),
    BottomNavItem(
        route = ScreenTimeDestinations.GOALS,
        title = "Goals",
        icon = "🎯",
        selectedIcon = "🎯"
    ),
    BottomNavItem(
        route = ScreenTimeDestinations.SETTINGS,
        title = "Settings",
        icon = "⚙️",
        selectedIcon = "⚙️"
    )
)