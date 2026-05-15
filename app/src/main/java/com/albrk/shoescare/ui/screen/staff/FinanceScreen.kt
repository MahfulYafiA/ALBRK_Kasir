package com.albrk.shoescare.ui.screen.staff

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share // Menggunakan ikon Share karena sangat aman dan didukung semua versi Android
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albrk.shoescare.utils.PdfHelper
import com.albrk.shoescare.viewmodel.ShoeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: ShoeViewModel) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")) // Format tanggal standar Indonesia

    // =======================================================
    // 1. PENGATURAN WAKTU DEFAULT (HARI INI)
    // =======================================================
    // Mengatur waktu awal hari (00:00:00) untuk filter default
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Mengatur waktu akhir hari (23:59:59) untuk filter default
    val todayEnd = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    // State untuk menyimpan tanggal filter yang dipilih user.
    // Diinisialisasi langsung ke rentang hari ini agar saat dibuka langsung muncul data hari ini.
    var startDate by remember { mutableStateOf<Long?>(todayStart) }
    var endDate by remember { mutableStateOf<Long?>(todayEnd) }

    // =======================================================
    // 2. OBSERVASI DATA FIREBASE (REALTIME)
    // =======================================================
    // collectAsState() sangat penting! Ini yang mengubah aliran data (Flow) dari Firebase
    // menjadi State UI. Jika data di Firebase berubah, UI akan otomatis menggambar ulang (Recomposition).
    val transactionList by viewModel.allTransactions.collectAsState(initial = emptyList())

    // =======================================================
    // 3. LOGIKA FILTERING DATA (LOKAL)
    // =======================================================
    // remember di sini akan mengkalkulasi ulang data HANYA JIKA transactionList, startDate, atau endDate berubah.
    // Ini menghemat memori (performa aplikasi jadi sangat cepat).
    val filteredTransactions = remember(transactionList, startDate, endDate) {
        transactionList.filter { transaction ->
            val isSelesai = transaction.status == "Selesai" // Laporan uang masuk HANYA dari pesanan yang sudah selesai
            val isAfterStart = startDate?.let { transaction.date >= it } ?: true
            val isBeforeEnd = endDate?.let { transaction.date <= it } ?: true

            isSelesai && isAfterStart && isBeforeEnd // Harus memenuhi ketiga syarat ini
        }.sortedByDescending { it.date } // Urutkan dari yang paling baru
    }

    // Menghitung total omzet (uang masuk) secara otomatis dari data yang sudah difilter
    val totalIncome = filteredTransactions.sumOf { it.totalPrice }

    // =======================================================
    // 4. PEMBUATAN UI (TAMPILAN)
    // =======================================================
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Keuangan", fontWeight = FontWeight.Bold) },
                actions = {
                    // Tombol untuk memicu pembuatan PDF
                    IconButton(onClick = {
                        if (filteredTransactions.isNotEmpty()) {
                            // Memanggil fungsi dari PdfHelper dan mengirimkan data yang sedang tampil di layar
                            PdfHelper.generateLaporanKeuangan(
                                context = context,
                                transactions = filteredTransactions,
                                totalIncome = totalIncome,
                                periodStart = startDate?.let { sdf.format(Date(it)) } ?: "Awal",
                                periodEnd = endDate?.let { sdf.format(Date(it)) } ?: "Sekarang"
                            )
                            Toast.makeText(context, "Mencetak PDF...", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Tidak ada data untuk dicetak", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Cetak PDF", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            // --- HEADER FILTER TANGGAL ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Filter Rentang Tanggal", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        // Tombol Pilih Tanggal Mulai (Start Date)
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance()
                                startDate?.let { cal.timeInMillis = it }
                                DatePickerDialog(context, { _, y, m, d ->
                                    val result = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
                                    startDate = result.timeInMillis
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(startDate?.let { sdf.format(Date(it)) } ?: "Dari", fontSize = 12.sp)
                        }

                        Text(" - ", Modifier.padding(horizontal = 4.dp))

                        // Tombol Pilih Tanggal Akhir (End Date)
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance()
                                endDate?.let { cal.timeInMillis = it }
                                DatePickerDialog(context, { _, y, m, d ->
                                    val result = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }
                                    endDate = result.timeInMillis
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(endDate?.let { sdf.format(Date(it)) } ?: "Sampai", fontSize = 12.sp)
                        }

                        // Tombol Reset Filter (Menampilkan semua data tanpa batas waktu)
                        IconButton(onClick = { startDate = null; endDate = null }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Reset")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- KARTU TOTAL PENDAPATAN ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Total Pendapatan", color = Color.White, fontSize = 12.sp)
                        Text("Rp $totalIncome", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text("${filteredTransactions.size} Transaksi", color = Color.LightGray, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Riwayat Transaksi Selesai", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            // --- DAFTAR TRANSAKSI (LAZY COLUMN) ---
            // LazyColumn sangat efisien karena hanya me-render item yang terlihat di layar HP
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                if (filteredTransactions.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("Tidak ada transaksi di periode ini.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(filteredTransactions) { transaction ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                        ) {
                            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(transaction.customerName, fontWeight = FontWeight.Bold)
                                    Text(transaction.serviceNames, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                                    Text(sdf.format(Date(transaction.date)), fontSize = 10.sp, color = Color.LightGray)
                                }
                                Text("Rp ${transaction.totalPrice}", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                            }
                        }
                    }
                }
            }
        }
    }
}