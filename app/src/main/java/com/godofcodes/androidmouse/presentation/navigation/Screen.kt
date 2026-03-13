package com.godofcodes.androidmouse.presentation.navigation

sealed class Screen(val route: String) {
    data object Scan : Screen("scan")
    data object Touchpad : Screen("touchpad")
    data object Jiggler : Screen("jiggler")
}
