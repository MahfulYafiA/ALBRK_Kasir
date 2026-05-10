package com.albrk.shoescare.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape // TAMBAHAN: Wajib diimport
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // TAMBAHAN: Wajib diimport
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale // TAMBAHAN: Wajib diimport
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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // Untuk kontrol mata password
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Gunakan warna latar belakang abu-abu terang (silver)
            .background(Color(0xFFEEEEEE))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ==========================================
        // HEADER: FOTO BULAT BUKAN KOTAK HITAM
        // ==========================================
        // UPDATE: Kita bungkus gambar kotak tadi dalam Surface bulet berbayang biar premium boss
        Surface(
            modifier = Modifier
                .size(130.dp) // Ukuran lingkaran harus sama lebar x tinggi
                .clip(CircleShape), // Wajib clip biar Surface-nya bulet
            color = Color.White, // Latar belakang putih bersih
            shadowElevation = 8.dp // Efek bayangan biar premium boss
        ) {
            Image(
                painter = painterResource(id = R.drawable.albrk),
                contentDescription = "ALBRK Logo",
                contentScale = ContentScale.Crop, // Wajib biar logo didalemnya full ga lonjong
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp) // Padding dikit biar logonya ga mentok border
                    .clip(CircleShape) // Clip image-nya juga
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ALBRK SHOESCARE",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "Premium Services Quality",
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // ==========================================
        // INPUT FIELDS DENGAN SUDUT TUMPUL (Matches image)
        // ==========================================
        // KOLOM INPUT EMAIL
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true,
            // Menambahkan leading icon biar mirip design tumpu-nya
            shape = RoundedCornerShape(12.dp) // Sudut tumpul sesuai gambar
        )

        // KOLOM INPUT PASSWORD
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true,
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

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
        }

        // ==========================================
        // TOMBOL LOGIN HITAM SOLID (Matches image "LOGIN")
        // ==========================================
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email dan Password tidak boleh kosong!"
                    return@Button
                }

                isLoading = true
                errorMessage = ""

                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            if (email.contains("staf")) {
                                onLoginClick("staff")
                            } else {
                                onLoginClick("pelanggan")
                            }
                        } else {
                            errorMessage = "Login Gagal: Cek kembali email dan password Anda."
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black, // Latar belakang tombol hitam solid
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("LOGIN", fontSize = 18.sp, fontWeight = FontWeight.Bold) // Gunakan LOGIN huruf kapital sesuai gambar boss
            }
        }
    }
}