package com.albrk.shoescare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val customerName: String = "",
    val serviceNames: String = "",
    val totalPrice: Int = 0,

    val date: Long = System.currentTimeMillis(),

    // UBAH: Status awal sekarang adalah "Diproses"
    val status: String = "Diproses",

    // TAMBAHAN: Menyimpan ID unik dari Firebase agar bisa diedit nantinya
    val firebaseKey: String = ""
)