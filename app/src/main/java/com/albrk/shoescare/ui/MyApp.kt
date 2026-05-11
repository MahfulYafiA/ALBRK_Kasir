package com.albrk.shoescare.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.albrk.shoescare.ui.screen.auth.LoginScreen
import com.albrk.shoescare.ui.screen.staff.MainScreen
import com.albrk.shoescare.viewmodel.ShoeViewModel

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val viewModel: ShoeViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {

        // 1. HALAMAN LOGIN
        composable("login") {
            LoginScreen(
                // Sekarang onLoginClick cuma bawa SATU data (role/userId)
                // Sesuai dengan LoginScreen(onLoginClick: (String) -> Unit)
                onLoginClick = { roleOrId ->
                    // Kita asumsikan userId sama dengan role untuk sementara
                    // agar navigasi tetap jalan.
                    navController.navigate("main/$roleOrId/$roleOrId") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 2. HALAMAN UTAMA (KHUSUS STAF)
        composable(
            route = "main/{role}/{userId}",
            arguments = listOf(
                navArgument("role") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "staff"
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            MainScreen(
                userId = userId,
                viewModel = viewModel,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main/$role/$userId") { inclusive = true }
                    }
                }
            )
        }
    }
}