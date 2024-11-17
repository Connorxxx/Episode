package com.connor.episode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.connor.episode.ui.HOME
import com.connor.episode.ui.screen.HomeScreen
import com.connor.episode.ui.theme.EpisodeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EpisodeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavMain(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun NavMain(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = HOME, modifier = modifier) {
        composable(HOME) {
            HomeScreen()
        }
    }
}