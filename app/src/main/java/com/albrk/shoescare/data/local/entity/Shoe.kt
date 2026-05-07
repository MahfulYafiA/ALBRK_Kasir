package com.albrk.shoescare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity memberitahu Room bahwa data class ini akan diubah menjadi sebuah tabel di database.
// tableName = "shoe_table" berarti nama tabel di dalam database HP-nya nanti adalah 'shoe_table'.
@Entity(tableName = "shoe_table")
data class Shoe(

    // @PrimaryKey menjadikan 'id' sebagai nomor urut unik (identitas) untuk setiap data.
    // autoGenerate = true membuat Room mengisikan nomor ID ini secara otomatis (1, 2, 3, dst.)
    // sehingga kita tidak perlu repot memasukkan ID saat menambah layanan baru.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Kolom untuk menyimpan nama layanan (contoh: "Fast Clean", "Deep Clean", "Unyellowing")
    val name: String,

    // Kolom untuk menyimpan harga layanan (contoh: 35000, 50000)
    val price: Int
)