package com.albrk.shoescare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.albrk.shoescare.data.local.entity.ServiceItem
import com.albrk.shoescare.data.local.entity.Shoe
import com.albrk.shoescare.data.local.entity.Transaction
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * VIEWMODEL: OTAK APLIKASI
 * Fungsi: Menangani logika bisnis, mengelola State UI, dan berkomunikasi dengan Firebase.
 * ViewModel menjaga data tetap aman saat terjadi rotasi layar atau perubahan konfigurasi.
 */
class ShoeViewModel : ViewModel() { // Repository dihapus karena kita sudah beralih 100% ke Firebase

    // 1. KONEKSI DATABASE
    private val db = FirebaseDatabase.getInstance("https://albrk-shoescare-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val transactionsRef = db.getReference("transactions")
    private val masterServicesRef = db.getReference("master_services")

    // =======================================================
    // 2. STATE MANAGEMENT (Penyimpanan Data Sementara di RAM)
    // Menggunakan MutableStateFlow agar data bisa di-update secara internal
    // dan dibaca secara eksternal melalui StateFlow (ReadOnly).
    // =======================================================

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: StateFlow<List<Transaction>> = _allTransactions

    private val _cartItems = MutableStateFlow<List<Shoe>>(emptyList())
    val cartItems: StateFlow<List<Shoe>> = _cartItems

    private val _cartCustomerName = MutableStateFlow("")
    val cartCustomerName: StateFlow<String> = _cartCustomerName

    private val _allServices = MutableStateFlow<List<ServiceItem>>(emptyList())
    val allServices: StateFlow<List<ServiceItem>> = _allServices

    // Filter otomatis: Hanya menampilkan layanan yang statusnya 'isActive = true' untuk pelanggan
    val activeServices = allServices.map { list ->
        list.filter { it.isActive }
    }

    // Perhitungan otomatis laporan keuangan (Hanya yang statusnya 'Selesai')
    val totalPendapatan = allTransactions.map { transactions ->
        transactions.filter { it.status == "Selesai" }.sumOf { it.totalPrice }
    }

    init {
        // =======================================================
        // 3. REALTIME LISTENERS (Mata-mata Firebase)
        // Fungsi ini akan terus memantau database. Jika ada perubahan di Cloud,
        // UI aplikasi akan otomatis berubah detik itu juga.
        // =======================================================

        // Listener untuk Daftar Layanan
        masterServicesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ServiceItem>()
                for (data in snapshot.children) {
                    val name = data.child("name").getValue(String::class.java) ?: ""

                    // Validasi tipe data harga agar tidak crash jika di Firebase berupa String/Int
                    val priceRaw = data.child("price").value
                    val price = when (priceRaw) {
                        is Long -> priceRaw.toInt()
                        is Int -> priceRaw
                        is String -> priceRaw.toIntOrNull() ?: 0
                        else -> 0
                    }

                    val imageUri = data.child("imageUri").getValue(String::class.java)
                    val isActive = data.child("isActive").getValue(Boolean::class.java) ?: true

                    list.add(ServiceItem(id = data.key ?: "", name = name, price = price, imageUri = imageUri, isActive = isActive))
                }
                _allServices.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Listener untuk Riwayat Transaksi (Urutkan dari yang terbaru)
        transactionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Transaction>()
                for (data in snapshot.children) {
                    val transaction = data.getValue(Transaction::class.java)
                    if (transaction != null) {
                        list.add(transaction.copy(firebaseKey = data.key ?: ""))
                    }
                }
                _allTransactions.value = list.sortedByDescending { it.date }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // =======================================================
    // 4. OPERASI CRUD (Create, Read, Update, Delete)
    // =======================================================

    // Tambah atau Update Layanan
    fun addMasterService(service: ServiceItem) {
        val key = if (service.id.isEmpty()) masterServicesRef.push().key ?: "" else service.id
        if (key.isNotEmpty()) {
            val serviceMap = mapOf(
                "id" to key, "name" to service.name, "price" to service.price,
                "imageRes" to 0, "imageUri" to service.imageUri, "isActive" to service.isActive
            )
            masterServicesRef.child(key).setValue(serviceMap)
        }
    }

    // Update Cepat Status Aktif (Tanpa mengubah data lain)
    fun updateServiceStatus(serviceId: String, isActiveStatus: Boolean) {
        if (serviceId.isNotEmpty()) masterServicesRef.child(serviceId).child("isActive").setValue(isActiveStatus)
    }

    // Hapus Layanan
    fun deleteMasterService(id: String) {
        if (id.isNotEmpty()) masterServicesRef.child(id).removeValue()
    }

    // Update Status Transaksi (Diajukan -> Diproses -> Selesai)
    fun updateTransactionStatus(firebaseKey: String, newStatus: String) {
        if (firebaseKey.isNotBlank()) transactionsRef.child(firebaseKey).child("status").setValue(newStatus)
    }

    // =======================================================
    // 5. LOGIKA KERANJANG (CART)
    // =======================================================

    fun addToCart(shoeBrand: String, serviceName: String, price: Int) {
        val detail = "$shoeBrand ($serviceName)"
        _cartItems.value = _cartItems.value + Shoe(name = detail, price = price)
    }

    fun removeFromCart(shoe: Shoe) {
        _cartItems.value = _cartItems.value.filter { it != shoe }
    }

    fun checkout(customerName: String, onComplete: () -> Unit) {
        if (_cartItems.value.isNotEmpty()) {
            val key = transactionsRef.push().key ?: return
            val combinedServices = _cartItems.value.joinToString(", ") { it.name }

            val newTransaction = Transaction(
                customerName = customerName,
                serviceNames = combinedServices,
                totalPrice = _cartItems.value.sumOf { it.price },
                date = System.currentTimeMillis(),
                status = "Diajukan",
                firebaseKey = key
            )

            transactionsRef.child(key).setValue(newTransaction).addOnSuccessListener {
                _cartItems.value = emptyList() // Kosongkan keranjang setelah checkout
                onComplete()
            }
        }
    }
}

/**
 * VIEWMODEL FACTORY
 * Fungsi: Boilerplate code agar ViewModel bisa dibuat tanpa parameter repository.
 */
class ShoeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}