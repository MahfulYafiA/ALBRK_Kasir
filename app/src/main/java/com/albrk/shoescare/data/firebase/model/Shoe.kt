package com.albrk.shoescare.data.firebase.model

// Hapus import androidx.room...

data class Shoe(
    // ID bisa tetap dibiarkan untuk membedakan item di keranjang (opsional)
    val id: Int = 0,

    // Default value "" agar aman saat diolah
    val name: String = "",

    // Default value 0 agar aman saat dijumlahkan
    val price: Int = 0
)