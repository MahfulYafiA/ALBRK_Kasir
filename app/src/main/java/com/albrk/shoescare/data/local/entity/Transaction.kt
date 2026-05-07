package com.albrk.shoescare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val serviceNames: String, // Contoh: "Deep Clean, Unyellowing"
    val totalPrice: Int,
    val date: Long = System.currentTimeMillis(),
    val status: String = "Selesai" // Bisa dikembangkan jadi "Proses/Selesai"
)