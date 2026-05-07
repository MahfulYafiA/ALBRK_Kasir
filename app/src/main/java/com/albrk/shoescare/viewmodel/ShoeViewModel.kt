package com.albrk.shoescare.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.albrk.shoescare.data.local.entity.Shoe
import com.albrk.shoescare.data.local.entity.Transaction
import com.albrk.shoescare.data.repository.ShoeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoeViewModel(private val repository: ShoeRepository) : ViewModel() {

    // 1. DATA LAYANAN (MASTER DATA)
    val allShoes: StateFlow<List<Shoe>> = repository.allShoes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. DATA TRANSAKSI (RIWAYAT KASIR)
    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 3. LOGIKA KERANJANG (TEMPORARY)
    var cartItems = mutableStateListOf<Shoe>()
        private set

    fun addToCart(shoe: Shoe) {
        cartItems.add(shoe)
    }

    fun removeFromCart(shoe: Shoe) {
        cartItems.remove(shoe)
    }

    fun clearCart() {
        cartItems.clear()
    }

    fun getTotalPrice(): Int {
        return cartItems.sumOf { it.price }
    }

    // 4. LOGIKA OPERASI DATABASE
    fun insertShoe(shoe: Shoe) = viewModelScope.launch {
        repository.insertShoe(shoe)
    }

    fun deleteShoe(shoe: Shoe) = viewModelScope.launch {
        repository.deleteShoe(shoe)
    }

    // FUNGSI CHECKOUT PROFESIONAL
    fun checkout(customerName: String) = viewModelScope.launch {
        if (cartItems.isNotEmpty()) {
            val services = cartItems.joinToString(", ") { it.name }
            val total = getTotalPrice()

            val newTransaction = Transaction(
                customerName = customerName,
                serviceNames = services,
                totalPrice = total
            )

            repository.insertTransaction(newTransaction)
            clearCart() // Kosongkan keranjang setelah berhasil disimpan ke DB
        }
    }
}

class ShoeViewModelFactory(private val repository: ShoeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}