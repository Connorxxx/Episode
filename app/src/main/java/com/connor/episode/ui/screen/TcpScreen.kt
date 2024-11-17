package com.connor.episode.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.connor.episode.ui.theme.EpisodeTheme

@Composable
fun TcpScreen() {
    Tcp()
}

@Composable
private fun Tcp() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Tcp")
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EpisodeTheme {
        Tcp()
    }
}