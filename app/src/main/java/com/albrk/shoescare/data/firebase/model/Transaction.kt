package com.albrk.shoescare.data.firebase.model

// Hapus import androidx.room...

data class Transaction(
    // ID internal/lokal (opsional, bisa dibiarkan atau dihapus,
    // karena kunci utamanya sekarang adalah firebaseKey)
    val id: Int = 0,

    val customerName: String = "",
    val customerPhone: String = "", // Menampung WA dari pelanggan
    val address: String = "",       // Menampung Alamat dari pelanggan
    val serviceNames: String = "",
    val totalPrice: Int = 0,

    val date: Long = System.currentTimeMillis(),

    // Status awal pemesanan
    val status: String = "Diajukan",

    // Menyimpan ID unik dari Firebase (sangat penting untuk update status)
    val firebaseKey: String = ""
)