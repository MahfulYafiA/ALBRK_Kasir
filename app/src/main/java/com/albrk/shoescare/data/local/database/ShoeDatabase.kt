package com.albrk.shoescare.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.albrk.shoescare.data.local.dao.ShoeDao
import com.albrk.shoescare.data.local.entity.ServiceItem // Tambahan import
import com.albrk.shoescare.data.local.entity.Shoe
import com.albrk.shoescare.data.local.entity.Transaction

// 1. Tambahkan ServiceItem::class ke dalam daftar entities.
// 2. Naikkan version menjadi 4 karena kita menambah kolom baru (imageUri) di tabel ServiceItem.
@Database(entities = [Shoe::class, Transaction::class, ServiceItem::class], version = 4, exportSchema = false)
abstract class ShoeDatabase : RoomDatabase() {

    abstract fun shoeDao(): ShoeDao

    companion object {
        @Volatile
        private var INSTANCE: ShoeDatabase? = null

        fun getDatabase(context: Context): ShoeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShoeDatabase::class.java,
                    "shoe_database"
                )
                    // Karena kita menggunakan fallbackToDestructiveMigration,
                    // Room akan otomatis mereset database ke versi 4 tanpa error.
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}