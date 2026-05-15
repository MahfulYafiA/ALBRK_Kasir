package com.albrk.shoescare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.albrk.shoescare.ui.theme.ALBRKSHOESCARETheme
import com.albrk.shoescare.ui.screen.auth.LoginScreen
import com.albrk.shoescare.ui.screen.staff.MainScreen
import com.albrk.shoescare.viewmodel.ShoeViewModel
import com.albrk.shoescare.viewmodel.ShoeViewModelFactory

/**
 * MAIN ACTIVITY (ENTRY POINT)
 * Fungsi: Merupakan pintu masuk utama aplikasi Android.
 * Di sini kita mengatur tema global dan logika navigasi tingkat paling atas (Top-Level Navigation).
 */
class MainActivity : ComponentActivity() {

    // =======================================================
    // 1. INISIALISASI VIEWMODEL (VERSI BERSIH)
    // =======================================================
    // Kita tidak lagi memanggil ShoeDatabase atau ShoeRepository di sini.
    // ShoeViewModel sekarang langsung menangani Firebase secara mandiri.
    private val viewModel: ShoeViewModel by viewModels {
        ShoeViewModelFactory() // Factory sekarang tidak memerlukan parameter repository lagi
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengatur tampilan konten menggunakan Jetpack Compose
        setContent {
            // Menerapkan tema kustom ALBRK yang sudah didefinisikan di ui.theme
            ALBRKSHOESCARETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // =======================================================
                    // 2. LOGIKA AUTENTIKASI (SESI LOGIN)
                    // =======================================================
                    // loggedInUserId berfungsi sebagai penanda:
                    // Jika null = User belum login (tampilkan LoginScreen)
                    // Jika terisi = User sudah login (tampilkan MainScreen/Dashboard)
                    var loggedInUserId by remember { mutableStateOf<String?>(null) }

                    if (loggedInUserId == null) {
                        // --- MENAMPILKAN HALAMAN LOGIN ---
                        LoginScreen(onLoginClick = { id ->
                            // Callback: Saat login sukses di LoginScreen, simpan ID user ke state
                            loggedInUserId = id
                        })
                    } else {
                        // --- MENAMPILKAN HALAMAN UTAMA STAF ---
                        // MainScreen bertindak sebagai navigasi internal untuk Dashboard, Laporan, dll.
                        MainScreen(
                            userId = loggedInUserId!!,
                            viewModel = viewModel,
                            onLogout = {
                                // Callback: Saat tombol Logout ditekan, reset state menjadi null
                                // UI otomatis akan kembali menampilkan LoginScreen (Recomposition).
                                loggedInUserId = null
                            }
                        )
                    }
                }
            }
        }
    }
}