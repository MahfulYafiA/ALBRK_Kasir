package com.albrk.shoescare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.albrk.shoescare.data.local.database.ShoeDatabase
import com.albrk.shoescare.data.repository.ShoeRepository
import com.albrk.shoescare.ui.MyApp
import com.albrk.shoescare.ui.theme.ALBRKSHOESCARETheme
import com.albrk.shoescare.viewmodel.ShoeViewModel
import com.albrk.shoescare.viewmodel.ShoeViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inisialisasi Database Room
        val database = ShoeDatabase.getDatabase(this)

        // 2. Inisialisasi Repository
        val repository = ShoeRepository(database.shoeDao())

        // 3. Membangun ViewModel dengan Factory
        val viewModel: ShoeViewModel by viewModels {
            ShoeViewModelFactory(repository)
        }

        setContent {
            // Jika nama thememu berbeda (misal: ShoesCareTheme), sesuaikan bagian ini
            ALBRKSHOESCARETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Memanggil layar utama MyApp dengan memasukkan mesin ViewModel-nya
                    MyApp(viewModel = viewModel)
                }
            }
        }
    }
}