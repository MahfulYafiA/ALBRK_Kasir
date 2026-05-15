package com.albrk.shoescare.ui.screen.staff

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albrk.shoescare.data.local.entity.ServiceItem
import com.albrk.shoescare.viewmodel.ShoeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageServiceScreen(viewModel: ShoeViewModel) {
    val context = LocalContext.current

    // =======================================================
    // 1. STATE MANAGEMENT UNTUK FORM INPUT
    // State ini akan mengontrol teks yang diketik pengguna.
    // Saat tombol "Edit" diklik, nilai di sini akan diisi dengan data layanan yang dipilih.
    // =======================================================
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf("") } // Jika kosong = mode Tambah, Jika terisi = mode Edit
    var isActiveState by remember { mutableStateOf(true) } // Mengingat status aktif/nonaktif saat mode Edit

    // =======================================================
    // 2. OBSERVASI DATA LAYANAN DARI FIREBASE (REALTIME)
    // =======================================================
    // Menggunakan collectAsState() agar UI otomatis me-refresh daftar layanan
    // setiap kali ada perubahan data (tambah/edit/hapus) di Firebase.
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
            // =======================================================
            // 3. AREA INPUT FORM (TAMBAH / EDIT LAYANAN)
            // =======================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Judul Form berubah otomatis tergantung mode (Tambah atau Edit)
                    Text(
                        text = if (selectedId.isEmpty()) "Tambah Layanan Baru" else "Edit Layanan",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Nama Layanan
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Layanan (contoh: Deep Clean)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input Harga Layanan (Keyboard diset khusus untuk angka)
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Harga (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tombol Simpan / Update
                    Button(
                        onClick = {
                            if (name.isNotBlank() && price.isNotBlank()) {
                                // Membuat nama file gambar standar berdasarkan nama layanan
                                // Contoh: "Deep Clean" -> "deepclean"
                                val autoImageName = name.lowercase().replace(" ", "")

                                // Menyimpan data ke Firebase melalui ViewModel
                                viewModel.addMasterService(
                                    ServiceItem(
                                        id = selectedId, // Jika id kosong, Firebase akan membuatkan key baru
                                        name = name,
                                        price = price.toIntOrNull() ?: 0,
                                        imageRes = 0,
                                        imageUri = autoImageName,
                                        isActive = isActiveState // Mempertahankan status aktif yang terakhir
                                    )
                                )

                                Toast.makeText(context, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()

                                // Reset semua isi form kembali ke kosong setelah sukses menyimpan
                                name = ""; price = ""; selectedId = ""; isActiveState = true
                            } else {
                                Toast.makeText(context, "Nama dan Harga wajib diisi!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (selectedId.isEmpty()) "Simpan" else "Update")
                    }

                    // Menampilkan tombol "Batal Edit" hanya jika sedang dalam mode Edit
                    if (selectedId.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                // Membatalkan edit dan mengosongkan form
                                name = ""; price = ""; selectedId = ""; isActiveState = true
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

            // =======================================================
            // 4. DAFTAR LAYANAN (LAZY COLUMN)
            // =======================================================
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(services) { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        // Warna Card berubah tergantung status layanan:
                        // Aktif = Putih abu-abu, Nonaktif = Merah muda
                        colors = CardDefaults.cardColors(
                            containerColor = if (service.isActive) Color(0xFFF5F5F5) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Warna teks judul meredup menjadi abu-abu jika layanan sedang nonaktif
                                Text(
                                    text = service.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (service.isActive) Color.Black else Color.Gray
                                )
                                Text(text = "Rp ${service.price}", color = Color.DarkGray)

                                // Badge (Label) Status Aktif/Nonaktif
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

                            // --- SWITCH (TOGGLE) AKTIF/NONAKTIF CEPAT ---
                            // Fitur ini memungkinkan kasir mematikan layanan tanpa harus masuk ke mode Edit
                            Switch(
                                checked = service.isActive,
                                onCheckedChange = { isChecked ->
                                    // Langsung menembak fungsi updateServiceStatus di ViewModel
                                    // agar Firebase hanya meng-update field 'isActive' tanpa menyentuh field lain.
                                    viewModel.updateServiceStatus(service.id, isChecked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF2E7D32),
                                    checkedTrackColor = Color(0xFFC8E6C9),
                                    uncheckedThumbColor = Color(0xFFC62828),
                                    uncheckedTrackColor = Color(0xFFFFCDD2)
                                )
                            )

                            Spacer(Modifier.width(8.dp))

                            // --- TOMBOL EDIT ---
                            IconButton(onClick = {
                                // Mengisi state form di atas (Bagian 1) dengan data layanan yang dipilih
                                // Ini otomatis akan mengubah tampilan form menjadi "Mode Edit"
                                name = service.name
                                price = service.price.toString()
                                selectedId = service.id
                                isActiveState = service.isActive
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Blue)
                            }

                            // --- TOMBOL HAPUS ---
                            IconButton(onClick = {
                                viewModel.deleteMasterService(service.id)
                                Toast.makeText(context, "${service.name} dihapus", Toast.LENGTH_SHORT).show()
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