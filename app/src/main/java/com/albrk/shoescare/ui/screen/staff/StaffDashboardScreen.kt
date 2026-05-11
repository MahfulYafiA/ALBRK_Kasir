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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albrk.shoescare.R
import com.albrk.shoescare.data.local.entity.Transaction
import com.albrk.shoescare.utils.PdfHelper
import com.albrk.shoescare.viewmodel.ShoeViewModel
import java.text.SimpleDateFormat
import java.util.*

// Model sederhana untuk data manual
data class ServiceItemManual(val name: String, val price: Int, val imageRes: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(
    viewModel: ShoeViewModel,
    onLogout: () -> Unit,
    onServiceClick: (String, Int) -> Unit
) {
    val context = LocalContext.current
    val transactionList by viewModel.allTransactions.collectAsState(initial = emptyList())

    // DAFTAR LAYANAN MANUAL
    val services = listOf(
        ServiceItemManual("Deep Clean", 25000, R.drawable.deepclean),
        ServiceItemManual("Fast Clean", 20000, R.drawable.fastclean),
        ServiceItemManual("Unyellowing", 30000, R.drawable.unyellowing),
        ServiceItemManual("Repaint", 35000, R.drawable.repaint)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Staf Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Layanan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(services) { service ->
                        ServiceCardManual(service) { onServiceClick(service.name, service.price) }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Antrean Transaksi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            items(transactionList) { transaction ->
                TransactionCardPremium(
                    transaction = transaction,
                    onStatusChange = { newStatus ->
                        viewModel.updateTransactionStatus(transaction.firebaseKey, newStatus)
                        Toast.makeText(context, "Status jadi $newStatus", Toast.LENGTH_SHORT).show()
                    },
                    onPrint = {
                        PdfHelper.generateStruk(context, transaction.customerName, transaction.serviceNames, transaction.totalPrice)
                        Toast.makeText(context, "Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
                    }
                )
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

    // FORMAT TANGGAL
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    val formattedDate = sdf.format(Date(transaction.date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(transaction.customerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    // MENAMPILKAN TANGGAL
                    Text(
                        text = "Dipesan: $formattedDate",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // BAGIAN STATUS
                Box {
                    val statusColor = when (transaction.status) {
                        "Diajukan" -> Color(0xFF2196F3)
                        "Diproses" -> Color(0xFFF57C00)
                        "Selesai" -> Color(0xFF388E3C)
                        "Dibatalkan" -> Color(0xFFD32F2F)
                        else -> Color.Gray
                    }

                    Text(
                        text = transaction.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { showMenu = true }
                            .padding(4.dp)
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
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

            Spacer(modifier = Modifier.height(12.dp))

            Text("Layanan: ${transaction.serviceNames}", fontSize = 13.sp, color = Color.DarkGray)
            Text("Total: Rp ${transaction.totalPrice}", fontWeight = FontWeight.ExtraBold, color = Color.Black)

            Button(
                onClick = onPrint,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Cetak Struk")
            }
        }
    }
}

@Composable
fun ServiceCardManual(service: ServiceItemManual, onClick: () -> Unit) {
    Card(modifier = Modifier.width(150.dp).clickable { onClick() }) {
        Column {
            Image(
                painter = painterResource(service.imageRes),
                contentDescription = null,
                modifier = Modifier.height(100.dp).fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(8.dp)) {
                Text(service.name, fontWeight = FontWeight.Bold)
                Text("Rp ${service.price}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
        }
    }
}