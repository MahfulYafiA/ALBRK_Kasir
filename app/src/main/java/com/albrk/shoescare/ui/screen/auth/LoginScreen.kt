package com.albrk.shoescare.ui.screen.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albrk.shoescare.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(onLoginClick: (String) -> Unit) {
    // ==========================================
    // 1. STATE MANAGEMENT (MANAJEMEN STATUS UI)
    // Ingat: 'remember' digunakan agar data tidak hilang saat UI direfresh (Recomposition)
    // ==========================================
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") } // Hanya dipakai saat mode daftar
    var passwordVisible by remember { mutableStateOf(false) } // Toggle mata password

    // State penentu mode halaman: true = Halaman Login, false = Halaman Daftar
    var isLoginMode by remember { mutableStateOf(true) }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // Menampilkan efek loading
    val context = LocalContext.current // Mengambil context untuk Toast

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ==========================================
        // 2. HEADER: LOGO DAN JUDUL APLIKASI
        // ==========================================
        Surface(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Image(
                painter = painterResource(id = R.drawable.albrk),
                contentDescription = "ALBRK Logo",
                contentScale = ContentScale.Crop, // Memotong gambar agar pas di dalam lingkaran
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ALBRK KASIR",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black
        )

        Text(
            text = if (isLoginMode) "Masuk ke Dashboard Staf" else "Daftar Akun Staf Baru",
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // ==========================================
        // 3. INPUT FIELDS (KOLOM ISIAN)
        // ==========================================
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Staf", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true,
            // Fitur untuk melihat/menyembunyikan teks password
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(
                        text = if (passwordVisible) "Tutup" else "Lihat",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
        )

        // Form Konfirmasi Password (Hanya dimunculkan oleh Compose jika isLoginMode == false)
        if (!isLoginMode) {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Konfirmasi Password", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation()
            )
        }

        // Menampilkan pesan error berwarna merah jika ada
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
        }

        // ==========================================
        // 4. TOMBOL UTAMA & LOGIKA FIREBASE AUTH
        // ==========================================
        Button(
            onClick = {
                // Validasi dasar: Mencegah form dikirim kosong
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email dan Password tidak boleh kosong!"
                    return@Button
                }

                // Validasi Keamanan Sistem: Membatasi hak akses registrasi khusus staf
                if (!email.contains("staf", ignoreCase = true)) {
                    errorMessage = "Gunakan email berakhiran 'staf' (Cth: mahful.staf@gmail.com)"
                    return@Button
                }

                isLoading = true // Memunculkan animasi muter (loading)
                errorMessage = ""

                val auth = FirebaseAuth.getInstance()

                if (isLoginMode) {
                    // --- LOGIKA LOGIN FIREBASE ---
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Login Staf Berhasil", Toast.LENGTH_SHORT).show()
                                // Melempar User ID (UID) ke navigasi utama agar masuk ke Dashboard
                                onLoginClick(auth.currentUser?.uid ?: "staff")
                            } else {
                                errorMessage = "Login Gagal: Cek kembali email dan password Anda."
                            }
                        }
                } else {
                    // --- LOGIKA DAFTAR (REGISTER) FIREBASE ---
                    if (password != confirmPassword) {
                        isLoading = false
                        errorMessage = "Password tidak cocok!"
                        return@Button
                    }
                    if (password.length < 6) {
                        isLoading = false
                        errorMessage = "Password minimal 6 karakter!"
                        return@Button
                    }

                    // Membuat akun baru ke server Firebase
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Akun Staf berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                onLoginClick(auth.currentUser?.uid ?: "staff")
                            } else {
                                errorMessage = "Pendaftaran gagal: ${task.exception?.message}"
                            }
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading // Mematikan tombol agar tidak di-spam klik saat loading
        ) {
            // Jika sedang memproses data, tampilkan loading. Jika tidak, tampilkan teks.
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (isLoginMode) "LOGIN STAF" else "DAFTAR STAF",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ==========================================
        // 5. TEKS PINDAH MODE (TOGGLE LOGIN/DAFTAR)
        // ==========================================
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isLoginMode) "Staf baru? " else "Sudah punya akun staf? ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = if (isLoginMode) "Daftar di sini" else "Login di sini",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    // Mengubah state, yang memicu Compose untuk menggambar ulang UI
                    isLoginMode = !isLoginMode
                    errorMessage = "" // Mereset pesan error saat ganti mode
                }
            )
        }
    }
}