package com.albrk.shoescare.ui.screen.add

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShoeScreen(
    // Dua parameter ini adalah jembatan untuk berpindah layar dan menyimpan data
    navigateBack: () -> Unit,
    onSaveClick: (String, Int) -> Unit
) {
    // Variabel untuk menyimpan teks yang diketik pengguna sementara
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Layanan") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Kolom input untuk Nama Layanan
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Layanan (Contoh: Deep Clean)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Kolom input untuk Harga
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Harga (Contoh: 50000)") },
                modifier = Modifier.fillMaxWidth(),
                // Memaksa keyboard yang muncul hanya berupa angka
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Mendorong tombol simpan ke bagian paling bawah layar
            Spacer(modifier = Modifier.weight(1f))

            // Tombol Simpan
            Button(
                onClick = {
                    // Pengecekan: Apakah kolom kosong?
                    if (name.isNotBlank() && price.isNotBlank()) {
                        val priceInt = price.toIntOrNull()
                        // Pengecekan: Apakah harga yang dimasukkan benar-benar angka?
                        if (priceInt != null) {
                            onSaveClick(name, priceInt) // Mengirim data untuk disimpan
                        } else {
                            Toast.makeText(context, "Harga harus berupa angka bulat", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Semua data wajib diisi!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan Layanan")
            }
        }
    }
}