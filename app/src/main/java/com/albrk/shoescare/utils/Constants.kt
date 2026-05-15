package com.albrk.shoescare.utils

/**
 * OBJECT CONSTANTS
 * Fungsi: Sebagai pusat penyimpanan variabel statis yang nilainya tidak pernah berubah.
 * Keuntungan: Menghindari kesalahan pengetikan (typo) dan memudahkan pemeliharaan kode (Maintenance).
 */
object Constants {

    // =======================================================
    // 1. FIREBASE CONFIGURATION
    // Nama-nama cabang (node) utama di Firebase Realtime Database.
    // =======================================================
    const val FIREBASE_URL = "https://albrk-shoescare-default-rtdb.asia-southeast1.firebasedatabase.app"
    const val REF_MASTER_SERVICES = "master_services" // Jalur data daftar layanan
    const val REF_TRANSACTIONS = "transactions"       // Jalur data antrean transaksi

    // =======================================================
    // 2. NAVIGATION ROUTES (RUTE NAVIGASI)
    // Digunakan di MyApp.kt dan MainScreen.kt agar navigasi antar layar konsisten.
    // =======================================================
    const val ROUTE_LOGIN = "login"
    const val ROUTE_DASHBOARD = "staff_dashboard"
    const val ROUTE_MANAGE = "manage_service"
    const val ROUTE_FINANCE = "finance_report"
    const val ROUTE_PROFILE = "profile"

    // =======================================================
    // 3. TRANSACTION STATUS (STATUS PESANAN)
    // Standarisasi teks status agar sinkron antara Dashboard dan Laporan.
    // =======================================================
    const val STATUS_PENDING = "Diajukan"
    const val STATUS_PROCESS = "Diproses"
    const val STATUS_SUCCESS = "Selesai"
    const val STATUS_CANCEL = "Dibatalkan"

    // =======================================================
    // 4. UI TEXT & METADATA
    // Teks statis untuk tampilan agar seragam di seluruh aplikasi.
    // =======================================================
    const val APP_NAME = "ALBRK SHOES CARE"
    const val CURRENCY_PREFIX = "Rp "
    const val EMPTY_TRX_MESSAGE = "Belum ada antrean transaksi masuk."
}