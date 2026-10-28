package com.vcm.adoptamascota

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vcm.adoptamascota.ui.navigation.AppNavigation
import com.vcm.adoptamascota.ui.theme.VitalMascotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VitalMascotTheme {
                AppNavigation()
            }
        }
    }
}
