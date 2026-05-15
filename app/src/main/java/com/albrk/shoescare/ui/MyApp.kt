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
    // =======================================================
    // 1. INISIALISASI NAVIGASI & STATE GLOBAL (VIEWMODEL)
    // =======================================================
    // navController adalah pengontrol utama perpindahan halaman (Activity/Screen)
    val navController = rememberNavController()

    // Inisialisasi ViewModel di tingkat paling atas (MyApp) agar data (state)
    // tetap hidup dan bisa dipakai bersama oleh semua layar di bawahnya.
    val viewModel: ShoeViewModel = viewModel()

    // =======================================================
    // 2. NAVHOST (PENGATUR RUTE APLIKASI)
    // =======================================================
    // startDestination = "login" artinya saat aplikasi pertama kali dibuka,
    // halaman yang langsung muncul adalah layar Login.
    NavHost(navController = navController, startDestination = "login") {

        // --- RUTE 1: HALAMAN LOGIN ---
        composable("login") {
            LoginScreen(
                onLoginClick = { roleOrId ->
                    // Saat tombol login berhasil ditekan, sistem akan mengarahkan ke MainScreen.
                    // Parameter dikirim melalui URL rute, mirip seperti web (main/staff/staff)
                    navController.navigate("main/$roleOrId/$roleOrId") {
                        // KUNCI KEAMANAN: popUpTo memastikan halaman login dihapus dari riwayat (backstack).
                        // Jadi kalau user pencet tombol 'Back' di HP, mereka langsung keluar dari aplikasi,
                        // BUKAN kembali ke halaman login dalam keadaan sudah masuk.
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // --- RUTE 2: HALAMAN UTAMA (KHUSUS STAF) ---
        // Rute ini menerima dua argumen dinamis (variabel) yaitu {role} dan {userId}
        composable(
            route = "main/{role}/{userId}",
            arguments = listOf(
                navArgument("role") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Menangkap data yang dikirim dari halaman Login
            val role = backStackEntry.arguments?.getString("role") ?: "staff"
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            // Memanggil MainScreen dan meneruskan data userId serta ViewModel
            MainScreen(
                userId = userId,
                viewModel = viewModel,
                onLogout = {
                    // Logika saat tombol Logout ditekan di ProfileScreen:
                    // Arahkan kembali ke halaman "login"
                    navController.navigate("login") {
                        // Bersihkan seluruh riwayat MainScreen dari memori HP
                        // agar pengguna lain tidak bisa menekan tombol 'Back' untuk mengintip data
                        popUpTo("main/$role/$userId") { inclusive = true }
                    }
                }
            )
        }
    }
}