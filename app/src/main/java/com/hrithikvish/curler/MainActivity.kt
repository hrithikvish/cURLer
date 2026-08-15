package com.hrithikvish.curler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hrithikvish.curler.ui.navigation.CurlerNavHost
import com.hrithikvish.curler.ui.theme.CurlerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurlerTheme {
                CurlerNavHost()
            }
        }
    }
}
