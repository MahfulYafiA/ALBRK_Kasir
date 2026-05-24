package com.albrk.shoescare.ui.screen.staff

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add // [PERBAIKAN] Menggunakan icon Add standar
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.albrk.shoescare.data.firebase.model.ServiceItem
import com.albrk.shoescare.viewmodel.ShoeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageServiceScreen(viewModel: ShoeViewModel) {
    val context = LocalContext.current

    // --- 1. STATE FORM INPUT ---
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf("") }
    var isActiveState by remember { mutableStateOf(true) }

    // State Baru Khusus untuk Foto Layanan
    var imageUri by remember { mutableStateOf<Uri?>(null) } // Foto lokal yang baru dipilih
    var existingImageUrl by remember { mutableStateOf("") } // URL foto lama yang sudah ada di Firebase
    var isUploading by remember { mutableStateOf(false) } // Indikator loading saat gambar dikirim

    // --- 2. LAUNCHER GALERI ---
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    // --- 3. OBSERVASI DATA FIREBASE ---
    val services by viewModel.allServices.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Layanan ALBRK", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // ================= FORM TAMBAH/EDIT =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (selectedId.isEmpty()) "Tambah Layanan Baru" else "Edit Layanan",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // --- AREA PILIH FOTO ---
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEEEEEE))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            // Menampilkan preview foto baru dari memori HP
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (existingImageUrl.isNotEmpty()) {
                            // Menampilkan foto lama dari server Firebase
                            AsyncImage(
                                model = existingImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Tampilan awal (Kosong)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // [PERBAIKAN] Menggunakan icon Add
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                                Text("Pilih Foto", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Layanan (Contoh: Deep Clean)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Harga (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- TOMBOL SIMPAN ---
                    Button(
                        onClick = {
                            if (name.isNotBlank() && price.isNotBlank()) {
                                isUploading = true
                                // Panggil fungsi Pro dari ViewModel
                                viewModel.saveServiceWithImage(
                                    id = selectedId,
                                    name = name,
                                    price = price.toIntOrNull() ?: 0,
                                    imageUri = imageUri,
                                    existingImageUrl = existingImageUrl
                                ) { isSuccess ->
                                    isUploading = false
                                    if (isSuccess) {
                                        Toast.makeText(context, "Layanan disimpan!", Toast.LENGTH_SHORT).show()
                                        // Reset Form
                                        name = ""; price = ""; selectedId = ""
                                        imageUri = null; existingImageUrl = ""
                                    } else {
                                        Toast.makeText(context, "Gagal mengupload layanan.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Nama dan Harga wajib diisi!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isUploading // Nonaktifkan tombol saat proses upload berlangsung
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text(if (selectedId.isEmpty()) "Simpan" else "Update")
                        }
                    }

                    if (selectedId.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                name = ""; price = ""; selectedId = ""; isActiveState = true
                                imageUri = null; existingImageUrl = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Batal Edit", color = Color.Red)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Daftar Produk di Toko", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // ================= DAFTAR LAYANAN =================
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(services) { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (service.isActive) Color(0xFFF5F5F5) else Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Menampilkan Thumbnail Gambar Kecil
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                            ) {
                                if (!service.imageUri.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = service.imageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = service.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (service.isActive) Color.Black else Color.Gray
                                )
                                Text(text = "Rp ${service.price}", color = Color.DarkGray)

                                Surface(
                                    color = if (service.isActive) Color(0xFFE8F5E9) else Color(0xFFFFCDD2),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = if (service.isActive) "AKTIF" else "NONAKTIF",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (service.isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                            }

                            Switch(
                                checked = service.isActive,
                                onCheckedChange = { isChecked -> viewModel.updateServiceStatus(service.id, isChecked) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF2E7D32), checkedTrackColor = Color(0xFFC8E6C9),
                                    uncheckedThumbColor = Color(0xFFC62828), uncheckedTrackColor = Color(0xFFFFCDD2)
                                )
                            )

                            IconButton(onClick = {
                                name = service.name
                                price = service.price.toString()
                                selectedId = service.id
                                isActiveState = service.isActive
                                existingImageUrl = service.imageUri ?: ""
                                imageUri = null // Reset gambar lokal saat memilih edit
                            }) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Blue) }

                            // [PERBAIKAN] Kirim service.id (String) sesuai dengan isi ViewModel
                            IconButton(onClick = {
                                viewModel.deleteMasterService(service.id) {
                                    Toast.makeText(context, "${service.name} dihapus", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}