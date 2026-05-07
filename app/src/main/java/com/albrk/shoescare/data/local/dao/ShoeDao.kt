package com.albrk.shoescare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.albrk.shoescare.data.local.entity.Shoe
import com.albrk.shoescare.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoeDao {

    // --- BAGIAN LAYANAN (KATALOG) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoe(shoe: Shoe)

    @Update
    suspend fun updateShoe(shoe: Shoe)

    @Delete
    suspend fun deleteShoe(shoe: Shoe)

    @Query("SELECT * FROM shoe_table ORDER BY id ASC")
    fun getAllShoes(): Flow<List<Shoe>>

    @Query("SELECT * FROM shoe_table WHERE id = :id")
    fun getShoeById(id: Int): Flow<Shoe>


    // --- BAGIAN TRANSAKSI (KASIR PROFESIONAL) ---

    // 1. Menyimpan data transaksi setelah tombol "Bayar" diklik
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: Transaction)

    // 2. Mengambil semua riwayat transaksi untuk laporan keuangan
    // Diurutkan berdasarkan tanggal terbaru (date DESC)
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // 3. (Opsional) Menghitung total omzet pendapatan secara otomatis
    @Query("SELECT SUM(totalPrice) FROM transactions")
    fun getTotalIncome(): Flow<Int>
}