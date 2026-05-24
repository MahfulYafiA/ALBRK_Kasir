package com.albrk.shoescare.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.albrk.shoescare.data.firebase.model.ServiceItem
import com.albrk.shoescare.data.firebase.model.Transaction
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ShoeViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance("https://albrk-shoescare-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val masterServicesRef = db.getReference("master_services")
    private val transactionsRef = db.getReference("transactions")
    private val usersRef = db.getReference("users")
    private val storage = FirebaseStorage.getInstance().reference

    // State Management
    private val _allServices = MutableStateFlow<List<ServiceItem>>(emptyList())
    val allServices: StateFlow<List<ServiceItem>> = _allServices

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: StateFlow<List<Transaction>> = _allTransactions

    init {
        fetchServices()
        fetchAllTransactions()
    }

    // ==========================================
    // 1. PENGAMBILAN DATA (REALTIME FETCH)
    // ==========================================
    private fun fetchServices() {
        masterServicesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ServiceItem>()
                for (data in snapshot.children) {
                    val price = (data.child("price").value as? Long)?.toInt() ?: 0
                    list.add(ServiceItem(
                        id = data.key ?: "",
                        name = data.child("name").getValue(String::class.java) ?: "",
                        price = price,
                        imageUri = data.child("imageUri").getValue(String::class.java),
                        isActive = data.child("isActive").getValue(Boolean::class.java) ?: true
                    ))
                }
                _allServices.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchAllTransactions() {
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

    // ==========================================
    // 2. MANAGEMENT TRANSAKSI (ANTREAN & CO)
    // ==========================================
    fun submitBooking(customerName: String, customerPhone: String, address: String, serviceNames: String, totalPrice: Int) {
        val key = transactionsRef.push().key ?: return
        val newTransaction = Transaction(
            customerName = customerName,
            customerPhone = customerPhone,
            address = address,
            serviceNames = serviceNames,
            totalPrice = totalPrice,
            date = System.currentTimeMillis(),
            status = "Diajukan",
            firebaseKey = key
        )
        transactionsRef.child(key).setValue(newTransaction)
    }

    fun updateTransactionStatus(firebaseKey: String, newStatus: String) {
        if (firebaseKey.isNotEmpty()) {
            transactionsRef.child(firebaseKey).child("status").setValue(newStatus)
        }
    }

    // ==========================================
    // 3. CRUD MASTER DATA LAYANAN (DENGAN CALLBACK BOOLEAN)
    // ==========================================
    fun saveServiceWithImage(
        id: String?,
        name: String,
        price: Int,
        imageUri: Uri?,
        existingImageUrl: String?,
        onComplete: (Boolean) -> Unit
    ) {
        val key = if (!id.isNullOrBlank()) id else masterServicesRef.push().key ?: return

        if (imageUri != null) {
            // Jika mengunggah gambar baru ke Storage
            val fileRef = storage.child("service_images/$key.jpg")
            fileRef.putFile(imageUri)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        val serviceMap = mapOf(
                            "name" to name,
                            "price" to price,
                            "imageUri" to uri.toString(),
                            "isActive" to true
                        )
                        masterServicesRef.child(key).setValue(serviceMap)
                            .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
                    }.addOnFailureListener { onComplete(false) }
                }
                .addOnFailureListener { onComplete(false) }
        } else {
            // Jika update teks saja tanpa mengganti gambar
            val serviceMap = mapOf(
                "name" to name,
                "price" to price,
                "imageUri" to (existingImageUrl ?: ""),
                "isActive" to true
            )
            masterServicesRef.child(key).setValue(serviceMap)
                .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
        }
    }

    fun updateServiceStatus(id: String, isActive: Boolean) {
        if (id.isNotEmpty()) {
            masterServicesRef.child(id).child("isActive").setValue(isActive)
        }
    }

    fun deleteMasterService(id: String, onComplete: () -> Unit) {
        if (id.isNotEmpty()) {
            masterServicesRef.child(id).removeValue().addOnCompleteListener { onComplete() }
        }
    }

    // ==========================================
    // 4. MANAJEMEN PROFIL KASIR
    // ==========================================
    fun getUserProfile(uid: String, onResult: (String, String, String, String) -> Unit) {
        usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                val address = snapshot.child("address").getValue(String::class.java) ?: ""
                val photoUrl = snapshot.child("photoUrl").getValue(String::class.java) ?: ""
                onResult(name, phone, address, photoUrl)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
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