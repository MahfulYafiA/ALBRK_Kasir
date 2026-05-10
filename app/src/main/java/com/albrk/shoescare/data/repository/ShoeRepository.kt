package com.albrk.shoescare.data.repository

import com.albrk.shoescare.data.local.dao.ShoeDao
import com.albrk.shoescare.data.local.entity.ServiceItem // Tambahan import
import com.albrk.shoescare.data.local.entity.Shoe
import com.albrk.shoescare.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

class ShoeRepository(private val shoeDao: ShoeDao) {

    // --- LOGIKA MENU LAYANAN DINAMIS (BARU) ---

    val allServiceItems: Flow<List<ServiceItem>> = shoeDao.getAllServiceItems()

    suspend fun insertServiceItem(service: ServiceItem) {
        shoeDao.insertServiceItem(service)
    }

    suspend fun updateServiceItem(service: ServiceItem) {
        shoeDao.updateServiceItem(service)
    }

    suspend fun deleteServiceItem(service: ServiceItem) {
        shoeDao.deleteServiceItem(service)
    }

    // --- LOGIKA KERANJANG SEPATU ---

    val allShoes: Flow<List<Shoe>> = shoeDao.getAllShoes()

    suspend fun insertShoe(shoe: Shoe) {
        shoeDao.insertShoe(shoe)
    }

    suspend fun updateShoe(shoe: Shoe) {
        shoeDao.updateShoe(shoe)
    }

    suspend fun deleteShoe(shoe: Shoe) {
        shoeDao.deleteShoe(shoe)
    }

    fun getShoeById(id: Int): Flow<Shoe> {
        return shoeDao.getShoeById(id)
    }

    // --- LOGIKA TRANSAKSI ---

    // Mengambil semua riwayat transaksi untuk laporan
    val allTransactions: Flow<List<Transaction>> = shoeDao.getAllTransactions()

    // Mengambil total omzet pendapatan
    val totalIncome: Flow<Int> = shoeDao.getTotalIncome()

    // Menyimpan transaksi baru saat kasir menekan tombol bayar
    suspend fun insertTransaction(transaction: Transaction) {
        shoeDao.insertTransaction(transaction)
    }
}