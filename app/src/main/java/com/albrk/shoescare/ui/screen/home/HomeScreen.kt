package com.albrk.shoescare.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.albrk.shoescare.data.local.entity.Shoe
import com.albrk.shoescare.ui.component.CheckoutDialog
import com.albrk.shoescare.ui.component.ShoeItem
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    shoeList: List<Shoe>,
    cartCount: Int,
    totalPrice: Int,
    onAddClick: () -> Unit,
    onItemClick: (Shoe) -> Unit,
    onCheckoutClick: (String) -> Unit // Mengirim String (nama pelanggan)
) {
    // State untuk mengontrol kemunculan dialog checkout
    var showDialog by remember { mutableStateOf(false) }

    // Menampilkan Dialog jika state showDialog bernilai true
    if (showDialog) {
        CheckoutDialog(
            totalPrice = totalPrice,
            onDismiss = { showDialog = false },
            onConfirm = { customerName ->
                onCheckoutClick(customerName)
                showDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ALBRK SHOESCARE",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Layanan Baru"
                )
            }
        },
        bottomBar = {
            if (cartCount > 0) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$cartCount Layanan Terpilih",
                                style = MaterialTheme.typography.labelMedium
                            )
                            val formattedTotal = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                                maximumFractionDigits = 0
                            }.format(totalPrice)
                            Text(
                                text = formattedTotal,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        // Tombol Bayar sekarang memicu kemunculan dialog
                        Button(onClick = { showDialog = true }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Text(modifier = Modifier.padding(start = 8.dp), text = "Bayar")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (shoeList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada daftar layanan.\nSilakan klik tombol + untuk menambah.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
            ) {
                items(items = shoeList, key = { it.id }) { shoe ->
                    ShoeItem(
                        shoe = shoe,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }
}