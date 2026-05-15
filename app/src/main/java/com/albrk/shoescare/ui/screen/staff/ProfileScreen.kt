package com.albrk.shoescare.ui.screen.staff

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albrk.shoescare.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    // =======================================================
    // 1. INISIALISASI CONTEXT & FIREBASE AUTH
    // =======================================================
    val context = LocalContext.current // Digunakan untuk memunculkan pesan pop-up (Toast)

    // Mengambil data user yang sedang login saat ini langsung dari server Firebase
    val user = FirebaseAuth.getInstance().currentUser

    // =======================================================
    // 2. STATE MANAGEMENT (MANAJEMEN STATUS UI)
    // =======================================================
    var kasirName by remember { mutableStateOf("Kasir ALBRK") }

    // Fitur Ganti Foto (Saat ini diset untuk 1 gambar bawaan)
    val avatarOptions = listOf(R.drawable.albrk)
    var avatarIndex by remember { mutableStateOf(0) }

    // State untuk form ganti kata sandi
    var newPassword by remember { mutableStateOf("") }

    // Indikator apakah sistem sedang memproses pergantian sandi (mencegah spam klik)
    var isChangingPassword by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pengaturan Profil", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // =======================================================
            // 3. AREA FOTO PROFIL
            // =======================================================
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape) // Membuat foto menjadi bulat sempurna
                    .clickable {
                        // Logika Ganti Foto: Menggunakan modulo (%) agar index berputar kembali ke 0
                        // jika sudah mencapai akhir list gambar.
                        avatarIndex = (avatarIndex + 1) % avatarOptions.size
                        Toast.makeText(context, "Klik ganti foto (Tambahkan aset lain nanti)", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Image(
                    painter = painterResource(id = avatarOptions[avatarIndex]),
                    contentDescription = "Foto Kasir",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // Memotong gambar agar tidak gepeng
                )

                // Overlay kotak hitam transparan bertuliskan "UBAH" di bagian bawah foto
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "UBAH",
                        color = Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(text = kasirName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = "Status: Staff / Kasir Aktif", fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(32.dp))

            // =======================================================
            // 4. FORM INFORMASI DATA DIRI
            // =======================================================
            OutlinedTextField(
                value = kasirName,
                onValueChange = { kasirName = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Field Email dikunci (Read-Only) karena email terikat langsung dengan kredensial Firebase
            OutlinedTextField(
                // Mengambil email asli dari Firebase, jika null maka gunakan teks default
                value = user?.email ?: "staf@gmail.com",
                onValueChange = {},
                label = { Text("Email (Hanya Baca)") },
                enabled = false, // KUNCI: Membuat field tidak bisa diketik
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider() // Garis pemisah visual
            Spacer(Modifier.height(24.dp))

            // =======================================================
            // 5. FITUR GANTI KATA SANDI (FIREBASE AUTHENTICATION)
            // =======================================================
            Text(
                text = "Keamanan Akun",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Kata Sandi Baru") },
                visualTransformation = PasswordVisualTransformation(), // Menyamarkan teks jadi titik-titik bulat
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    // Validasi lokal: Firebase mengharuskan password minimal 6 karakter
                    if (newPassword.length >= 6) {
                        isChangingPassword = true // Mengubah state tombol menjadi "Menyimpan..."

                        // Memanggil API Firebase untuk mengupdate password pengguna saat ini
                        user?.updatePassword(newPassword)
                            ?.addOnCompleteListener { task ->
                                isChangingPassword = false
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Sandi berhasil diubah!", Toast.LENGTH_SHORT).show()
                                    newPassword = "" // Kosongkan field jika sukses
                                } else {
                                    // Firebase memiliki fitur keamanan: jika sesi login sudah terlalu lama (stale token),
                                    // Firebase akan menolak ganti sandi demi keamanan. User harus relogin.
                                    Toast.makeText(context, "Gagal: Harus login ulang dulu", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Sandi minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                // Tombol hanya bisa diklik jika input tidak kosong DAN tidak sedang proses loading
                enabled = newPassword.isNotEmpty() && !isChangingPassword,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isChangingPassword) "Menyimpan..." else "Perbarui Sandi")
            }

            Spacer(Modifier.weight(1f)) // Mendorong tombol logout mentok ke bagian bawah layar

            // =======================================================
            // 6. TOMBOL LOGOUT
            // =======================================================
            Button(
                // Memanggil fungsi onLogout() yang di-passing dari MainScreen untuk mengatur navigasi
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), // Merah peringatan
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout / Keluar", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}