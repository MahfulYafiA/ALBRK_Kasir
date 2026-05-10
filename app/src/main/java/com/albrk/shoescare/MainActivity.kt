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
import com.albrk.shoescare.data.local.database.ShoeDatabase
import com.albrk.shoescare.data.repository.ShoeRepository
import com.albrk.shoescare.ui.theme.ALBRKSHOESCARETheme
import com.albrk.shoescare.ui.screen.auth.LoginScreen
import com.albrk.shoescare.viewmodel.ShoeViewModel
import com.albrk.shoescare.viewmodel.ShoeViewModelFactory

// Import dari folder staff
import com.albrk.shoescare.ui.screen.staff.MainScreen

class MainActivity : ComponentActivity() {

    /**
     * ✅ PERBAIKAN UTAMA: Menggunakan inisialisasi lazy yang lebih aman.
     * Pastikan ShoeRepository menerima dao dari database yang sudah di-init.
     */
    private val viewModel: ShoeViewModel by viewModels {
        val database = ShoeDatabase.getDatabase(applicationContext)
        val repository = ShoeRepository(database.shoeDao())
        ShoeViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ALBRKSHOESCARETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // State untuk melacak sesi login
                    var loggedInUserId by remember { mutableStateOf<String?>(null) }

                    if (loggedInUserId == null) {
                        // Tampilkan layar login
                        LoginScreen(onLoginClick = { id ->
                            loggedInUserId = id
                        })
                    } else {
                        // Tampilkan MainScreen (Dashboard Staf / Tracking Pelanggan)
                        MainScreen(
                            userId = loggedInUserId!!,
                            viewModel = viewModel,
                            onLogout = {
                                loggedInUserId = null // Reset sesi
                            }
                        )
                    }
                }
            }
        }
    }
}