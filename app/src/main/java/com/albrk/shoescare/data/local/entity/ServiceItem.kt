package com.albrk.shoescare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class ServiceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val price: Int,

    // imageRes tetap ada untuk mendukung gambar bawaan dari folder drawable
    val imageRes: Int,

    // KOLOM BARU: Untuk menyimpan lokasi (URI) foto dari galeri HP
    // Kita buat optional (bisa null) agar tidak error jika tidak ada foto
    val imageUri: String? = null
)