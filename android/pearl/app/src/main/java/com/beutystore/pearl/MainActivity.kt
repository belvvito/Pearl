package com.beutystore.pearl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.beutystore.pearl.navigation.PearlNavigation
import com.beutystore.pearl.ui.theme.PearlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PearlTheme {
                PearlNavigation()
            }
        }
    }
}