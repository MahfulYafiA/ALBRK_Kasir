package com.albrk.shoescare.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.albrk.shoescare.data.local.entity.Shoe
import com.albrk.shoescare.ui.screen.add.AddShoeScreen
import com.albrk.shoescare.ui.screen.detail.DetailScreen
import com.albrk.shoescare.ui.screen.staff.StaffDashboardScreen
import com.albrk.shoescare.viewmodel.ShoeViewModel

@Composable
fun MyApp(
    viewModel: ShoeViewModel,
    onLogout: () -> Unit,
    onOrderListClick: () -> Unit
) {
    // Navigasi Manual (Aman tanpa Library Tambahan)
    var currentScreen by remember { mutableStateOf("dashboard") }
    var selectedShoe by remember { mutableStateOf<Shoe?>(null) }

    // Variabel penampung data untuk dikirim ke Keranjang (AddShoeScreen)
    var selectedServiceName by remember { mutableStateOf("") }
    var selectedServicePrice by remember { mutableStateOf(0) }

    // Menangani tombol "Back" di HP
    if (currentScreen != "dashboard") {
        BackHandler {
            currentScreen = "dashboard"
        }
    }

    when (currentScreen) {
        "dashboard" -> {
            Box(modifier = Modifier.fillMaxSize()) {
                // Memanggil Dashboard Staf (onManageClick dihapus)
                StaffDashboardScreen(
                    viewModel = viewModel,
                    onLogout = onLogout,
                    onServiceClick = { name, price ->
                        selectedServiceName = name
                        selectedServicePrice = price
                        currentScreen = "add"
                    }
                )

                // Tombol Melayang "Riwayat Pesanan"
                Button(
                    onClick = onOrderListClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp, end = 16.dp)
                ) {
                    Text("Riwayat Pesanan")
                }
            }
        }
        "add" -> {
            // Memanggil Keranjang (AddShoeScreen)
            AddShoeScreen(
                serviceName = selectedServiceName,
                price = selectedServicePrice,
                viewModel = viewModel,
                onBack = { currentScreen = "dashboard" }
            )
        }
        "detail" -> {
            selectedShoe?.let { shoe ->
                DetailScreen(
                    name = shoe.name,
                    price = shoe.price,
                    navigateBack = { currentScreen = "dashboard" },
                    onDeleteClick = {
                        viewModel.deleteShoe(shoe)
                        currentScreen = "dashboard"
                    }
                )
            }
        }
    }
}