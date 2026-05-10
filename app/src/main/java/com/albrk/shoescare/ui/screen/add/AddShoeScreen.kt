package com.albrk.shoescare.ui.screen.add

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albrk.shoescare.viewmodel.ShoeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShoeScreen(
    serviceName: String,
    price: Int,
    viewModel: ShoeViewModel,
    onBack: () -> Unit
) {
    val customerName by viewModel.cartCustomerName.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    var shoeBrand by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Input Pesanan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                text = "Layanan: $serviceName (Rp $price)",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = customerName,
                onValueChange = { viewModel.updateCartCustomerName(it) },
                label = { Text("Nama Pelanggan") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = shoeBrand,
                onValueChange = { shoeBrand = it },
                label = { Text("Merek / Warna Sepatu") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (customerName.isNotBlank() && shoeBrand.isNotBlank()) {
                        viewModel.addToCart(shoeBrand, serviceName, price)
                        shoeBrand = ""
                        Toast.makeText(context, "Masuk keranjang!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Lengkapi data dulu boss!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Text("Tambah ke Keranjang")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Isi Keranjang saat ini:", fontWeight = FontWeight.Bold)

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cartItems) { shoe ->
                    ListItem(
                        headlineContent = { Text(shoe.name) },
                        supportingContent = { Text("Rp ${shoe.price}") },
                        trailingContent = {
                            IconButton(onClick = { viewModel.removeFromCart(shoe) }) {
                                Text("❌", color = Color.Red)
                            }
                        }
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.checkout(customerName) {
                        Toast.makeText(context, "Berhasil Checkout!", Toast.LENGTH_LONG).show()
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = cartItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Proses Transaksi (Rp ${viewModel.getTotalPrice()})")
            }
        }
    }
}