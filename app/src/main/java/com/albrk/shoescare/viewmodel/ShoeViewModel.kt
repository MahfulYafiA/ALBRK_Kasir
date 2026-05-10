package com.albrk.shoescare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.albrk.shoescare.data.local.entity.ServiceItem // Tambahan import
import com.albrk.shoescare.data.local.entity.Shoe
import com.albrk.shoescare.data.local.entity.Transaction
import com.albrk.shoescare.data.repository.ShoeRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShoeViewModel(private val repository: ShoeRepository) : ViewModel() {

    private val db = FirebaseDatabase.getInstance("https://albrk-shoescare-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val shoesRef = db.getReference("shoes")
    private val transactionsRef = db.getReference("transactions")

    private val _allShoes = MutableStateFlow<List<Shoe>>(emptyList())
    val allShoes: StateFlow<List<Shoe>> = _allShoes

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: StateFlow<List<Transaction>> = _allTransactions

    // State untuk Keranjang Belanja (Multi-item)
    private val _cartItems = MutableStateFlow<List<Shoe>>(emptyList())
    val cartItems: StateFlow<List<Shoe>> = _cartItems

    private val _cartCustomerName = MutableStateFlow("")
    val cartCustomerName: StateFlow<String> = _cartCustomerName

    // --- STATE UNTUK MENU LAYANAN DINAMIS (ROOM) ---
    private val _allServices = MutableStateFlow<List<ServiceItem>>(emptyList())
    val allServices: StateFlow<List<ServiceItem>> = _allServices

    fun updateCartCustomerName(name: String) {
        _cartCustomerName.value = name
    }

    init {
        // Ambil data MENU LAYANAN dari Room Database Lokal
        viewModelScope.launch {
            repository.allServiceItems.collect { services ->
                _allServices.value = services
            }
        }

        // Listener Firebase untuk Katalog Layanan (Lama)
        shoesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Shoe>()
                for (data in snapshot.children) {
                    val shoe = data.getValue(Shoe::class.java)
                    if (shoe != null) list.add(shoe)
                }
                _allShoes.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Listener Firebase untuk Riwayat Transaksi (Realtime)
        transactionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Transaction>()
                for (data in snapshot.children) {
                    val transaction = data.getValue(Transaction::class.java)
                    if (transaction != null) {
                        // Tempelkan key Firebase ke objek Transaction
                        val trxWithKey = transaction.copy(firebaseKey = data.key ?: "")
                        list.add(trxWithKey)
                    }
                }
                // Urutkan berdasarkan tanggal terbaru
                _allTransactions.value = list.sortedByDescending { it.date }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // --- FUNGSI KELOLA LAYANAN (ROOM) ---
    fun addService(service: ServiceItem) {
        viewModelScope.launch {
            repository.insertServiceItem(service)
        }
    }

    fun deleteService(service: ServiceItem) {
        viewModelScope.launch {
            repository.deleteServiceItem(service)
        }
    }

    // --- FUNGSI KERANJANG & CHECKOUT ---

    // Fungsi menambah item ke keranjang sementara
    fun addToCart(shoeBrand: String, serviceName: String, price: Int) {
        val detailSepatu = "$shoeBrand ($serviceName)"
        _cartItems.value = _cartItems.value + Shoe(name = detailSepatu, price = price)
    }

    // Fungsi menghapus item dari keranjang
    fun removeFromCart(shoe: Shoe) {
        _cartItems.value = _cartItems.value.filter { it != shoe }
        if (_cartItems.value.isEmpty()) {
            _cartCustomerName.value = ""
        }
    }

    fun getTotalPrice(): Int = _cartItems.value.sumOf { it.price }

    // Logika Checkout: Menggabungkan semua item keranjang menjadi 1 transaksi
    fun checkout(customerName: String, onComplete: () -> Unit) {
        if (_cartItems.value.isNotEmpty()) {
            val key = transactionsRef.push().key ?: return

            // Gabungkan semua nama layanan menjadi satu string
            val combinedServices = _cartItems.value.joinToString(", ") { it.name }

            val newTransaction = Transaction(
                customerName = customerName,
                serviceNames = combinedServices,
                totalPrice = getTotalPrice(),
                date = System.currentTimeMillis(),
                status = "Diproses",
                firebaseKey = key
            )

            transactionsRef.child(key).setValue(newTransaction).addOnSuccessListener {
                // Reset keranjang setelah berhasil
                _cartItems.value = emptyList()
                _cartCustomerName.value = ""
                onComplete()
            }
        }
    }

    fun updateTransactionStatus(firebaseKey: String, newStatus: String) {
        if (firebaseKey.isNotBlank()) {
            transactionsRef.child(firebaseKey).child("status").setValue(newStatus)
        }
    }

    fun deleteShoe(shoe: Shoe) {
        // Logic delete layanan jika diperlukan
    }
}

// Factory untuk inisialisasi di MainActivity
class ShoeViewModelFactory(private val repository: ShoeRepository) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}