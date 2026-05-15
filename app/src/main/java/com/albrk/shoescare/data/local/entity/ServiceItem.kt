package com.albrk.shoescare.data.local.entity

// Hapus import androidx.room.Entity dan PrimaryKey

data class ServiceItem(
    // ID menggunakan String karena mengikuti Key (push ID) dari Firebase
    val id: String = "",

    val name: String = "",

    val price: Int = 0,

    // imageRes tetap ada untuk mendukung gambar bawaan (default dari drawable)
    val imageRes: Int = 0,

    // imageUri menyimpan nama file di drawable (misal: "fastclean")
    // atau URI path jika kedepannya pakai upload galeri
    val imageUri: String? = null,

    // FITUR PRODUKSI: Untuk mengontrol layanan tampil atau tidak di sisi pelanggan
    // Default true agar saat dibuat langsung aktif
    val isActive: Boolean = true
)