package com.albrk.shoescare.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.albrk.shoescare.data.firebase.model.ServiceItem
import com.albrk.shoescare.data.firebase.model.Shoe
import com.albrk.shoescare.data.firebase.model.Transaction
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * VIEWMODEL PRO: OTAK APLIKASI STAFF/ADMIN
 * Fungsi: Mengelola Data Master, Transaksi Cloud, Upload Foto, dan Sistem Kasir Offline.
 */
class ShoeViewModel : ViewModel() {

    // =======================================================
    // 1. KONEKSI CLOUD (REALTIME DB & STORAGE)
    // =======================================================
    private val db = FirebaseDatabase.getInstance("https://albrk-shoescare-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val transactionsRef = db.getReference("transactions")
    private val masterServicesRef = db.getReference("master_services")

    // [BARU] Koneksi ke Firebase Storage untuk menyimpan foto layanan
    private val storage = FirebaseStorage.getInstance().reference

    // =======================================================
    // 2. STATE MANAGEMENT (IN-MEMORY)
    // =======================================================
    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: StateFlow<List<Transaction>> = _allTransactions

    private val _cartItems = MutableStateFlow<List<Shoe>>(emptyList())
    val cartItems: StateFlow<List<Shoe>> = _cartItems

    private val _cartCustomerName = MutableStateFlow("")
    val cartCustomerName: StateFlow<String> = _cartCustomerName

    private val _allServices = MutableStateFlow<List<ServiceItem>>(emptyList())
    val allServices: StateFlow<List<ServiceItem>> = _allServices

    val activeServices = allServices.map { list -> list.filter { it.isActive } }

    // Perhitungan Laporan Keuangan
    val totalPendapatan = allTransactions.map { transactions ->
        transactions.filter { it.status == "Selesai" }.sumOf { it.totalPrice }
    }

    init {
        // =======================================================
        // 3. REALTIME LISTENERS
        // =======================================================
        masterServicesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ServiceItem>()
                for (data in snapshot.children) {
                    val name = data.child("name").getValue(String::class.java) ?: ""
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

        transactionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Transaction>()
                for (data in snapshot.children) {
                    data.getValue(Transaction::class.java)?.let {
                        list.add(it.copy(firebaseKey = data.key ?: ""))
                    }
                }
                _allTransactions.value = list.sortedByDescending { it.date }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // =======================================================
    // 4. OPERASI CRUD MASTER DATA (HAK AKSES ADMIN)
    // =======================================================

    /**
     * [UPGRADE] Menyimpan Layanan beserta Foto ke Cloud Storage
     */
    fun saveServiceWithImage(id: String?, name: String, price: Int, imageUri: Uri?, existingImageUrl: String?, onComplete: (Boolean) -> Unit) {
        val key = id.takeIf { !it.isNullOrEmpty() } ?: masterServicesRef.push().key ?: return

        if (imageUri != null) {
            // Jika ada foto baru, upload ke Storage dulu
            val fileRef = storage.child("service_images/$key.jpg")
            fileRef.putFile(imageUri).addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val serviceMap = mapOf(
                        "id" to key, "name" to name, "price" to price,
                        "imageRes" to 0, "imageUri" to downloadUrl.toString(), "isActive" to true
                    )
                    masterServicesRef.child(key).setValue(serviceMap).addOnCompleteListener { onComplete(it.isSuccessful) }
                }.addOnFailureListener { onComplete(false) }
            }.addOnFailureListener { onComplete(false) }
        } else {
            // Jika tidak ada perubahan foto, langsung simpan teks ke Realtime DB
            val serviceMap = mapOf(
                "id" to key, "name" to name, "price" to price,
                "imageRes" to 0, "imageUri" to (existingImageUrl ?: ""), "isActive" to true
            )
            masterServicesRef.child(key).setValue(serviceMap).addOnCompleteListener { onComplete(it.isSuccessful) }
        }
    }

    fun updateServiceStatus(serviceId: String, isActiveStatus: Boolean) {
        if (serviceId.isNotEmpty()) masterServicesRef.child(serviceId).child("isActive").setValue(isActiveStatus)
    }

    /**
     * [UPGRADE] Menghapus Layanan + Menghapus Fotonya di Server
     */
    fun deleteMasterService(service: ServiceItem) {
        if (service.id.isNotEmpty()) {
            // Hapus dari Realtime DB
            masterServicesRef.child(service.id).removeValue()

            // Bersihkan file foto dari Storage agar tidak menuhin memori server
            if (service.imageUri?.startsWith("http") == true) {
                try {
                    val fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(service.imageUri)
                    fileRef.delete()
                } catch (e: Exception) {

                }
            }
        }
    }

    // =======================================================
    // 5. MANAJEMEN TRANSAKSI
    // =======================================================

    fun updateTransactionStatus(firebaseKey: String, newStatus: String) {
        if (firebaseKey.isNotBlank()) transactionsRef.child(firebaseKey).child("status").setValue(newStatus)
    }

    fun deleteTransaction(firebaseKey: String) {
        if (firebaseKey.isNotBlank()) transactionsRef.child(firebaseKey).removeValue()
    }

    // =======================================================
    // 6. LOGIKA KASIR (CART OFFLINE STAFF)
    // =======================================================

    fun addToCart(shoeBrand: String, serviceName: String, price: Int) {
        val detail = "$shoeBrand ($serviceName)"
        _cartItems.value = _cartItems.value + Shoe(name = detail, price = price)
    }

    fun removeFromCart(shoe: Shoe) {
        _cartItems.value = _cartItems.value.filter { it != shoe }
    }

    fun checkout(customerName: String, customerPhone: String, customerAddress: String, onComplete: () -> Unit) {
        if (_cartItems.value.isNotEmpty()) {
            val key = transactionsRef.push().key ?: return
            val combinedServices = _cartItems.value.joinToString(", ") { it.name }

            val newTransaction = Transaction(
                customerName = customerName,
                customerPhone = customerPhone,
                address = customerAddress,
                serviceNames = combinedServices,
                totalPrice = _cartItems.value.sumOf { it.price },
                date = System.currentTimeMillis(),
                status = "Diproses", // Kalau lewat kasir langsung diproses
                firebaseKey = key
            )

            transactionsRef.child(key).setValue(newTransaction).addOnSuccessListener {
                _cartItems.value = emptyList()
                onComplete()
            }
        }
    }
}

class ShoeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}