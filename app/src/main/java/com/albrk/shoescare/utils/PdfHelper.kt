package com.albrk.shoescare.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.albrk.shoescare.data.local.entity.Transaction
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * PDF HELPER OBJECT
 * Fungsi: Utilitas untuk membuat (generate) dokumen PDF secara programatik.
 * Menggunakan library asli Android: android.graphics.pdf.PdfDocument.
 */
object PdfHelper {

    /**
     * FUNGSI 1: MENGHASILKAN LAPORAN KEUANGAN (UKURAN A4)
     * Digunakan untuk merekap banyak transaksi dalam satu lembar kertas resmi.
     */
    fun generateLaporanKeuangan(
        context: Context,
        transactions: List<Transaction>,
        totalIncome: Int,
        periodStart: String,
        periodEnd: String
    ) {
        val pdfDocument = PdfDocument()

        // --- SETTING HALAMAN ---
        // Ukuran A4 Standar dalam satuan 'points' (595 x 842). 1 point = 1/72 inch.
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        // Canvas adalah media untuk 'menggambar' teks dan garis pada PDF
        val canvas: Canvas = page.canvas
        // Paint adalah 'kuas' yang menentukan warna, ukuran teks, dan ketebalan garis
        val paint = Paint()

        // --- MENGGAMBAR JUDUL LAPORAN ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("LAPORAN PENDAPATAN ALBRK", 297f, 60f, paint) // 297f adalah titik tengah lebar A4

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        canvas.drawText("Periode: $periodStart - $periodEnd", 297f, 85f, paint)

        // Menggambar Garis Horizontal (Header)
        paint.strokeWidth = 2f
        canvas.drawLine(50f, 110f, 545f, 110f, paint)

        // --- MENGGAMBAR HEADER TABEL ---
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TANGGAL", 55f, 140f, paint)
        canvas.drawText("PELANGGAN", 160f, 140f, paint)
        canvas.drawText("TOTAL", 460f, 140f, paint)

        canvas.drawLine(50f, 150f, 545f, 150f, paint)

        // --- LOOPING DATA TRANSAKSI (ISI TABEL) ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        var yPos = 180f // Koordinat vertikal awal untuk baris pertama
        val sdf = SimpleDateFormat("dd/MM/yy", Locale("id", "ID"))

        for (trans in transactions) {
            // Logika Keamanan: Jika data terlalu banyak, hentikan gambar sebelum menabrak batas bawah kertas
            if (yPos > 750f) break

            canvas.drawText(sdf.format(Date(trans.date)), 55f, yPos, paint)

            // Membatasi panjang nama agar tidak bertabrakan dengan kolom total (Ellipsize manual)
            val displayName = if(trans.customerName.length > 25) trans.customerName.take(22) + "..." else trans.customerName
            canvas.drawText(displayName, 160f, yPos, paint)

            canvas.drawText("Rp ${trans.totalPrice}", 460f, yPos, paint)

            yPos += 25f // Memberi jarak 25 point untuk baris berikutnya
        }

        // Garis Penutup Tabel
        canvas.drawLine(50f, yPos, 545f, yPos, paint)

        // --- RINGKASAN TOTAL ---
        yPos += 40f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        canvas.drawText("TOTAL PENDAPATAN: Rp $totalIncome", 55f, yPos, paint)

        // --- FOOTER TANDA TANGAN ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Madiun, ${SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())}", 530f, yPos + 60f, paint)
        canvas.drawText("Admin Produksi ALBRK", 530f, yPos + 80f, paint)
        canvas.drawText("____________________", 530f, yPos + 140f, paint)

        // Menyelesaikan halaman dan menutup dokumen
        pdfDocument.finishPage(page)

        val fileName = "Laporan_Keuangan_${System.currentTimeMillis()}.pdf"
        savePdfFile(context, pdfDocument, fileName)
    }

    /**
     * FUNGSI 2: MENGHASILKAN STRUK PEMBAYARAN (UKURAN KECIL/THERMAL)
     * Digunakan untuk mencetak nota instan per transaksi.
     */
    fun generateStruk(context: Context, customerName: String, services: String, totalPrice: Int) {
        val pdfDocument = PdfDocument()

        // Ukuran kertas disesuaikan dengan Struk Thermal (Lebar 400, Tinggi 600)
        val pageInfo = PdfDocument.PageInfo.Builder(400, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Judul Struk
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("ALBRK SHOES CARE", 200f, 60f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        canvas.drawText("Premium Shoe Treatment", 200f, 85f, paint)

        canvas.drawLine(20f, 110f, 380f, 110f, paint)

        // Detail Transaksi
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 14f
        canvas.drawText("Nama  : $customerName", 30f, 150f, paint)
        canvas.drawText("Item  : $services", 30f, 180f, paint)

        canvas.drawLine(20f, 210f, 380f, 210f, paint)

        // Bagian Total
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        canvas.drawText("TOTAL : Rp $totalPrice", 30f, 250f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Terima kasih atas kunjungan Anda!", 200f, 320f, paint)

        pdfDocument.finishPage(page)

        val fileName = "Struk_${customerName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        savePdfFile(context, pdfDocument, fileName)
    }

    /**
     * HELPER: PROSES PENYIMPANAN KE FILESYSTEM
     * Fungsi ini bertugas memindahkan objek PdfDocument dari memori RAM ke file fisik di storage.
     */
    private fun savePdfFile(context: Context, pdfDocument: PdfDocument, fileName: String) {
        // Mengarahkan lokasi simpan ke folder 'Download' agar mudah ditemukan user
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, fileName)

        try {
            // Proses Stream Data: Menulis data PDF ke dalam file fisik
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF tersimpan di folder Download", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal menyimpan PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            // Sangat penting untuk menutup document agar tidak terjadi Memory Leak
            pdfDocument.close()
        }
    }
}