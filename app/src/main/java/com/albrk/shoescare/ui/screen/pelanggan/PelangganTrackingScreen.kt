package com.albrk.shoescare.ui.screen.pelanggan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albrk.shoescare.data.local.entity.Transaction
import com.albrk.shoescare.viewmodel.ShoeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PelangganTrackingScreen(viewModel: ShoeViewModel, onLogout: () -> Unit) {
    var searchName by remember { mutableStateOf("") }
    val allTransactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    val filteredTransactions = allTransactions.filter {
        it.customerName.contains(searchName, ignoreCase = true) && searchName.isNotBlank()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Lacak Pesanan", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Keluar", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            OutlinedTextField(
                value = searchName,
                onValueChange = { searchName = it },
                placeholder = { Text("Masukkan Nama Anda...") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (searchName.isEmpty()) {
                InfoPlaceholder("Gunakan fitur ini untuk memantau status cuci sepatu Anda secara realtime.")
            } else if (filteredTransactions.isEmpty()) {
                InfoPlaceholder("Nama '$searchName' tidak ditemukan.", isError = true)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredTransactions) { transaction ->
                        TrackingCardPremium(transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingCardPremium(transaction: Transaction) {
    // Penentuan step progres (1: Diajukan, 2: Diproses, 3: Selesai)
    val currentStep = when (transaction.status) {
        "Diajukan" -> 1
        "Diproses" -> 2
        "Selesai" -> 3
        else -> 0 // Dibatalkan atau lainnya
    }

    val isCancelled = transaction.status == "Dibatalkan"

    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    val formattedDate = sdf.format(Date(transaction.date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Halo, ${transaction.customerName}", fontSize = 12.sp, color = Color.Gray)
                    Text(text = formattedDate, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                }

                Surface(
                    color = when(transaction.status) {
                        "Selesai" -> Color(0xFFE8F5E9)
                        "Dibatalkan" -> Color(0xFFFFEBEE)
                        else -> Color(0xFFFFF3E0)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = transaction.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(transaction.status) {
                            "Selesai" -> Color(0xFF2E7D32)
                            "Dibatalkan" -> Color(0xFFC62828)
                            else -> Color(0xFFEF6C00)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = transaction.serviceNames, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Total Bayar: ", fontSize = 14.sp, color = Color.DarkGray)
                Text(
                    text = "Rp ${transaction.totalPrice}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LOGIKA VISUAL TRACKER
            if (isCancelled) {
                Text(
                    text = "Mohon maaf, pesanan Anda dibatalkan.",
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    StatusPoint("Diajukan", currentStep >= 1)
                    StatusLine(currentStep >= 2)
                    StatusPoint("Diproses", currentStep >= 2)
                    StatusLine(currentStep >= 3)
                    StatusPoint("Selesai", currentStep >= 3)
                }
            }
        }
    }
}

@Composable
fun RowScope.StatusPoint(label: String, isActive: Boolean) {
    val color = if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun RowScope.StatusLine(isActive: Boolean) {
    val color = if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray
    Box(modifier = Modifier.weight(1f).height(2.dp).background(color).padding(horizontal = 4.dp))
}

@Composable
fun InfoPlaceholder(text: String, isError: Boolean = false) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = if (isError) Color.Red else Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(40.dp)
        )
    }
}