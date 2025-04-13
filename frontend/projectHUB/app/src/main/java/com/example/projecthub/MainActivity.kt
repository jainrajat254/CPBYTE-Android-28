package com.example.projecthub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.projecthub.navigation.appNavigation
import com.example.projecthub.ui.theme.ProjectHUBTheme
import com.example.projecthub.viewModel.ThemeViewModel
import com.example.projecthub.viewModel.authViewModel

class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : authViewModel by viewModels()
        setContent {
            ProjectHUBTheme(themeViewModel) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    appNavigation(modifier = Modifier.padding(innerPadding),authViewModel = authViewModel,themeViewModel = themeViewModel)
                }
            }
        }
    }
}

