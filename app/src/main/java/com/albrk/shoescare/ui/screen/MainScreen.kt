package com.albrk.shoescare.ui.screen.staff

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Import Firebase Auth
import com.google.firebase.auth.FirebaseAuth

// Import ViewModel
import com.albrk.shoescare.viewmodel.ShoeViewModel

@Composable
fun MainScreen(userId: String, viewModel: ShoeViewModel, onLogout: () -> Unit) {

    // Ambil data user yang sedang login
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email

    // Karena ini aplikasi KHUSUS STAF, kita hanya izinkan login staf
    if (userEmail == "staf@gmail.com" || userId == "staff") {

        // =======================================================
        // TAMPILAN DASHBOARD STAF
        // =======================================================
        StaffDashboardScreen(
            viewModel = viewModel,
            onLogout = onLogout,
            onServiceClick = { name, price ->
                // Catatan: Karena form 'add' sudah dihapus, tombol layanan
                // di dashboard staf sementara tidak mengarah ke mana-mana.
                // Fokus ke update status dan cetak struk antrean dulu.
            }
        )

    } else {
        // Jika pelanggan nyasar login ke aplikasi Staf, tampilkan error
        ErrorScreen()
    }
}

// ==========================================
// TAMPILAN JIKA BUKAN STAF YANG LOGIN
// ==========================================
@Composable
fun ErrorScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Akses Ditolak: Aplikasi ini khusus untuk Staf/Kasir.")
    }
}