package com.taxlot.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.taxlot.android.ui.TaxlotApp
import com.taxlot.android.ui.theme.TaxlotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaxlotTheme {
                TaxlotApp()
            }
        }
    }
}
