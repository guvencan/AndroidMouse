package com.godofcodes.androidmouse.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.godofcodes.androidmouse.presentation.ui.jiggler.JigglerScreen
import com.godofcodes.androidmouse.presentation.ui.scan.DeviceScanScreen
import com.godofcodes.androidmouse.presentation.ui.touchpad.TouchpadScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Touchpad.route,
    ) {
        composable(Screen.Touchpad.route) {
            TouchpadScreen(
                onNavigateToJiggler = { navController.navigate(Screen.Jiggler.route) },
                onDisconnected = {
                    navController.navigate(Screen.Scan.route) {
                        popUpTo(Screen.Touchpad.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Scan.route) {
            DeviceScanScreen(
                onConnected = {
                    navController.navigate(Screen.Touchpad.route) {
                        popUpTo(Screen.Scan.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Jiggler.route) {
            JigglerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
