package com.albrk.shoescare.ui.screen.staff

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Import Firebase Auth
import com.google.firebase.auth.FirebaseAuth

// =======================================================
// IMPORT DARI FOLDER PELANGGAN, ADD, DAN VIEWMODEL
// =======================================================
import com.albrk.shoescare.ui.screen.pelanggan.PelangganTrackingScreen
import com.albrk.shoescare.ui.screen.add.AddShoeScreen
import com.albrk.shoescare.viewmodel.ShoeViewModel

@Composable
fun MainScreen(userId: String, viewModel: ShoeViewModel, onLogout: () -> Unit) {

    // Ambil data user yang sedang login langsung dari mesin Firebase
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email

    // Gabungkan logika Firebase dan logika Backup (userId) biar kodenya nggak berulang
    if (userEmail == "staf@gmail.com" || userId == "staff") {

        // =======================================================
        // ROUTING KHUSUS STAF (Dashboard & Keranjang)
        // =======================================================
        var staffScreen by remember { mutableStateOf("dashboard") }
        var selectedServiceName by remember { mutableStateOf("") }
        var selectedServicePrice by remember { mutableStateOf(0) }

        // Mencegah aplikasi langsung keluar saat tombol Back HP ditekan di halaman Keranjang
        if (staffScreen != "dashboard") {
            BackHandler {
                staffScreen = "dashboard"
            }
        }

        when (staffScreen) {
            "dashboard" -> {
                // Memanggil Dashboard Staf (Tanpa Parameter ManageClick)
                StaffDashboardScreen(
                    viewModel = viewModel,
                    onLogout = onLogout,
                    onServiceClick = { name, price ->
                        selectedServiceName = name
                        selectedServicePrice = price
                        staffScreen = "add" // Pindah ke halaman form keranjang
                    }
                )
            }
            "add" -> {
                // Memanggil Form Keranjang
                AddShoeScreen(
                    serviceName = selectedServiceName,
                    price = selectedServicePrice,
                    viewModel = viewModel,
                    onBack = { staffScreen = "dashboard" }
                )
            }
        }

    } else if (userEmail != null || userId == "pelanggan") {

        // =======================================================
        // TAMPILAN KHUSUS PELANGGAN
        // =======================================================
        PelangganTrackingScreen(viewModel = viewModel, onLogout = onLogout)

    } else {
        ErrorScreen()
    }
}

// ==========================================
// KUMPULAN HALAMAN TAMBAHAN
// ==========================================
@Composable
fun ErrorScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Error: Akun tidak ditemukan atau role salah.")
    }
}