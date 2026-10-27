package com.itsmesujan.jlpt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.itsmesujan.jlpt.ui.AppNav
import com.itsmesujan.jlpt.ui.AppViewModel
import com.itsmesujan.jlpt.ui.theme.JlptQuestTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // API 36 enforces edge-to-edge; opt in explicitly
        setContent {
            JlptQuestTheme {
                AppNav(viewModel)
            }
        }
    }
}
