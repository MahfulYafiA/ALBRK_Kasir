package com.albrk.shoescare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val customerName: String = "",
    val customerPhone: String = "", // BARU: Menampung WA dari pelanggan
    val address: String = "",       // BARU: Menampung Alamat dari pelanggan
    val serviceNames: String = "",
    val totalPrice: Int = 0,

    val date: Long = System.currentTimeMillis(),

    // UBAH: Status awalnya kita samakan jadi "Diajukan"
    val status: String = "Diajukan",

    // Menyimpan ID unik dari Firebase agar bisa diedit nantinya
    val firebaseKey: String = ""
)