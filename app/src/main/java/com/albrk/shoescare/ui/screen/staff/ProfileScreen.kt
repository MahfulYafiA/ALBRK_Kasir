package com.albrk.shoescare.ui.screen.staff

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateBack: (() -> Unit)? = null // Opsional: Berjaga-jaga jika Admin butuh tombol kembali
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val uid = user?.uid ?: ""

    // --- Referensi Database & Storage Khusus Admin ---
    val adminRef = FirebaseDatabase.getInstance("https://albrk-shoescare-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("admins").child(uid)
    val storageRef = FirebaseStorage.getInstance().reference.child("admin_profiles/$uid.jpg")

    // --- State Data Profil (Sama Persis dengan Pelanggan) ---
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var photoUrl by remember { mutableStateOf("") }

    // --- State Keamanan ---
    var newPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // --- Launcher Galeri ---
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri ->
            isLoading = true
            Toast.makeText(context, "Mengunggah foto...", Toast.LENGTH_SHORT).show()
            storageRef.putFile(imageUri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val url = downloadUrl.toString()
                    adminRef.child("photoUrl").setValue(url)
                    photoUrl = url
                    isLoading = false
                    Toast.makeText(context, "Foto diperbarui!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                isLoading = false
                Toast.makeText(context, "Gagal unggah foto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Ambil Data Admin Saat Layar Dibuka ---
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            adminRef.get().addOnSuccessListener { snapshot ->
                name = snapshot.child("name").getValue(String::class.java) ?: ""
                phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                address = snapshot.child("address").getValue(String::class.java) ?: ""
                photoUrl = snapshot.child("photoUrl").getValue(String::class.java) ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kelola Profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ================= FOTO PROFIL (PENSIL BULAT UTUH) =================
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clickable(enabled = !isLoading) { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                // Box Internal (Area Foto)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp))
                    }
                }

                // Lencana Ikon Pensil (Di luar lingkaran agar tidak terpotong)
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp).offset(x = 4.dp, y = 4.dp),
                        tonalElevation = 4.dp
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ================= FORM INFORMASI UMUM =================
            Text("Informasi Umum", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("Nomor WhatsApp") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address, onValueChange = { address = it },
                label = { Text("Alamat Default") }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ================= FORM AKUN & KEAMANAN =================
            Text("Akun & Keamanan", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email Akun") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newPassword, onValueChange = { newPassword = it },
                label = { Text("Sandi Baru (Kosongkan jika tidak diubah)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ================= TOMBOL SIMPAN =================
            Button(
                onClick = {
                    isLoading = true
                    // 1. Simpan Data Teks ke Firebase Realtime Database
                    val adminMap = mapOf(
                        "name" to name, "phone" to phone,
                        "address" to address, "photoUrl" to photoUrl
                    )
                    adminRef.updateChildren(adminMap).addOnCompleteListener {
                        // 2. Ganti Email (Jika ada perubahan)
                        if (email.isNotEmpty() && email != user?.email) {
                            user?.verifyBeforeUpdateEmail(email)
                        }
                        // 3. Ganti Sandi (Jika kolom diisi)
                        if (newPassword.isNotEmpty() && newPassword.length >= 6) {
                            user?.updatePassword(newPassword)
                        }

                        isLoading = false
                        Toast.makeText(context, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        newPassword = "" // Kosongkan field sandi setelah sukses
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Simpan Perubahan")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= TOMBOL LOGOUT =================
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
            ) {
                Text("Keluar dari Akun")
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}