package com.connor.episode.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.connor.episode.ui.theme.EpisodeTheme

@Composable
fun WebSocketScreen() {
    WebSocket()
}

@Composable
private fun WebSocket() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "WebSocket")
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EpisodeTheme {
        WebSocket()
    }
}