package com.albrk.shoescare.ui.screen.staff

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.albrk.shoescare.viewmodel.ShoeViewModel

@Composable
fun MainScreen(
    userId: String,
    viewModel: ShoeViewModel,
    onLogout: () -> Unit
) {
    // =======================================================
    // 1. SETUP NAVIGASI (PENGATUR JALUR)
    // =======================================================
    // navController adalah "supir" yang akan mengantarkan pengguna dari satu layar ke layar lain.
    // 'remember' memastikan navigasi tidak ter-reset saat layar di-refresh.
    val navController = rememberNavController()

    // =======================================================
    // 2. OTENTIKASI & HAK AKSES (ROLE-BASED ACCESS)
    // =======================================================
    // Mengambil data pengguna yang sedang login saat ini dari server Firebase.
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email

    // Cek Hak Akses (Authorization):
    // Memastikan hanya akun dengan email staf atau ID "staff" yang boleh masuk.
    // Ini mencegah pelanggan biasa (jika ada aplikasi klien) menyusup ke dashboard.
    if (userEmail != null && userEmail.contains("staf", ignoreCase = true) || userId == "staff") {

        // =======================================================
        // 3. NAVHOST (PETA APLIKASI)
        // =======================================================
        // NavHost adalah wadah yang menyimpan semua layar (halaman) dalam aplikasi.
        // startDestination menentukan layar pertama yang terbuka saat MainScreen dipanggil.
        NavHost(
            navController = navController,
            startDestination = "staff_dashboard"
        ) {

            // --- RUTE 1: HALAMAN DASHBOARD UTAMA ---
            composable("staff_dashboard") {
                StaffDashboardScreen(
                    viewModel = viewModel,
                    onLogout = onLogout,
                    onManageServiceClick = {
                        navController.navigate("manage_service") // Pindah ke layar kelola layanan
                    },
                    onFinanceClick = {
                        navController.navigate("finance_report") // Pindah ke layar laporan
                    },
                    onProfileClick = {
                        navController.navigate("profile") // Pindah ke layar profil
                    },
                    onServiceClick = { name, price ->
                        // Aksi saat layanan diklik. Dibiarkan kosong agar tidak
                        // tidak mengganggu proses kelola antrean di dashboard staf.
                    }
                )
            }

            // --- RUTE 2: HALAMAN KELOLA LAYANAN ---
            composable("manage_service") {
                ManageServiceScreen(viewModel = viewModel)
            }

            // --- RUTE 3: HALAMAN LAPORAN KEUANGAN ---
            composable("finance_report") {
                FinanceScreen(viewModel = viewModel)
            }

            // --- RUTE 4: HALAMAN PROFIL KASIR ---
            composable("profile") {
                ProfileScreen(
                    onLogout = onLogout
                )
            }
        }

    } else {
        // =======================================================
        // 4. FALLBACK: LAYAR ERROR / DITOLAK
        // =======================================================
        // Jika yang login ternyata bukan staf (gagal validasi), tampilkan layar ini.
        ErrorScreen()
    }
}

// Komponen layar error jika akses ditolak
@Composable
fun ErrorScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Akses Ditolak: Aplikasi ini khusus untuk Staf/Kasir.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}