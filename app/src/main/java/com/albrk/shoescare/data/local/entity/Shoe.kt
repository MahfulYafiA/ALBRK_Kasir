package com.albrk.shoescare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shoe_table")
data class Shoe(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // TAMBAHKAN = "" agar Firebase tidak error
    val name: String = "",

    // TAMBAHKAN = 0 agar Firebase tidak error
    val price: Int = 0
)