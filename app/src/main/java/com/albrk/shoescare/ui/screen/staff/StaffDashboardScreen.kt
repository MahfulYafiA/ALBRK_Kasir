package com.albrk.shoescare.ui.screen.staff

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albrk.shoescare.R
import com.albrk.shoescare.data.firebase.model.ServiceItem
import com.albrk.shoescare.data.firebase.model.Transaction
import com.albrk.shoescare.utils.PdfHelper
import com.albrk.shoescare.viewmodel.ShoeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(
    viewModel: ShoeViewModel,
    onLogout: () -> Unit,
    onManageServiceClick: () -> Unit,
    onFinanceClick: () -> Unit,
    onProfileClick: () -> Unit,
    onServiceClick: (String, Int) -> Unit
) {
    val context = LocalContext.current

    // Observasi Data Firebase
    val transactionList by viewModel.allTransactions.collectAsState(initial = emptyList())
    val servicesFromFirebase by viewModel.allServices.collectAsState(initial = emptyList())

    // State untuk Mengontrol Dialog Form Pesanan Baru
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ALBRK Dashboard", fontWeight = FontWeight.ExtraBold) },
                actions = {
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
        },
        // 1. TOMBOL MELAYANG UNTUK BUAT PESANAN BARU (KASIR CO)
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pesanan")
            }
        }
    ) { paddingValues ->

        // 2. DIALOG POP-UP: FORM INPUT TRANSAKSI BARU OLEH KASIR
        if (showCreateDialog) {
            CreateOrderDialog(
                availableServices = servicesFromFirebase.filter { it.isActive },
                onDismiss = { showCreateDialog = false },
                onConfirm = { name, phone, address, services, total ->
                    viewModel.submitBooking(name, phone, address, services, total)
                    Toast.makeText(context, "Pesanan Berhasil Dibuat!", Toast.LENGTH_SHORT).show()
                    showCreateDialog = false
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Header & Katalog Layanan
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
                        TextButton(onClick = onFinanceClick) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Laporan")
                        }
                        TextButton(onClick = onManageServiceClick) {
                            Text("Kelola All")
                        }
                    }
                }

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

            // Daftar Antrean Transaksi
            if (transactionList.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada antrean masuk.", color = Color.Gray)
                    }
                }
            } else {
                items(transactionList) { transaction ->
                    TransactionCardPremium(
                        transaction = transaction,
                        onStatusChange = { newStatus ->
                            viewModel.updateTransactionStatus(transaction.firebaseKey, newStatus)
                            Toast.makeText(context, "Status jadi: $newStatus", Toast.LENGTH_SHORT).show()
                        },
                        onPrint = {
                            PdfHelper.generateStruk(
                                context,
                                transaction.customerName,
                                transaction.serviceNames,
                                transaction.totalPrice
                            )
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// =======================================================
// DIALOG COMPONENT: FORM INPUT TRANSAKSI BARU (WALK-IN/OFFLINE)
// =======================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderDialog(
    availableServices: List<ServiceItem>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String, services: String, total: Int) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var selectedServices by remember { mutableStateOf(setOf<ServiceItem>()) }

    val dropOffOptions = listOf("Antar Sendiri", "Dijemput Kurir")
    var selectedDropOff by remember { mutableStateOf(dropOffOptions[0]) }
    val returnOptions = listOf("Ambil Sendiri", "Diantar Kurir")
    var selectedReturn by remember { mutableStateOf(returnOptions[0]) }

    val requiresAddress = selectedDropOff == "Dijemput Kurir" || selectedReturn == "Diantar Kurir"
    val grandTotal = selectedServices.sumOf { it.price }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (customerName.isBlank() || customerPhone.isBlank() || (requiresAddress && customerAddress.isBlank())) {
                        return@Button
                    }
                    if (selectedServices.isEmpty()) {
                        return@Button
                    }
                    val serviceNamesString = selectedServices.joinToString(", ") { it.name }
                    val finalAddress = if (requiresAddress) "[$selectedDropOff & $selectedReturn] - $customerAddress" else "[Walk-in / Offline Toko]"

                    onConfirm(customerName, customerPhone, finalAddress, serviceNamesString, grandTotal)
                }
            ) {
                Text("Buat Nota")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        title = { Text("Input Transaksi Baru", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Nama Pelanggan") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { if (it.all { c -> c.isDigit() }) customerPhone = it },
                    label = { Text("No. WhatsApp (Untuk Lacak)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Opsi Penyerahan & Pengembalian
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Penyerahan:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                        dropOffOptions.forEach { method ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedDropOff = method }) {
                                RadioButton(selected = selectedDropOff == method, onClick = { selectedDropOff = method })
                                Text(text = method, fontSize = 11.sp)
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pengembalian:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                        returnOptions.forEach { method ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedReturn = method }) {
                                RadioButton(selected = selectedReturn == method, onClick = { selectedReturn = method })
                                Text(text = method, fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (requiresAddress) {
                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        label = { Text("Alamat Pengiriman/Jemput") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Pilih Layanan Sepatu (Bisa > 1):", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                // List Layanan yang bisa dicentang
                availableServices.forEach { service ->
                    val isChecked = selectedServices.contains(service)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedServices = if (isChecked) selectedServices - service else selectedServices + service
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                selectedServices = if (isChecked) selectedServices - service else selectedServices + service
                            }
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                            Text(service.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Rp ${service.price}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Tagihan:", fontWeight = FontWeight.Bold)
                    Text("Rp $grandTotal", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    )
}

// =======================================================
// KOMPONEN DATA LAIN (TETAP SAMA SEPERTI ASLI)
// =======================================================
@Composable
fun ServiceCardDynamic(service: ServiceItem, onClick: () -> Unit) {
    val context = LocalContext.current
    val imageResId = remember(service.imageUri) {
        val resName = service.imageUri?.lowercase()?.replace(" ", "")?.trim() ?: "albrk"
        val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
        if (id != 0) id else R.drawable.albrk
    }

    Card(
        modifier = Modifier.width(160.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier = Modifier.height(110.dp).fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(12.dp)) {
                Text(service.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, maxLines = 1)
                Text(text = "Rp ${service.price}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun TransactionCardPremium(
    transaction: Transaction,
    onStatusChange: (String) -> Unit,
    onPrint: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    val formattedDate = sdf.format(Date(transaction.date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(transaction.customerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("WA: ${transaction.customerPhone}", fontSize = 12.sp, color = Color.Gray)
                    Text("Alamat: ${transaction.address}", fontSize = 12.sp, color = Color.Gray)
                    Text("Dipesan: $formattedDate", fontSize = 11.sp, color = Color.LightGray)
                }

                Box {
                    val statusColor = when (transaction.status) {
                        "Diajukan" -> Color(0xFF2196F3)
                        "Diproses" -> Color(0xFFF57C00)
                        "Selesai" -> Color(0xFF388E3C)
                        "Dibatalkan" -> Color(0xFFD32F2F)
                        else -> Color.Gray
                    }

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

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        listOf("Diajukan", "Diproses", "Selesai", "Dibatalkan").forEach { statusName ->
                            DropdownMenuItem(
                                text = { Text(statusName) },
                                onClick = {
                                    onStatusChange(statusName)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)

            Text("Layanan: ${transaction.serviceNames}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = "Total Bayar: Rp ${transaction.totalPrice}", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.Black)

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