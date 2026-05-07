package com.albrk.shoescare.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.albrk.shoescare.data.local.entity.Shoe
import com.albrk.shoescare.ui.screen.add.AddShoeScreen
import com.albrk.shoescare.ui.screen.detail.DetailScreen
import com.albrk.shoescare.ui.screen.home.HomeScreen
import com.albrk.shoescare.viewmodel.ShoeViewModel

@Composable
fun MyApp(viewModel: ShoeViewModel) {
    val shoeList by viewModel.allShoes.collectAsState()
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf("home") }
    var selectedShoe by remember { mutableStateOf<Shoe?>(null) }

    // Menangani tombol back agar kembali ke home jika tidak di layar utama
    if (currentScreen != "home") {
        BackHandler {
            currentScreen = "home"
        }
    }

    when (currentScreen) {
        "home" -> {
            HomeScreen(
                shoeList = shoeList,
                cartCount = viewModel.cartItems.size,
                totalPrice = viewModel.getTotalPrice(),
                onAddClick = { currentScreen = "add" },
                onItemClick = { shoe ->
                    // Menambah layanan ke keranjang belanja
                    viewModel.addToCart(shoe)
                    Toast.makeText(context, "${shoe.name} ditambahkan", Toast.LENGTH_SHORT).show()
                },
                onCheckoutClick = { customerName ->
                    // Memproses transaksi nyata ke database
                    val total = viewModel.getTotalPrice()
                    viewModel.checkout(customerName)
                    Toast.makeText(
                        context,
                        "Transaksi $customerName berhasil disimpan! Total: Rp$total",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
        "add" -> {
            AddShoeScreen(
                navigateBack = { currentScreen = "home" },
                onSaveClick = { name, price ->
                    viewModel.insertShoe(Shoe(name = name, price = price))
                    currentScreen = "home"
                }
            )
        }
        "detail" -> {
            selectedShoe?.let { shoe ->
                DetailScreen(
                    name = shoe.name,
                    price = shoe.price,
                    navigateBack = { currentScreen = "home" },
                    onDeleteClick = {
                        viewModel.deleteShoe(shoe)
                        currentScreen = "home"
                    }
                )
            }
        }
    }
}