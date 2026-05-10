package com.albrk.shoescare.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfHelper {

    // --- FUNGSI 1: GENERATE STRUK PDF (DESAIN PREMIUM KAMU) ---
    fun generateStruk(context: Context, customerName: String, services: String, totalPrice: Int) {
        val pdfDocument = PdfDocument()

        // Ukuran kertas ala Struk Thermal Printer (Lebar 400, Tinggi 600)
        val pageInfo = PdfDocument.PageInfo.Builder(400, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint()
        val dashedPaint = Paint()

        canvas.drawColor(Color.WHITE)

        // HEADER
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 26f
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText("ALBRK SHOES CARE", 200f, 60f, titlePaint)

        paint.textSize = 14f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Premium Shoe Treatment", 200f, 85f, paint)
        paint.textSize = 12f
        canvas.drawText("Jl. Madiun Raya No. 123", 200f, 105f, paint)

        // GARIS PUTUS-PUTUS
        dashedPaint.style = Paint.Style.STROKE
        dashedPaint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
        dashedPaint.strokeWidth = 2f
        dashedPaint.color = Color.DKGRAY
        canvas.drawLine(20f, 130f, 380f, 130f, dashedPaint)

        // INFO PELANGGAN
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 14f
        paint.color = Color.BLACK
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        canvas.drawText("Tanggal  : $date", 20f, 160f, paint)
        canvas.drawText("Klien      : $customerName", 20f, 185f, paint)

        canvas.drawLine(20f, 210f, 380f, 210f, dashedPaint)

        // DAFTAR LAYANAN (Dukungan Multi-Item)
        titlePaint.textSize = 14f
        titlePaint.textAlign = Paint.Align.LEFT
        canvas.drawText("ITEM / LAYANAN :", 20f, 240f, titlePaint)

        var yPos = 265f
        val items = services.split(", ")
        paint.textSize = 14f
        for (item in items) {
            canvas.drawText("• $item", 30f, yPos, paint)
            yPos += 25f
        }

        canvas.drawLine(20f, yPos + 15f, 380f, yPos + 15f, dashedPaint)

        // TOTAL
        titlePaint.textSize = 22f
        titlePaint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL", 20f, yPos + 55f, titlePaint)

        titlePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Rp $totalPrice", 380f, yPos + 55f, titlePaint)

        canvas.drawLine(20f, yPos + 80f, 380f, yPos + 80f, dashedPaint)

        // FOOTER
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 12f
        canvas.drawText("Terima kasih atas kepercayaan Anda!", 200f, yPos + 120f, paint)
        canvas.drawText("Sepatu bersih, langkah makin percaya diri.", 200f, yPos + 140f, paint)

        pdfDocument.finishPage(page)

        // SIMPAN FILE
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "Struk_${customerName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val file = File(dir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF Berhasil Tersimpan di Download", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal simpan PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    // --- FUNGSI 2: KIRIM STRUK VIA WHATSAPP ---
    fun sendWhatsApp(context: Context, customerName: String, services: String, totalPrice: Int) {
        val message = """
            *ALBRK SHOES CARE* 👟
            ---------------------------
            Halo Kak *$customerName*, 
            Terima kasih telah mempercayakan sepatu Anda kepada kami!
            
            *Rincian Layanan:*
            ${services.replace(", ", "\n• ")}
            
            *Total Biaya:* Rp $totalPrice
            *Status:* Diproses
            ---------------------------
            Pantau terus status cucian Anda di aplikasi kami ya Kak!
        """.trimIndent()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp tidak terpasang boss!", Toast.LENGTH_SHORT).show()
        }
    }
}