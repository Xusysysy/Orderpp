package com.opp.oder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.opp.oder.ui.OderAppContent
import com.opp.oder.ui.theme.OderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OderTheme {
                OderAppContent()
            }
        }
    }
}
