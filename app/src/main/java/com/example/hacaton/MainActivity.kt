package com.example.hacaton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hacaton.Compuse.HacatonApp
import com.example.hacaton.ui.theme.HacatonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HacatonTheme {
                HacatonApp()
            }
        }
    }
}
