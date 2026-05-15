package com.albrk.shoescare.ui.screen.staff

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albrk.shoescare.R
import com.albrk.shoescare.data.local.entity.ServiceItem
import com.albrk.shoescare.data.local.entity.Transaction
import com.albrk.shoescare.utils.PdfHelper
import com.albrk.shoescare.viewmodel.ShoeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(
    viewModel: ShoeViewModel,
    onLogout: () -> Unit, // (Catatan: Fungsi ini sekarang di-handle di ProfileScreen)
    onManageServiceClick: () -> Unit,
    onFinanceClick: () -> Unit,
    onProfileClick: () -> Unit,
    onServiceClick: (String, Int) -> Unit
) {
    val context = LocalContext.current

    // =======================================================
    // 1. OBSERVASI DATA FIREBASE (REALTIME)
    // Mengubah Flow data dari ViewModel menjadi State Jetpack Compose.
    // Jika ada pesanan masuk atau data layanan diupdate, UI otomatis berubah (Recomposition).
    // =======================================================
    val transactionList by viewModel.allTransactions.collectAsState(initial = emptyList())
    val servicesFromFirebase by viewModel.allServices.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ALBRK Dashboard", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    // Tombol Profil di pojok kanan atas untuk navigasi ke pengaturan/logout
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil Kasir",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // =======================================================
        // 2. KONTEN UTAMA (LAZY COLUMN)
        // Digunakan agar layar bisa di-scroll dengan performa sangat cepat
        // =======================================================
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- BAGIAN HEADER: DAFTAR LAYANAN ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Layanan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                    Row {
                        // Tombol masuk ke Halaman Laporan Keuangan
                        TextButton(onClick = onFinanceClick) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Laporan")
                        }
                        // Tombol masuk ke Halaman Kelola Data Master
                        TextButton(onClick = onManageServiceClick) {
                            Text("Kelola All")
                        }
                    }
                }

                // LazyRow: Menampilkan daftar layanan secara horizontal (bisa digeser ke kiri/kanan)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(servicesFromFirebase) { service ->
                        ServiceCardDynamic(service) {
                            Toast.makeText(context, "Detail: ${service.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Antrean Transaksi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // =======================================================
            // 3. DAFTAR ANTREAN TRANSAKSI
            // =======================================================
            if (transactionList.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada antrean masuk.", color = Color.Gray)
                    }
                }
            } else {
                items(transactionList) { transaction ->
                    // Memanggil Card khusus untuk setiap transaksi
                    TransactionCardPremium(
                        transaction = transaction,
                        onStatusChange = { newStatus ->
                            // Update status (misal: Diajukan -> Selesai) langsung ke Firebase
                            viewModel.updateTransactionStatus(transaction.firebaseKey, newStatus)
                            Toast.makeText(context, "Status jadi: $newStatus", Toast.LENGTH_SHORT).show()
                        },
                        onPrint = {
                            // Mencetak struk PDF untuk diberikan ke pelanggan
                            PdfHelper.generateStruk(
                                context,
                                transaction.customerName,
                                transaction.serviceNames,
                                transaction.totalPrice
                            )
                            // Toast tidak ditaruh di sini lagi karena sudah ada di dalam fungsi generateStruk
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// =======================================================
// KOMPONEN: KARTU LAYANAN (HORIZONTAL)
// =======================================================
@Composable
fun ServiceCardDynamic(service: ServiceItem, onClick: () -> Unit) {
    val context = LocalContext.current

    // Logika Pemanggilan Gambar Dinamis:
    // Mencocokkan nama file di Firebase dengan nama file di folder drawable lokal Android.
    // Jika gambar tidak ditemukan (id == 0), maka gunakan logo ALBRK sebagai default.
    val imageResId = remember(service.imageUri) {
        val resName = service.imageUri?.lowercase()?.replace(" ", "")?.trim() ?: "albrk"
        val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
        if (id != 0) id else R.drawable.albrk
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier = Modifier
                    .height(110.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(12.dp)) {
                Text(service.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, maxLines = 1)
                Text(
                    text = "Rp ${service.price}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// =======================================================
// KOMPONEN: KARTU ANTREAN TRANSAKSI (VERTIKAL)
// =======================================================
@Composable
fun TransactionCardPremium(
    transaction: Transaction,
    onStatusChange: (String) -> Unit,
    onPrint: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) } // State untuk memunculkan Dropdown Status
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    val formattedDate = sdf.format(Date(transaction.date)) // Konversi milisecond ke tanggal bisa dibaca

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Info Pelanggan
                Column(Modifier.weight(1f)) {
                    Text(transaction.customerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("WA: ${transaction.customerPhone}", fontSize = 12.sp, color = Color.Gray)
                    Text("Alamat: ${transaction.address}", fontSize = 12.sp, color = Color.Gray)
                    Text("Dipesan: $formattedDate", fontSize = 11.sp, color = Color.LightGray)
                }

                // --- DROP DOWN UBAH STATUS ---
                Box {
                    // Penentuan Warna berdasarkan Status
                    val statusColor = when (transaction.status) {
                        "Diajukan" -> Color(0xFF2196F3) // Biru
                        "Diproses" -> Color(0xFFF57C00) // Orange
                        "Selesai" -> Color(0xFF388E3C)  // Hijau
                        "Dibatalkan" -> Color(0xFFD32F2F) // Merah
                        else -> Color.Gray
                    }

                    // Tampilan Label Status (Bisa Diklik)
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { showMenu = true }
                    ) {
                        Text(
                            text = transaction.status,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    // Pilihan Menu Status
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        listOf("Diajukan", "Diproses", "Selesai", "Dibatalkan").forEach { statusName ->
                            DropdownMenuItem(
                                text = { Text(statusName) },
                                onClick = {
                                    onStatusChange(statusName) // Mengirim perintah update ke Firebase
                                    showMenu = false // Tutup menu setelah diklik
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)

            // Info Pembayaran
            Text("Layanan: ${transaction.serviceNames}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "Total Bayar: Rp ${transaction.totalPrice}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = Color.Black
            )

            // Tombol Cetak PDF
            Button(
                onClick = onPrint,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Cetak Struk", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}